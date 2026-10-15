import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { authGuard } from './auth.guard';
import { AuthService } from '../services/auth.service';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';

describe('authGuard', () => {
  let router: Router;
  let authService: AuthService;

  beforeEach(() => {
    localStorage.clear();

    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        {
          provide: AuthService,
          useValue: {
            isAuthenticated: vi.fn(),
            getAccessToken: vi.fn()
          }
        }
      ]
    });

    router = TestBed.inject(Router);
    authService = TestBed.inject(AuthService);
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should allow access if user is authenticated', () => {
    vi.mocked(authService.isAuthenticated).mockReturnValue(true);

    const result = TestBed.runInInjectionContext(() =>
      authGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot)
    );

    expect(result).toBe(true);
  });

  it('should allow access if access token exists in storage', () => {
    vi.mocked(authService.isAuthenticated).mockReturnValue(false);
    vi.mocked(authService.getAccessToken).mockReturnValue('valid-token');

    const result = TestBed.runInInjectionContext(() =>
      authGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot)
    );

    expect(result).toBe(true);
  });

  it('should redirect to /login if user is unauthenticated and has no token', () => {
    vi.mocked(authService.isAuthenticated).mockReturnValue(false);
    vi.mocked(authService.getAccessToken).mockReturnValue(null);

    const navigateSpy = vi.spyOn(router, 'navigate');

    const result = TestBed.runInInjectionContext(() =>
      authGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot)
    );

    expect(result).toBe(false);
    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  });
});
