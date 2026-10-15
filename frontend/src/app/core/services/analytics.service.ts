import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import {
  DashboardOverview,
  DepartmentAnalytics,
  CountryAnalytics,
  SalaryBand
} from '../models/analytics.model';

@Injectable({
  providedIn: 'root'
})
export class AnalyticsService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/analytics`;

  getOverview(): Observable<ApiResponse<DashboardOverview>> {
    return this.http.get<ApiResponse<DashboardOverview>>(`${this.baseUrl}/overview`);
  }

  getByCountry(): Observable<ApiResponse<CountryAnalytics[]>> {
    return this.http.get<ApiResponse<CountryAnalytics[]>>(`${this.baseUrl}/by-country`);
  }

  getByDepartment(): Observable<ApiResponse<DepartmentAnalytics[]>> {
    return this.http.get<ApiResponse<DepartmentAnalytics[]>>(`${this.baseUrl}/by-department`);
  }

  getSalaryDistribution(currency: string): Observable<ApiResponse<SalaryBand[]>> {
    const params = new HttpParams().set('currency', currency);
    return this.http.get<ApiResponse<SalaryBand[]>>(`${this.baseUrl}/salary-distribution`, { params });
  }
}
