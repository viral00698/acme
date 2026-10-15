import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { SalaryService } from '../../../core/services/salary.service';
import { OrganizationService } from '../../../core/services/organization.service';
import { NotificationService } from '../../../core/services/notification.service';
import { Currency } from '../../../core/models/organization.model';

export interface SalaryDialogData {
  employeeId: number;
  employeeName: string;
  currentSalary?: {
    amount: number;
    currencyCode: string;
    payFrequency: string;
  };
}

@Component({
  selector: 'app-salary-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './salary-dialog.component.html',
  styleUrls: ['./salary-dialog.component.scss']
})
export class SalaryDialogComponent implements OnInit {
  private fb = inject(FormBuilder);
  private salaryService = inject(SalaryService);
  private orgService = inject(OrganizationService);
  private notification = inject(NotificationService);
  dialogRef = inject(MatDialogRef<SalaryDialogComponent>);
  data: SalaryDialogData = inject(MAT_DIALOG_DATA);

  currencies = signal<Currency[]>([]);
  isLoading = signal<boolean>(false);
  errorMessage = signal<string | null>(null);

  reasons: string[] = [
    'Annual Merit Increase',
    'Promotion / Title Upgrade',
    'Market Adjustment',
    'Initial Compensation',
    'Role Restructure',
    'Other'
  ];

  salaryForm!: FormGroup;

  ngOnInit(): void {
    this.initForm();
    this.loadCurrencies();
  }

  private initForm(): void {
    const today = new Date().toISOString().substring(0, 10);
    const curr = this.data.currentSalary;

    this.salaryForm = this.fb.group({
      amount: [curr?.amount || '', [Validators.required, Validators.min(0.01)]],
      currencyCode: [curr?.currencyCode || 'USD', [Validators.required]],
      payFrequency: [curr?.payFrequency || 'ANNUAL', [Validators.required]],
      effectiveFrom: [today, [Validators.required]],
      changeReason: ['Annual Merit Increase', [Validators.required]],
      notes: ['']
    });
  }

  private loadCurrencies(): void {
    this.orgService.getCurrencies().subscribe(res => {
      if (res.success) {
        this.currencies.set(res.data);
      }
    });
  }

  onSubmit(): void {
    if (this.salaryForm.invalid) {
      this.salaryForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    const payload = this.salaryForm.value;

    this.salaryService.createSalaryRevision(this.data.employeeId, payload).subscribe({
      next: (res) => {
        this.isLoading.set(false);
        this.notification.success('Salary revision applied successfully');
        this.dialogRef.close(res.data);
      },
      error: (err) => {
        this.isLoading.set(false);
        if (err.status === 409) {
          this.errorMessage.set(err.error?.message || 'Effective date overlaps with existing salary record');
        } else {
          this.errorMessage.set(err.error?.message || 'Failed to apply salary revision');
        }
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
