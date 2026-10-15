import { Component, OnInit, ViewChild, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSort, MatSortModule, Sort } from '@angular/material/sort';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { EmployeeService } from '../../../core/services/employee.service';
import { OrganizationService } from '../../../core/services/organization.service';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { EmployeeSummary, EmployeeSearchParams, EmploymentStatus } from '../../../core/models/employee.model';
import { Department, Country } from '../../../core/models/organization.model';
import { EmployeeFormDialogComponent } from '../employee-form-dialog/employee-form-dialog.component';
import { StatusDialogComponent } from '../status-dialog/status-dialog.component';

@Component({
  selector: 'app-employee-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    MatCardModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatDialogModule,
    MatProgressSpinnerModule,
    MatTooltipModule
  ],
  templateUrl: './employee-list.component.html',
  styleUrls: ['./employee-list.component.scss']
})
export class EmployeeListComponent implements OnInit {
  private employeeService = inject(EmployeeService);
  private orgService = inject(OrganizationService);
  authService = inject(AuthService);
  private notification = inject(NotificationService);
  private dialog = inject(MatDialog);
  private router = inject(Router);
  private fb = inject(FormBuilder);

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  displayedColumns: string[] = [
    'employeeCode',
    'fullName',
    'email',
    'departmentName',
    'countryName',
    'jobTitle',
    'status',
    'actions'
  ];

  employees = signal<EmployeeSummary[]>([]);
  totalElements = signal<number>(0);
  pageSize = signal<number>(20);
  pageIndex = signal<number>(0);
  isLoading = signal<boolean>(false);

  departments = signal<Department[]>([]);
  countries = signal<Country[]>([]);

  filterForm: FormGroup = this.fb.group({
    search: [''],
    departmentId: [null],
    countryId: [null],
    status: [null]
  });

  private currentSortBy = 'id';
  private currentSortDir: 'asc' | 'desc' = 'asc';

  ngOnInit(): void {
    this.loadReferenceFilters();
    this.setupFilterListeners();
    this.loadEmployees();
  }

  private loadReferenceFilters(): void {
    this.orgService.getDepartments().subscribe(res => {
      if (res.success) this.departments.set(res.data);
    });

    this.orgService.getCountries().subscribe(res => {
      if (res.success) this.countries.set(res.data);
    });
  }

  private setupFilterListeners(): void {
    this.filterForm.get('search')?.valueChanges
      .pipe(debounceTime(350), distinctUntilChanged())
      .subscribe(() => {
        this.pageIndex.set(0);
        this.loadEmployees();
      });

    this.filterForm.get('departmentId')?.valueChanges.subscribe(() => {
      this.pageIndex.set(0);
      this.loadEmployees();
    });

    this.filterForm.get('countryId')?.valueChanges.subscribe(() => {
      this.pageIndex.set(0);
      this.loadEmployees();
    });

    this.filterForm.get('status')?.valueChanges.subscribe(() => {
      this.pageIndex.set(0);
      this.loadEmployees();
    });
  }

  loadEmployees(): void {
    this.isLoading.set(true);
    const filterVals = this.filterForm.value;

    const params: EmployeeSearchParams = {
      page: this.pageIndex(),
      size: this.pageSize(),
      search: filterVals.search || undefined,
      departmentId: filterVals.departmentId || undefined,
      countryId: filterVals.countryId || undefined,
      status: filterVals.status || undefined,
      sortBy: this.currentSortBy,
      sortDir: this.currentSortDir
    };

    this.employeeService.getEmployees(params).subscribe({
      next: (res) => {
        this.isLoading.set(false);
        if (res.success && res.data) {
          this.employees.set(res.data.content);
          this.totalElements.set(res.data.totalElements);
        }
      },
      error: () => {
        this.isLoading.set(false);
        this.notification.error('Failed to load employees list');
      }
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.loadEmployees();
  }

  onSortChange(sort: Sort): void {
    this.currentSortBy = sort.active ? sort.active : 'id';
    this.currentSortDir = (sort.direction as 'asc' | 'desc') || 'asc';
    this.pageIndex.set(0);
    this.loadEmployees();
  }

  clearFilters(): void {
    this.filterForm.reset({
      search: '',
      departmentId: null,
      countryId: null,
      status: null
    });
    this.pageIndex.set(0);
    this.loadEmployees();
  }

  viewDetails(employee: EmployeeSummary): void {
    this.router.navigate(['/employees', employee.id]);
  }

  openAddEmployeeDialog(): void {
    const dialogRef = this.dialog.open(EmployeeFormDialogComponent, {
      width: '600px',
      data: { isEdit: false }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadEmployees();
      }
    });
  }

  openEditEmployeeDialog(employee: EmployeeSummary): void {
    const dialogRef = this.dialog.open(EmployeeFormDialogComponent, {
      width: '600px',
      data: { isEdit: true, employee }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadEmployees();
      }
    });
  }

  openStatusDialog(employee: EmployeeSummary): void {
    const dialogRef = this.dialog.open(StatusDialogComponent, {
      width: '450px',
      data: {
        employeeId: employee.id,
        employeeName: `${employee.firstName} ${employee.lastName}`,
        currentStatus: employee.status
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.employeeService.updateStatus(employee.id, result).subscribe({
          next: () => {
            this.notification.success(`Status updated for ${employee.employeeCode}`);
            this.loadEmployees();
          },
          error: (err) => {
            this.notification.error(err.error?.message || 'Failed to update status');
          }
        });
      }
    });
  }
}
