import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { EmployeeService } from '../../../core/services/employee.service';
import { SalaryService } from '../../../core/services/salary.service';
import { AuditService } from '../../../core/services/audit.service';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { EmployeeDetail } from '../../../core/models/employee.model';
import { SalaryRecord } from '../../../core/models/salary.model';
import { AuditLog } from '../../../core/models/audit.model';
import { EmployeeFormDialogComponent } from '../employee-form-dialog/employee-form-dialog.component';
import { StatusDialogComponent } from '../status-dialog/status-dialog.component';
import { SalaryDialogComponent } from '../salary-dialog/salary-dialog.component';

@Component({
  selector: 'app-employee-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatTableModule,
    MatProgressSpinnerModule,
    MatDialogModule
  ],
  templateUrl: './employee-detail.component.html',
  styleUrls: ['./employee-detail.component.scss']
})
export class EmployeeDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private employeeService = inject(EmployeeService);
  private salaryService = inject(SalaryService);
  private auditService = inject(AuditService);
  authService = inject(AuthService);
  private notification = inject(NotificationService);
  private dialog = inject(MatDialog);

  employeeId!: number;
  employee = signal<EmployeeDetail | null>(null);
  salaryHistory = signal<SalaryRecord[]>([]);
  salaryAudits = signal<AuditLog[]>([]);
  isLoading = signal<boolean>(true);
  isLoadingHistory = signal<boolean>(false);
  isLoadingAudits = signal<boolean>(false);

  historyColumns: string[] = [
    'effectivePeriod',
    'amount',
    'frequency',
    'status',
    'reason',
    'modifiedBy',
    'createdAt'
  ];

  auditColumns: string[] = [
    'action',
    'actor',
    'ipAddress',
    'details',
    'createdAt'
  ];

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.employeeId = Number(idParam);
      this.loadEmployee();
      this.loadSalaryHistory();
      if (this.authService.isAdmin()) {
        this.loadSalaryAudits();
      }
    } else {
      this.router.navigate(['/employees']);
    }
  }

  loadEmployee(): void {
    this.isLoading.set(true);
    this.employeeService.getEmployee(this.employeeId).subscribe({
      next: (res) => {
        this.isLoading.set(false);
        if (res.success && res.data) {
          const raw = res.data;
          const deptName = raw.departmentName || (typeof raw.department === 'object' ? raw.department?.name : '') || '';
          const countryName = raw.countryName || (typeof raw.country === 'object' ? raw.country?.name : '') || '';
          const countryCode = raw.countryCode || (typeof raw.country === 'object' && raw.country ? ((raw.country as any).isoCode || (raw.country as any).code) : '') || '';
          const jobTitleStr = typeof raw.jobTitle === 'string' ? raw.jobTitle : ((raw.jobTitle as any)?.title || '');

          const normalized: EmployeeDetail = {
            ...raw,
            departmentName: deptName,
            countryName: countryName,
            countryCode: countryCode,
            jobTitle: jobTitleStr,
            department: {
              id: raw.departmentId || (typeof raw.department === 'object' ? raw.department?.id : undefined),
              name: deptName,
              code: countryCode,
              departmentCode: (typeof raw.department === 'object' ? (raw.department as any)?.departmentCode : '') || deptName.substring(0, 3).toUpperCase()
            },
            country: {
              id: raw.countryId || (typeof raw.country === 'object' ? raw.country?.id : undefined),
              name: countryName,
              code: countryCode,
              isoCode: countryCode
            },
            currentSalary: raw.currentSalary ? raw.currentSalary : (raw.currentSalaryAmount != null ? {
              id: 0,
              amount: raw.currentSalaryAmount,
              currencyCode: raw.currentSalaryCurrency || 'USD',
              currencySymbol: raw.currentSalarySymbol || '$',
              payFrequency: raw.currentPayFrequency || 'ANNUAL',
              effectiveFrom: raw.currentSalaryEffectiveFrom || '',
              effectiveTo: undefined
            } : undefined)
          };
          this.employee.set(normalized);
        }
      },
      error: () => {
        this.isLoading.set(false);
        this.notification.error('Employee record not found');
        this.router.navigate(['/employees']);
      }
    });
  }

  loadSalaryHistory(): void {
    this.isLoadingHistory.set(true);
    this.salaryService.getSalaryHistory(this.employeeId).subscribe({
      next: (res) => {
        this.isLoadingHistory.set(false);
        if (res.success && res.data) {
          const historyList: SalaryRecord[] = res.data.history || res.data.records || [];
          this.salaryHistory.set(historyList);

          // If current salary returned from salary history endpoint, sync into employee signal
          if (res.data.currentSalary) {
            const current = this.employee();
            if (current) {
              const cs = res.data.currentSalary;
              this.employee.set({
                ...current,
                currentSalaryAmount: cs.amount,
                currentSalaryCurrency: cs.currencyCode,
                currentSalarySymbol: cs.currencySymbol,
                currentPayFrequency: cs.payFrequency,
                currentSalaryEffectiveFrom: cs.effectiveFrom,
                currentSalary: {
                  id: cs.id,
                  amount: cs.amount,
                  currencyCode: cs.currencyCode,
                  currencySymbol: cs.currencySymbol,
                  payFrequency: cs.payFrequency,
                  effectiveFrom: cs.effectiveFrom,
                  effectiveTo: cs.effectiveTo,
                  changeReason: cs.changeReason,
                  notes: cs.notes
                }
              });
            }
          }
        }
      },
      error: () => {
        this.isLoadingHistory.set(false);
      }
    });
  }

  loadSalaryAudits(): void {
    if (!this.authService.isAdmin()) return;
    this.isLoadingAudits.set(true);
    this.auditService.getAuditLogs({
      entityName: 'SALARY_RECORD',
      entityId: this.employeeId,
      size: 50
    }).subscribe({
      next: (res) => {
        this.isLoadingAudits.set(false);
        if (res.success && res.data) {
          this.salaryAudits.set(res.data.content);
        }
      },
      error: () => {
        this.isLoadingAudits.set(false);
      }
    });
  }

  getDepartment(emp: EmployeeDetail | null): string {
    if (!emp) return '';
    return emp.departmentName || (emp.department as any)?.name || 'N/A';
  }

  getDepartmentCode(emp: EmployeeDetail | null): string {
    if (!emp) return '';
    return (emp.department as any)?.departmentCode || (emp.department as any)?.code || (emp.departmentName ? emp.departmentName.substring(0, 3).toUpperCase() : '');
  }

  getCountry(emp: EmployeeDetail | null): string {
    if (!emp) return '';
    const name = emp.countryName || (emp.country as any)?.name || 'N/A';
    const code = emp.countryCode || (emp.country as any)?.code || (emp.country as any)?.isoCode || '';
    return code ? `${name} (${code})` : name;
  }

  getJobTitle(emp: EmployeeDetail | null): string {
    if (!emp) return '';
    if (typeof emp.jobTitle === 'string') return emp.jobTitle;
    return (emp.jobTitle as any)?.title || 'N/A';
  }

  getCurrentSalary(emp: EmployeeDetail | null) {
    if (!emp) return null;
    if (emp.currentSalary) return emp.currentSalary;
    if (emp.currentSalaryAmount != null) {
      return {
        amount: emp.currentSalaryAmount,
        currencyCode: emp.currentSalaryCurrency || 'USD',
        currencySymbol: emp.currentSalarySymbol || '$',
        payFrequency: emp.currentPayFrequency || 'ANNUAL',
        effectiveFrom: emp.currentSalaryEffectiveFrom || '',
        effectiveTo: undefined,
        changeReason: undefined,
        notes: undefined
      };
    }
    return null;
  }

  openReviseSalaryDialog(): void {
    const current = this.employee();
    if (!current) return;

    const currSalary = this.getCurrentSalary(current);

    const dialogRef = this.dialog.open(SalaryDialogComponent, {
      width: '580px',
      data: {
        employeeId: current.id,
        employeeName: `${current.firstName} ${current.lastName}`,
        currentSalary: currSalary ? {
          amount: currSalary.amount,
          currencyCode: currSalary.currencyCode,
          payFrequency: currSalary.payFrequency
        } : undefined
      }
    });

    dialogRef.afterClosed().subscribe(res => {
      if (res) {
        this.loadEmployee();
        this.loadSalaryHistory();
        if (this.authService.isAdmin()) {
          this.loadSalaryAudits();
        }
      }
    });
  }

  editProfile(): void {
    const current = this.employee();
    if (!current) return;

    const dialogRef = this.dialog.open(EmployeeFormDialogComponent, {
      width: '600px',
      data: { isEdit: true, employee: current }
    });

    dialogRef.afterClosed().subscribe(res => {
      if (res) {
        this.loadEmployee();
      }
    });
  }

  changeStatus(): void {
    const current = this.employee();
    if (!current) return;

    const dialogRef = this.dialog.open(StatusDialogComponent, {
      width: '450px',
      data: {
        employeeId: current.id,
        employeeName: `${current.firstName} ${current.lastName}`,
        currentStatus: current.status
      }
    });

    dialogRef.afterClosed().subscribe(res => {
      if (res) {
        this.employeeService.updateStatus(current.id, res).subscribe({
          next: () => {
            this.notification.success('Status updated successfully');
            this.loadEmployee();
          },
          error: (err) => {
            this.notification.error(err.error?.message || 'Failed to update status');
          }
        });
      }
    });
  }
}
