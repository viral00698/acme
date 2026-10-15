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
import { OrganizationService } from '../../../core/services/organization.service';
import { EmployeeService } from '../../../core/services/employee.service';
import { NotificationService } from '../../../core/services/notification.service';
import { Department, Country, JobTitle } from '../../../core/models/organization.model';
import { EmployeeDetail, EmployeeSummary } from '../../../core/models/employee.model';

export interface EmployeeFormDialogData {
  isEdit: boolean;
  employee?: EmployeeDetail | EmployeeSummary;
}

@Component({
  selector: 'app-employee-form-dialog',
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
  templateUrl: './employee-form-dialog.component.html',
  styleUrls: ['./employee-form-dialog.component.scss']
})
export class EmployeeFormDialogComponent implements OnInit {
  private fb = inject(FormBuilder);
  private orgService = inject(OrganizationService);
  private employeeService = inject(EmployeeService);
  private notification = inject(NotificationService);
  dialogRef = inject(MatDialogRef<EmployeeFormDialogComponent>);
  data: EmployeeFormDialogData = inject(MAT_DIALOG_DATA);

  departments = signal<Department[]>([]);
  countries = signal<Country[]>([]);
  jobTitles = signal<JobTitle[]>([]);
  isLoading = signal<boolean>(false);
  errorMessage = signal<string | null>(null);

  employeeForm!: FormGroup;

  ngOnInit(): void {
    this.initForm();
    this.loadReferenceData();
  }

  private initForm(): void {
    const isEdit = this.data.isEdit;
    const emp = this.data.employee;

    const deptId = emp ? ('department' in emp && typeof emp.department === 'object' ? emp.department.id : (emp as any).departmentId) : '';
    const countryId = emp ? ('country' in emp && typeof emp.country === 'object' ? emp.country.id : (emp as any).countryId) : '';
    const jobTitleId = emp ? ('jobTitle' in emp && typeof emp.jobTitle === 'object' ? emp.jobTitle.id : (emp as any).jobTitleId) : '';

    this.employeeForm = this.fb.group({
      employeeCode: [
        { value: emp?.employeeCode || '', disabled: isEdit },
        [Validators.required, Validators.pattern(/^[A-Za-z0-9_-]{3,20}$/)]
      ],
      firstName: [emp?.firstName || '', [Validators.required, Validators.maxLength(50)]],
      lastName: [emp?.lastName || '', [Validators.required, Validators.maxLength(50)]],
      email: [emp?.email || '', [Validators.required, Validators.email, Validators.maxLength(100)]],
      departmentId: [deptId, [Validators.required]],
      countryId: [countryId, [Validators.required]],
      jobTitleId: [jobTitleId, [Validators.required]],
      hireDate: [
        { value: emp?.hireDate || new Date().toISOString().substring(0, 10), disabled: isEdit },
        [Validators.required]
      ]
    });

    if (deptId) {
      this.loadJobTitlesForDept(Number(deptId));
    }
  }

  private loadReferenceData(): void {
    this.orgService.getDepartments().subscribe(res => {
      if (res.success) this.departments.set(res.data);
    });

    this.orgService.getCountries().subscribe(res => {
      if (res.success) this.countries.set(res.data);
    });
  }

  onDepartmentChange(deptId: number): void {
    this.employeeForm.patchValue({ jobTitleId: '' });
    this.loadJobTitlesForDept(deptId);
  }

  private loadJobTitlesForDept(deptId: number): void {
    this.orgService.getJobTitles(deptId).subscribe(res => {
      if (res.success) {
        this.jobTitles.set(res.data);
      }
    });
  }

  onSubmit(): void {
    if (this.employeeForm.invalid) {
      this.employeeForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    const formValues = this.employeeForm.getRawValue();

    if (this.data.isEdit && this.data.employee) {
      const updatePayload = {
        firstName: formValues.firstName,
        lastName: formValues.lastName,
        email: formValues.email,
        departmentId: formValues.departmentId,
        countryId: formValues.countryId,
        jobTitleId: formValues.jobTitleId
      };

      this.employeeService.updateEmployee(this.data.employee.id, updatePayload).subscribe({
        next: (res) => {
          this.isLoading.set(false);
          this.notification.success('Employee updated successfully');
          this.dialogRef.close(res.data);
        },
        error: (err) => {
          this.isLoading.set(false);
          this.errorMessage.set(err.error?.message || 'Failed to update employee');
        }
      });
    } else {
      const createPayload = {
        employeeCode: formValues.employeeCode,
        firstName: formValues.firstName,
        lastName: formValues.lastName,
        email: formValues.email,
        departmentId: formValues.departmentId,
        countryId: formValues.countryId,
        jobTitleId: formValues.jobTitleId,
        hireDate: formValues.hireDate
      };

      this.employeeService.createEmployee(createPayload).subscribe({
        next: (res) => {
          this.isLoading.set(false);
          this.notification.success('Employee created successfully');
          this.dialogRef.close(res.data);
        },
        error: (err) => {
          this.isLoading.set(false);
          this.errorMessage.set(err.error?.message || 'Failed to create employee');
        }
      });
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
