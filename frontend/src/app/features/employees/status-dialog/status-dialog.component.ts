import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { EmploymentStatus } from '../../../core/models/employee.model';

export interface StatusDialogData {
  employeeId: number;
  employeeName: string;
  currentStatus: EmploymentStatus;
}

@Component({
  selector: 'app-status-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatButtonModule
  ],
  template: `
    <h2 mat-dialog-title>Update Employment Status</h2>
    <mat-dialog-content>
      <p class="dialog-desc">Updating status for <strong>{{ data.employeeName }}</strong></p>

      <form [formGroup]="statusForm">
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Employment Status</mat-label>
          <mat-select formControlName="status">
            <mat-option value="ACTIVE">ACTIVE</mat-option>
            <mat-option value="INACTIVE">INACTIVE</mat-option>
            <mat-option value="ON_LEAVE">ON_LEAVE</mat-option>
            <mat-option value="TERMINATED">TERMINATED</mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Status Change Reason (Optional)</mat-label>
          <textarea matInput formControlName="reason" rows="3" placeholder="Reason for status change..."></textarea>
        </mat-form-field>
      </form>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Cancel</button>
      <button mat-flat-button color="primary" [disabled]="statusForm.invalid" (click)="onSave()">Update Status</button>
    </mat-dialog-actions>
  `,
  styles: [`
    .full-width { width: 100%; margin-top: 8px; }
    .dialog-desc { color: #64748b; font-size: 0.9rem; margin-bottom: 16px; }
  `]
})
export class StatusDialogComponent {
  private fb = inject(FormBuilder);
  dialogRef = inject(MatDialogRef<StatusDialogComponent>);
  data: StatusDialogData = inject(MAT_DIALOG_DATA);

  statusForm: FormGroup = this.fb.group({
    status: [this.data.currentStatus, [Validators.required]],
    reason: ['']
  });

  onCancel(): void {
    this.dialogRef.close();
  }

  onSave(): void {
    if (this.statusForm.valid) {
      this.dialogRef.close(this.statusForm.value);
    }
  }
}
