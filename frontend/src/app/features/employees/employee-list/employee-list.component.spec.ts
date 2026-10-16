import { describe, it, expect, vi, beforeEach } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { EmployeeListComponent } from './employee-list.component';
import { EmployeeService } from '../../../core/services/employee.service';
import { OrganizationService } from '../../../core/services/organization.service';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { MatDialog } from '@angular/material/dialog';

describe('EmployeeListComponent', () => {
  let component: EmployeeListComponent;
  let fixture: ComponentFixture<EmployeeListComponent>;
  let employeeService: any;
  let orgService: any;
  let authService: any;
  let dialog: any;

  beforeEach(async () => {
    employeeService = {
      getEmployees: vi.fn().mockReturnValue(of({
        success: true,
        data: {
          content: [
            {
              id: 1,
              employeeCode: 'EMP-00001',
              firstName: 'John',
              lastName: 'Doe',
              email: 'john.doe@acme.corp',
              departmentName: 'Engineering',
              countryName: 'United States',
              jobTitle: 'Software Engineer',
              status: 'ACTIVE',
              hireDate: '2023-01-10'
            }
          ],
          totalElements: 1,
          pageNumber: 0,
          pageSize: 20,
          totalPages: 1
        }
      })),
      updateStatus: vi.fn().mockReturnValue(of({ success: true }))
    };

    orgService = {
      getDepartments: vi.fn().mockReturnValue(of({
        success: true,
        data: [
          { id: 1, code: 'ENG', name: 'Engineering' },
          { id: 2, code: 'HR', name: 'Human Resources' }
        ]
      })),
      getCountries: vi.fn().mockReturnValue(of({
        success: true,
        data: [
          { id: 1, code: 'USA', name: 'United States' },
          { id: 2, code: 'GBR', name: 'United Kingdom' }
        ]
      }))
    };

    authService = {
      isManager: vi.fn().mockReturnValue(true)
    };

    dialog = {
      open: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [EmployeeListComponent],
      providers: [
        provideAnimationsAsync(),
        provideRouter([]),
        { provide: EmployeeService, useValue: employeeService },
        { provide: OrganizationService, useValue: orgService },
        { provide: AuthService, useValue: authService },
        { provide: NotificationService, useValue: { success: vi.fn(), error: vi.fn() } },
        { provide: MatDialog, useValue: dialog }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(EmployeeListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('should create and load initial employee list', () => {
    expect(component).toBeTruthy();
    expect(employeeService.getEmployees).toHaveBeenCalled();
    expect(component.employees().length).toBe(1);
    expect(component.departments().length).toBe(2);
    expect(component.countries().length).toBe(2);
  });

  it('should reload when department and then country filters are selected consecutively with same id', async () => {
    employeeService.getEmployees.mockClear();

    // 1. Select Department ID 1
    component.filterForm.get('departmentId')?.setValue(1);
    await Promise.resolve();

    expect(employeeService.getEmployees).toHaveBeenCalledTimes(1);
    expect(employeeService.getEmployees).toHaveBeenLastCalledWith(
      expect.objectContaining({ departmentId: 1, countryId: undefined })
    );

    // 2. Select Country ID 1 (same numeric ID as department)
    component.filterForm.get('countryId')?.setValue(1);
    await Promise.resolve();

    // With the bug, distinctUntilChanged dropped this emission because 1 === 1!
    expect(employeeService.getEmployees).toHaveBeenCalledTimes(2);
    expect(employeeService.getEmployees).toHaveBeenLastCalledWith(
      expect.objectContaining({ departmentId: 1, countryId: 1 })
    );
  });

  it('should reload when country is selected first and then department', async () => {
    employeeService.getEmployees.mockClear();

    // 1. Select Country ID 2
    component.filterForm.get('countryId')?.setValue(2);
    await Promise.resolve();

    expect(employeeService.getEmployees).toHaveBeenCalledTimes(1);
    expect(employeeService.getEmployees).toHaveBeenLastCalledWith(
      expect.objectContaining({ departmentId: undefined, countryId: 2 })
    );

    // 2. Select Department ID 2
    component.filterForm.get('departmentId')?.setValue(2);
    await Promise.resolve();

    expect(employeeService.getEmployees).toHaveBeenCalledTimes(2);
    expect(employeeService.getEmployees).toHaveBeenLastCalledWith(
      expect.objectContaining({ departmentId: 2, countryId: 2 })
    );
  });

  it('should reset all filters and reload when clearFilters is called', async () => {
    component.filterForm.patchValue({
      search: 'Alice',
      departmentId: 1,
      countryId: 1,
      status: 'ACTIVE'
    });
    await Promise.resolve();
    employeeService.getEmployees.mockClear();

    component.clearFilters();
    await Promise.resolve();

    expect(component.filterForm.value).toEqual({
      search: '',
      departmentId: null,
      countryId: null,
      status: null
    });
    expect(employeeService.getEmployees).toHaveBeenCalledTimes(1);
    expect(employeeService.getEmployees).toHaveBeenLastCalledWith(
      expect.objectContaining({
        search: undefined,
        departmentId: undefined,
        countryId: undefined,
        status: undefined
      })
    );
  });

  it('should update sorting and trigger reload on sort change', async () => {
    employeeService.getEmployees.mockClear();

    component.onSortChange({ active: 'departmentName', direction: 'desc' });
    await Promise.resolve();

    expect(employeeService.getEmployees).toHaveBeenCalledTimes(1);
    expect(employeeService.getEmployees).toHaveBeenLastCalledWith(
      expect.objectContaining({
        sortBy: 'departmentName',
        sortDir: 'desc'
      })
    );
  });
});
