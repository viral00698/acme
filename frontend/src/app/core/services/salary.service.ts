import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import { SalaryHistory, SalaryRecord, CreateSalaryRequest } from '../models/salary.model';

@Injectable({
  providedIn: 'root'
})
export class SalaryService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/employees`;

  getSalaryHistory(employeeId: number): Observable<ApiResponse<SalaryHistory>> {
    return this.http.get<ApiResponse<SalaryHistory>>(`${this.baseUrl}/${employeeId}/salaries`);
  }

  createSalaryRevision(employeeId: number, request: CreateSalaryRequest): Observable<ApiResponse<SalaryRecord>> {
    return this.http.post<ApiResponse<SalaryRecord>>(`${this.baseUrl}/${employeeId}/salaries`, request);
  }

  getSalaryRecord(employeeId: number, salaryId: number): Observable<ApiResponse<SalaryRecord>> {
    return this.http.get<ApiResponse<SalaryRecord>>(`${this.baseUrl}/${employeeId}/salaries/${salaryId}`);
  }
}
