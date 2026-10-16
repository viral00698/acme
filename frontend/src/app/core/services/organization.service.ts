import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, shareReplay, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import { Department, Country, Currency, JobTitle } from '../models/organization.model';

@Injectable({
  providedIn: 'root'
})
export class OrganizationService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/organization`;

  private departments$?: Observable<ApiResponse<Department[]>>;
  private countries$?: Observable<ApiResponse<Country[]>>;
  private currencies$?: Observable<ApiResponse<Currency[]>>;

  getDepartments(): Observable<ApiResponse<Department[]>> {
    if (!this.departments$) {
      this.departments$ = this.http.get<ApiResponse<Department[]>>(`${this.baseUrl}/departments`).pipe(
        shareReplay(1)
      );
    }
    return this.departments$;
  }

  getCountries(): Observable<ApiResponse<Country[]>> {
    if (!this.countries$) {
      this.countries$ = this.http.get<ApiResponse<Country[]>>(`${this.baseUrl}/countries`).pipe(
        map(res => ({
          ...res,
          data: res.data ? res.data.map(c => ({
            ...c,
            isoCode: c.isoCode || (c as any).code || ''
          })) : []
        })),
        shareReplay(1)
      );
    }
    return this.countries$;
  }

  getCurrencies(): Observable<ApiResponse<Currency[]>> {
    if (!this.currencies$) {
      this.currencies$ = this.http.get<ApiResponse<Currency[]>>(`${this.baseUrl}/currencies`).pipe(
        shareReplay(1)
      );
    }
    return this.currencies$;
  }

  getJobTitles(departmentId?: number): Observable<ApiResponse<JobTitle[]>> {
    let params = new HttpParams();
    if (departmentId) {
      params = params.set('departmentId', departmentId.toString());
    }
    return this.http.get<ApiResponse<JobTitle[]>>(`${this.baseUrl}/job-titles`, { params });
  }

  clearCache(): void {
    this.departments$ = undefined;
    this.countries$ = undefined;
    this.currencies$ = undefined;
  }
}
