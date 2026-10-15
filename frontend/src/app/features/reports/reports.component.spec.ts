import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { of } from 'rxjs';
import { ReportsComponent } from './reports.component';
import { ReportService } from '../../core/services/report.service';
import { OrganizationService } from '../../core/services/organization.service';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';

describe('ReportsComponent', () => {
  let component: ReportsComponent;
  let fixture: ComponentFixture<ReportsComponent>;
  let reportService: any;
  let orgService: any;
  let authService: any;

  beforeEach(async () => {
    reportService = {
      getSalaryReport: vi.fn().mockReturnValue(of({
        success: true,
        data: {
          content: [
            {
              employeeCode: 'EMP-001',
              employeeName: 'John Doe',
              department: 'Engineering',
              country: 'United States',
              jobTitle: 'Lead Architect',
              salaryAmount: 145000,
              currency: 'USD',
              payFrequency: 'ANNUAL',
              effectiveFrom: '2024-01-01',
              status: 'ACTIVE'
            }
          ],
          totalElements: 1,
          pageNumber: 0,
          pageSize: 20,
          totalPages: 1
        }
      })),
      downloadCsv: vi.fn().mockReturnValue(of(new Blob(['mock,csv,data'], { type: 'text/csv' })))
    };

    orgService = {
      getDepartments: vi.fn().mockReturnValue(of({ success: true, data: [] })),
      getCountries: vi.fn().mockReturnValue(of({ success: true, data: [] })),
      getCurrencies: vi.fn().mockReturnValue(of({ success: true, data: [] }))
    };

    authService = {
      isManager: vi.fn().mockReturnValue(true)
    };

    await TestBed.configureTestingModule({
      imports: [ReportsComponent],
      providers: [
        provideAnimationsAsync(),
        { provide: ReportService, useValue: reportService },
        { provide: OrganizationService, useValue: orgService },
        { provide: AuthService, useValue: authService },
        { provide: NotificationService, useValue: { success: vi.fn(), error: vi.fn() } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ReportsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create and load initial salary report', () => {
    expect(component).toBeTruthy();
    expect(reportService.getSalaryReport).toHaveBeenCalled();
    expect(component.reports().length).toBe(1);
    expect(component.reports()[0].employeeCode).toBe('EMP-001');
  });

  it('should reset filters to default state', () => {
    component.filterForm.patchValue({
      currency: 'USD',
      minSalary: 50000
    });

    component.resetFilters();

    expect(component.filterForm.get('currency')?.value).toBeNull();
    expect(component.filterForm.get('minSalary')?.value).toBeNull();
    expect(component.filterForm.get('status')?.value).toBe('ACTIVE');
  });

  it('should call downloadCsv when export is triggered', () => {
    component.exportCsv();
    expect(reportService.downloadCsv).toHaveBeenCalled();
  });
});
