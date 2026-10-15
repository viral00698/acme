import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../models/api-response.model';
import { SalaryReportRow } from '../models/salary.model';

export interface ReportFilterParams {
  departmentId?: number | null;
  countryId?: number | null;
  currency?: string | null;
  minSalary?: number | null;
  maxSalary?: number | null;
  status?: string | null;
  page?: number;
  size?: number;
}

@Injectable({
  providedIn: 'root'
})
export class ReportService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/reports/salaries`;

  getSalaryReport(filter: ReportFilterParams): Observable<ApiResponse<PageResponse<SalaryReportRow>>> {
    const params = this.buildHttpParams(filter);
    return this.http.get<ApiResponse<PageResponse<SalaryReportRow>>>(this.baseUrl, { params });
  }

  downloadCsv(filter: ReportFilterParams): Observable<Blob> {
    const params = this.buildHttpParams(filter);
    return this.http.get(`${this.baseUrl}/export`, {
      params,
      responseType: 'blob'
    });
  }

  private buildHttpParams(filter: ReportFilterParams): HttpParams {
    let params = new HttpParams();

    if (filter.departmentId) {
      params = params.set('departmentId', filter.departmentId.toString());
    }
    if (filter.countryId) {
      params = params.set('countryId', filter.countryId.toString());
    }
    if (filter.currency) {
      params = params.set('currency', filter.currency);
    }
    if (filter.minSalary !== undefined && filter.minSalary !== null) {
      params = params.set('minSalary', filter.minSalary.toString());
    }
    if (filter.maxSalary !== undefined && filter.maxSalary !== null) {
      params = params.set('maxSalary', filter.maxSalary.toString());
    }
    if (filter.status) {
      params = params.set('status', filter.status);
    }
    if (filter.page !== undefined && filter.page !== null) {
      params = params.set('page', filter.page.toString());
    }
    if (filter.size !== undefined && filter.size !== null) {
      params = params.set('size', filter.size.toString());
    }

    return params;
  }
}
