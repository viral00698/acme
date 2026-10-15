import { TestBed } from '@angular/core/testing';
import { provideRouter, Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { roleGuard } from './role.guard';
import { AuthService } from '../services/auth.service';

describe('roleGuard', () => {
  let router: Router;
  let authService: AuthService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        {
          provide: AuthService,
          useValue: {
            hasRole: vi.fn()
          }
        }
      ]
    });

    router = TestBed.inject(Router);
    authService = TestBed.inject(AuthService);
  });

  it('should allow access when user possesses required role', () => {
    vi.mocked(authService.hasRole).mockImplementation(role => role === 'ROLE_HR_ADMIN');

    const route = {
      data: { roles: ['ROLE_HR_ADMIN'] }
    } as unknown as ActivatedRouteSnapshot;

    const result = TestBed.runInInjectionContext(() =>
      roleGuard(route, {} as RouterStateSnapshot)
    );

    expect(result).toBe(true);
  });

  it('should redirect to /dashboard when user lacks required role', () => {
    vi.mocked(authService.hasRole).mockReturnValue(false);

    const navigateSpy = vi.spyOn(router, 'navigate');
    const route = {
      data: { roles: ['ROLE_HR_ADMIN'] }
    } as unknown as ActivatedRouteSnapshot;

    const result = TestBed.runInInjectionContext(() =>
      roleGuard(route, {} as RouterStateSnapshot)
    );

    expect(result).toBe(false);
    expect(navigateSpy).toHaveBeenCalledWith(['/dashboard']);
  });

  it('should allow access if no roles are required on route', () => {
    const route = {
      data: {}
    } as unknown as ActivatedRouteSnapshot;

    const result = TestBed.runInInjectionContext(() =>
      roleGuard(route, {} as RouterStateSnapshot)
    );

    expect(result).toBe(true);
  });
});
