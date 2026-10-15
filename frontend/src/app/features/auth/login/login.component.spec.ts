import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { of, throwError } from 'rxjs';
import { LoginComponent } from './login.component';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authService: any;
  let router: Router;

  beforeEach(async () => {
    authService = {
      login: vi.fn(),
      currentUser: vi.fn().mockReturnValue(null),
      isAuthenticated: vi.fn().mockReturnValue(false)
    };

    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        provideRouter([]),
        provideAnimationsAsync(),
        { provide: AuthService, useValue: authService },
        { provide: NotificationService, useValue: { success: vi.fn(), error: vi.fn() } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('should create login component', () => {
    expect(component).toBeTruthy();
    expect(component.loginForm.valid).toBe(false);
  });

  it('should validate required fields', () => {
    const usernameCtrl = component.loginForm.get('username');
    const passwordCtrl = component.loginForm.get('password');

    expect(usernameCtrl?.hasError('required')).toBe(true);
    expect(passwordCtrl?.hasError('required')).toBe(true);

    usernameCtrl?.setValue('admin');
    passwordCtrl?.setValue('Admin@Acme2026!');

    expect(component.loginForm.valid).toBe(true);
  });

  it('should populate credentials when preset is selected', () => {
    component.setPresetCredentials('admin');

    expect(component.loginForm.get('username')?.value).toBe('admin');
    expect(component.loginForm.get('password')?.value).toBe('Admin@Acme2026!');
    expect(component.loginForm.valid).toBe(true);
  });

  it('should submit form and navigate to dashboard on success', () => {
    const navigateSpy = vi.spyOn(router, 'navigate');
    authService.login.mockReturnValue(of({
      success: true,
      data: {
        accessToken: 'tok',
        user: { username: 'admin', fullName: 'Administrator', roles: ['ROLE_HR_ADMIN'] }
      }
    }));

    component.setPresetCredentials('admin');
    component.onSubmit();

    expect(authService.login).toHaveBeenCalledWith({
      username: 'admin',
      password: 'Admin@Acme2026!'
    });
    expect(navigateSpy).toHaveBeenCalledWith(['/dashboard']);
  });

  it('should display error message on authentication failure', () => {
    authService.login.mockReturnValue(throwError(() => ({
      status: 401,
      error: { message: 'Invalid credentials' }
    })));

    component.setPresetCredentials('viewer');
    component.onSubmit();

    expect(component.errorMessage()).toBe('Invalid username or password.');
    expect(component.isLoading()).toBe(false);
  });
});
