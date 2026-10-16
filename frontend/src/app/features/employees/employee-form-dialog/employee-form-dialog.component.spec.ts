import { describe, it, expect, vi, beforeEach } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { of, throwError } from 'rxjs';
import { EmployeeFormDialogComponent, EmployeeFormDialogData } from './employee-form-dialog.component';
import { EmployeeService } from '../../../core/services/employee.service';
import { OrganizationService } from '../../../core/services/organization.service';
import { NotificationService } from '../../../core/services/notification.service';

describe('EmployeeFormDialogComponent', () => {
  let component: EmployeeFormDialogComponent;
  let fixture: ComponentFixture<EmployeeFormDialogComponent>;
  let employeeService: any;
  let orgService: any;
  let notificationService: any;
  let dialogRef: any;

  const mockEmployeeData: EmployeeFormDialogData = {
    isEdit: true,
    employee: {
      id: 1,
      employeeCode: 'EMP-00001',
      firstName: 'John',
      lastName: 'Doe',
      email: 'john.doe@acme.corp',
      phoneNumber: '+1-555-0100',
      departmentId: 1,
      departmentName: 'Engineering',
      countryId: 1,
      countryName: 'United States',
      jobTitleId: 1,
      jobTitle: 'Software Engineer',
      status: 'ACTIVE',
      hireDate: '2023-01-10'
    }
  };

  beforeEach(async () => {
    employeeService = {
      createEmployee: vi.fn().mockReturnValue(of({ success: true, data: {} })),
      updateEmployee: vi.fn().mockReturnValue(of({ success: true, data: {} }))
    };

    orgService = {
      getDepartments: vi.fn().mockReturnValue(of({
        success: true,
        data: [{ id: 1, code: 'ENG', name: 'Engineering' }]
      })),
      getCountries: vi.fn().mockReturnValue(of({
        success: true,
        data: [{ id: 1, code: 'USA', name: 'United States' }]
      })),
      getJobTitles: vi.fn().mockReturnValue(of({
        success: true,
        data: [{ id: 1, title: 'Software Engineer', departmentId: 1 }]
      }))
    };

    notificationService = {
      success: vi.fn(),
      error: vi.fn()
    };

    dialogRef = {
      close: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [EmployeeFormDialogComponent],
      providers: [
        provideAnimationsAsync(),
        { provide: EmployeeService, useValue: employeeService },
        { provide: OrganizationService, useValue: orgService },
        { provide: NotificationService, useValue: notificationService },
        { provide: MatDialogRef, useValue: dialogRef },
        { provide: MAT_DIALOG_DATA, useValue: mockEmployeeData }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(EmployeeFormDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should initialize form with existing employee values in edit mode', () => {
    expect(component).toBeTruthy();
    expect(component.employeeForm.get('firstName')?.value).toBe('John');
    expect(component.employeeForm.get('lastName')?.value).toBe('Doe');
    expect(component.employeeForm.get('email')?.value).toBe('john.doe@acme.corp');
    expect(component.employeeForm.get('phoneNumber')?.value).toBe('+1-555-0100');
    expect(component.employeeForm.get('departmentId')?.value).toBe(1);
    expect(component.employeeForm.get('countryId')?.value).toBe(1);
    expect(component.employeeForm.get('jobTitleId')?.value).toBe(1);
    expect(component.employeeForm.get('hireDate')?.value).toBe('2023-01-10');
    expect(component.employeeForm.get('hireDate')?.disabled).toBe(true);
    expect(component.employeeForm.get('employeeCode')?.disabled).toBe(true);
  });

  it('should include hireDate and numeric IDs in updatePayload when updating profile', () => {
    component.employeeForm.patchValue({
      firstName: 'Johnny',
      lastName: 'Doeman'
    });

    component.onSubmit();

    expect(employeeService.updateEmployee).toHaveBeenCalledTimes(1);
    expect(employeeService.updateEmployee).toHaveBeenCalledWith(
      1,
      expect.objectContaining({
        firstName: 'Johnny',
        lastName: 'Doeman',
        email: 'john.doe@acme.corp',
        departmentId: 1,
        countryId: 1,
        jobTitleId: 1,
        hireDate: '2023-01-10'
      })
    );
    expect(dialogRef.close).toHaveBeenCalled();
  });

  it('should display field errors when backend validation fails', () => {
    employeeService.updateEmployee.mockReturnValue(throwError(() => ({
      error: {
        message: 'Validation failed for one or more fields',
        fieldErrors: [
          { field: 'email', message: 'Email must be valid' }
        ]
      }
    })));

    component.onSubmit();

    expect(component.errorMessage()).toContain('Validation failed');
    expect(component.errorMessage()).toContain('email: Email must be valid');
    expect(dialogRef.close).not.toHaveBeenCalled();
  });
});
