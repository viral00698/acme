import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ReportService, ReportFilterParams } from '../../core/services/report.service';
import { OrganizationService } from '../../core/services/organization.service';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';
import { SalaryReportRow } from '../../core/models/salary.model';
import { Department, Country, Currency } from '../../core/models/organization.model';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatTableModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './reports.component.html',
  styleUrls: ['./reports.component.scss']
})
export class ReportsComponent implements OnInit {
  private reportService = inject(ReportService);
  private orgService = inject(OrganizationService);
  authService = inject(AuthService);
  private notification = inject(NotificationService);
  private fb = inject(FormBuilder);

  displayedColumns: string[] = [
    'employeeCode',
    'employeeName',
    'department',
    'country',
    'jobTitle',
    'salaryAmount',
    'payFrequency',
    'effectiveFrom',
    'status'
  ];

  reports = signal<SalaryReportRow[]>([]);
  totalElements = signal<number>(0);
  pageSize = signal<number>(20);
  pageIndex = signal<number>(0);
  isLoading = signal<boolean>(false);
  isExporting = signal<boolean>(false);

  departments = signal<Department[]>([]);
  countries = signal<Country[]>([]);
  currencies = signal<Currency[]>([]);

  filterForm: FormGroup = this.fb.group({
    departmentId: [null],
    countryId: [null],
    currency: [null],
    minSalary: [null],
    maxSalary: [null],
    status: ['ACTIVE']
  });

  ngOnInit(): void {
    this.loadReferenceData();
    this.loadReport();
  }

  private loadReferenceData(): void {
    this.orgService.getDepartments().subscribe(res => {
      if (res.success) this.departments.set(res.data);
    });

    this.orgService.getCountries().subscribe(res => {
      if (res.success) this.countries.set(res.data);
    });

    this.orgService.getCurrencies().subscribe(res => {
      if (res.success) this.currencies.set(res.data);
    });
  }

  loadReport(): void {
    this.isLoading.set(true);
    const formVals = this.filterForm.value;

    const params: ReportFilterParams = {
      departmentId: formVals.departmentId || undefined,
      countryId: formVals.countryId || undefined,
      currency: formVals.currency || undefined,
      minSalary: formVals.minSalary || undefined,
      maxSalary: formVals.maxSalary || undefined,
      status: formVals.status || undefined,
      page: this.pageIndex(),
      size: this.pageSize()
    };

    this.reportService.getSalaryReport(params).subscribe({
      next: (res) => {
        this.isLoading.set(false);
        if (res.success && res.data) {
          this.reports.set(res.data.content);
          this.totalElements.set(res.data.totalElements);
        }
      },
      error: () => {
        this.isLoading.set(false);
        this.notification.error('Failed to load salary report data');
      }
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.loadReport();
  }

  applyFilters(): void {
    this.pageIndex.set(0);
    this.loadReport();
  }

  resetFilters(): void {
    this.filterForm.reset({
      departmentId: null,
      countryId: null,
      currency: null,
      minSalary: null,
      maxSalary: null,
      status: 'ACTIVE'
    });
    this.pageIndex.set(0);
    this.loadReport();
  }

  exportCsv(): void {
    this.isExporting.set(true);
    const formVals = this.filterForm.value;

    const params: ReportFilterParams = {
      departmentId: formVals.departmentId || undefined,
      countryId: formVals.countryId || undefined,
      currency: formVals.currency || undefined,
      minSalary: formVals.minSalary || undefined,
      maxSalary: formVals.maxSalary || undefined,
      status: formVals.status || undefined
    };

    this.reportService.downloadCsv(params).subscribe({
      next: (blob) => {
        this.isExporting.set(false);
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        const timestamp = new Date().toISOString().substring(0, 10);
        a.download = `acme_salary_report_${timestamp}.csv`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
        this.notification.success('CSV export downloaded successfully');
      },
      error: () => {
        this.isExporting.set(false);
        this.notification.error('Failed to generate CSV export');
      }
    });
  }
}
