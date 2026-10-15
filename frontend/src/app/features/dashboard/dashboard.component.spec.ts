import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { of } from 'rxjs';
import { DashboardComponent } from './dashboard.component';
import { AnalyticsService } from '../../core/services/analytics.service';
import { DashboardOverview } from '../../core/models/analytics.model';

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let analyticsService: any;

  const mockOverview: DashboardOverview = {
    totalActiveEmployees: 10000,
    totalDepartments: 6,
    totalCountries: 8,
    activeCurrenciesCount: 5,
    currencySummaries: [
      {
        currencyCode: 'USD',
        currencySymbol: '$',
        employeeCount: 4000,
        minSalary: 45000,
        maxSalary: 210000,
        avgSalary: 112000,
        totalAnnualSpend: 448000000
      }
    ]
  };

  beforeEach(async () => {
    analyticsService = {
      getOverview: vi.fn().mockReturnValue(of({ success: true, data: mockOverview })),
      getByDepartment: vi.fn().mockReturnValue(of({ success: true, data: [] })),
      getByCountry: vi.fn().mockReturnValue(of({ success: true, data: [] })),
      getSalaryDistribution: vi.fn().mockReturnValue(of({
        success: true,
        data: [
          { bandName: '50k - 100k', minAmount: 50000, maxAmount: 100000, currencyCode: 'USD', employeeCount: 1500 }
        ]
      }))
    };

    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [
        provideAnimationsAsync(),
        { provide: AnalyticsService, useValue: analyticsService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create dashboard and load live compensation metrics', () => {
    expect(component).toBeTruthy();
    expect(analyticsService.getOverview).toHaveBeenCalled();
    expect(component.overview()?.totalActiveEmployees).toBe(10000);
    expect(component.selectedCurrency()).toBe('USD');
  });

  it('should reload distribution when currency is changed', () => {
    component.onCurrencyChange('EUR');

    expect(component.selectedCurrency()).toBe('EUR');
    expect(analyticsService.getSalaryDistribution).toHaveBeenCalledWith('EUR');
  });
});
