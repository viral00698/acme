import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTableModule } from '@angular/material/table';
import { AnalyticsService } from '../../core/services/analytics.service';
import {
  DashboardOverview,
  CountryAnalytics,
  DepartmentAnalytics,
  SalaryBand
} from '../../core/models/analytics.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatSelectModule,
    MatFormFieldModule,
    MatProgressSpinnerModule,
    MatProgressBarModule,
    MatTabsModule,
    MatTableModule
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  private analyticsService = inject(AnalyticsService);

  isLoading = signal<boolean>(true);
  overview = signal<DashboardOverview | null>(null);
  countries = signal<CountryAnalytics[]>([]);
  departments = signal<DepartmentAnalytics[]>([]);
  salaryBands = signal<SalaryBand[]>([]);
  selectedCurrency = signal<string>('USD');

  departmentColumns: string[] = ['name', 'employees'];
  countryColumns: string[] = ['code', 'name', 'employees'];

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.isLoading.set(true);

    this.analyticsService.getOverview().subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.overview.set(res.data);
          if (res.data.currencySummaries && res.data.currencySummaries.length > 0) {
            this.selectedCurrency.set(res.data.currencySummaries[0].currencyCode);
            this.loadDistribution(this.selectedCurrency());
          }
        }
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });

    this.analyticsService.getByDepartment().subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.departments.set(res.data);
        }
      }
    });

    this.analyticsService.getByCountry().subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.countries.set(res.data);
        }
      }
    });
  }

  onCurrencyChange(curr: string): void {
    this.selectedCurrency.set(curr);
    this.loadDistribution(curr);
  }

  loadDistribution(curr: string): void {
    this.analyticsService.getSalaryDistribution(curr).subscribe({
      next: (res) => {
        if (res.success && res.data) {
          this.salaryBands.set(res.data);
        }
      }
    });
  }

  getMaxBandCount(): number {
    const bands = this.salaryBands();
    if (!bands.length) return 1;
    const max = Math.max(...bands.map(b => b.employeeCount));
    return max > 0 ? max : 1;
  }

  getBandPercentage(count: number): number {
    return Math.round((count / this.getMaxBandCount()) * 100);
  }
}
