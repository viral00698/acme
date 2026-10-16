import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../models/api-response.model';
import {
  EmployeeSummary,
  EmployeeDetail,
  CreateEmployeeRequest,
  UpdateEmployeeRequest,
  UpdateStatusRequest,
  EmployeeSearchParams
} from '../models/employee.model';

@Injectable({
  providedIn: 'root'
})
export class EmployeeService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/employees`;

  getEmployees(params: EmployeeSearchParams): Observable<ApiResponse<PageResponse<EmployeeSummary>>> {
    let httpParams = new HttpParams();

    if (params.page !== undefined && params.page !== null) {
      httpParams = httpParams.set('page', params.page.toString());
    }
    if (params.size !== undefined && params.size !== null) {
      httpParams = httpParams.set('size', params.size.toString());
    }
    if (params.search && params.search.trim().length > 0) {
      httpParams = httpParams.set('search', params.search.trim());
    }
    if (params.departmentId !== undefined && params.departmentId !== null) {
      httpParams = httpParams.set('departmentId', params.departmentId.toString());
    }
    if (params.countryId !== undefined && params.countryId !== null) {
      httpParams = httpParams.set('countryId', params.countryId.toString());
    }
    if (params.jobTitleId !== undefined && params.jobTitleId !== null) {
      httpParams = httpParams.set('jobTitleId', params.jobTitleId.toString());
    }
    if (params.status && params.status.trim().length > 0) {
      httpParams = httpParams.set('status', params.status.trim());
    }
    if (params.sortBy) {
      httpParams = httpParams.set('sortBy', params.sortBy);
    }
    if (params.sortDir) {
      httpParams = httpParams.set('sortDir', params.sortDir);
    }

    return this.http.get<ApiResponse<PageResponse<EmployeeSummary>>>(this.baseUrl, { params: httpParams });
  }

  getEmployee(id: number): Observable<ApiResponse<EmployeeDetail>> {
    return this.http.get<ApiResponse<EmployeeDetail>>(`${this.baseUrl}/${id}`);
  }

  createEmployee(request: CreateEmployeeRequest): Observable<ApiResponse<EmployeeDetail>> {
    return this.http.post<ApiResponse<EmployeeDetail>>(this.baseUrl, request);
  }

  updateEmployee(id: number, request: UpdateEmployeeRequest): Observable<ApiResponse<EmployeeDetail>> {
    return this.http.put<ApiResponse<EmployeeDetail>>(`${this.baseUrl}/${id}`, request);
  }

  updateStatus(id: number, request: UpdateStatusRequest): Observable<ApiResponse<EmployeeDetail>> {
    return this.http.patch<ApiResponse<EmployeeDetail>>(`${this.baseUrl}/${id}/status`, request);
  }
}
