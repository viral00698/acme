import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../models/api-response.model';
import { AuditLog } from '../models/audit.model';

export interface AuditFilterParams {
  entityName?: string | null;
  entityId?: number | null;
  actor?: string | null;
  page?: number;
  size?: number;
}

@Injectable({
  providedIn: 'root'
})
export class AuditService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/audit-logs`;

  getAuditLogs(filter: AuditFilterParams): Observable<ApiResponse<PageResponse<AuditLog>>> {
    let params = new HttpParams();

    if (filter.entityName) {
      params = params.set('entityName', filter.entityName);
    }
    if (filter.entityId) {
      params = params.set('entityId', filter.entityId.toString());
    }
    if (filter.actor) {
      params = params.set('actor', filter.actor);
    }
    if (filter.page !== undefined && filter.page !== null) {
      params = params.set('page', filter.page.toString());
    }
    if (filter.size !== undefined && filter.size !== null) {
      params = params.set('size', filter.size.toString());
    }

    return this.http.get<ApiResponse<PageResponse<AuditLog>>>(this.baseUrl, { params });
  }
}
