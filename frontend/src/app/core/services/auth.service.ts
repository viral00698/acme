import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, catchError, throwError, of, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../models/api-response.model';
import { AuthResponse, LoginRequest, RefreshTokenRequest, UserProfile } from '../models/auth.model';

const ACCESS_TOKEN_KEY = 'acme_access_token';
const REFRESH_TOKEN_KEY = 'acme_refresh_token';
const USER_KEY = 'acme_user';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  private baseUrl = `${environment.apiUrl}/auth`;

  // Reactive user state
  private currentUserSignal = signal<UserProfile | null>(this.getStoredUser());
  readonly currentUser = this.currentUserSignal.asReadonly();
  readonly isAuthenticated = computed(() => !!this.currentUserSignal());

  readonly isAdmin = computed(() => this.hasRole('ROLE_HR_ADMIN'));
  readonly isManager = computed(() => this.hasRole('ROLE_HR_MANAGER') || this.hasRole('ROLE_HR_ADMIN'));
  readonly isViewer = computed(() => this.hasRole('ROLE_HR_VIEWER') || this.isManager());

  getAccessToken(): string | null {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  }

  hasRole(role: string): boolean {
    const user = this.currentUserSignal();
    return !!user && user.roles.includes(role);
  }

  login(credentials: LoginRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.baseUrl}/login`, credentials).pipe(
      tap(response => {
        if (response.success && response.data) {
          this.storeSession(response.data);
        }
      })
    );
  }

  refreshToken(): Observable<ApiResponse<AuthResponse>> {
    const refreshToken = this.getRefreshToken();
    if (!refreshToken) {
      this.clearSession();
      return throwError(() => new Error('No refresh token available'));
    }

    const payload: RefreshTokenRequest = { refreshToken };
    return this.http.post<ApiResponse<AuthResponse>>(`${this.baseUrl}/refresh`, payload).pipe(
      tap(response => {
        if (response.success && response.data) {
          this.storeSession(response.data);
        }
      }),
      catchError(err => {
        this.clearSession();
        this.router.navigate(['/login']);
        return throwError(() => err);
      })
    );
  }

  logout(): void {
    const refreshToken = this.getRefreshToken();
    if (refreshToken) {
      this.http.post<ApiResponse<void>>(`${this.baseUrl}/logout`, { refreshToken }).subscribe({
        next: () => {},
        error: () => {}
      });
    }
    this.clearSession();
    this.router.navigate(['/login']);
  }

  fetchCurrentUser(): Observable<UserProfile | null> {
    const token = this.getAccessToken();
    if (!token) {
      return of(null);
    }
    return this.http.get<ApiResponse<UserProfile>>(`${this.baseUrl}/me`).pipe(
      map(res => {
        if (res.success && res.data) {
          this.currentUserSignal.set(res.data);
          localStorage.setItem(USER_KEY, JSON.stringify(res.data));
          return res.data;
        }
        return null;
      }),
      catchError(() => {
        return of(null);
      })
    );
  }

  private storeSession(authResponse: AuthResponse): void {
    localStorage.setItem(ACCESS_TOKEN_KEY, authResponse.accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, authResponse.refreshToken);
    localStorage.setItem(USER_KEY, JSON.stringify(authResponse.user));
    this.currentUserSignal.set(authResponse.user);
  }

  private clearSession(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.currentUserSignal.set(null);
  }

  private getStoredUser(): UserProfile | null {
    const userJson = localStorage.getItem(USER_KEY);
    if (!userJson) return null;
    try {
      return JSON.parse(userJson) as UserProfile;
    } catch {
      return null;
    }
  }
}
