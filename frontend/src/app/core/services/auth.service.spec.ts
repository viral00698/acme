import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { ApiResponse } from '../models/api-response.model';
import { AuthResponse } from '../models/auth.model';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let router: Router;

  const mockAuthResponse: ApiResponse<AuthResponse> = {
    success: true,
    message: 'Login successful',
    timestamp: new Date().toISOString(),
    data: {
      accessToken: 'mock-jwt-access-token',
      refreshToken: 'mock-jwt-refresh-token',
      tokenType: 'Bearer',
      expiresInSeconds: 900,
      user: {
        id: 1,
        username: 'manager',
        email: 'manager@acme.corp',
        fullName: 'HR Manager',
        roles: ['ROLE_HR_MANAGER']
      }
    }
  };

  beforeEach(() => {
    localStorage.clear();

    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([])
      ]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should authenticate user and store tokens on login', () => {
    expect(service.isAuthenticated()).toBe(false);

    service.login({ username: 'manager', password: 'Manager@Acme2026!' }).subscribe(res => {
      expect(res.success).toBe(true);
      expect(res.data.user.username).toBe('manager');
    });

    const req = httpMock.expectOne('/api/v1/auth/login');
    expect(req.request.method).toBe('POST');
    req.flush(mockAuthResponse);

    expect(service.getAccessToken()).toBe('mock-jwt-access-token');
    expect(service.getRefreshToken()).toBe('mock-jwt-refresh-token');
    expect(service.isAuthenticated()).toBe(true);
    expect(service.isManager()).toBe(true);
    expect(service.isAdmin()).toBe(false);
  });

  it('should verify role permissions correctly', () => {
    expect(service.hasRole('ROLE_HR_MANAGER')).toBe(false);

    service.login({ username: 'manager', password: 'password' }).subscribe();
    const req = httpMock.expectOne('/api/v1/auth/login');
    req.flush(mockAuthResponse);

    expect(service.hasRole('ROLE_HR_MANAGER')).toBe(true);
    expect(service.hasRole('ROLE_HR_ADMIN')).toBe(false);
  });

  it('should clear session and tokens on logout', () => {
    service.login({ username: 'manager', password: 'password' }).subscribe();
    httpMock.expectOne('/api/v1/auth/login').flush(mockAuthResponse);

    expect(service.getAccessToken()).toBeTruthy();

    const navigateSpy = vi.spyOn(router, 'navigate');
    service.logout();

    // Logout request to backend
    const logoutReq = httpMock.expectOne('/api/v1/auth/logout');
    expect(logoutReq.request.method).toBe('POST');
    logoutReq.flush({ success: true, message: 'Logged out' });

    expect(service.getAccessToken()).toBeNull();
    expect(service.getRefreshToken()).toBeNull();
    expect(service.isAuthenticated()).toBe(false);
    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  });
});
