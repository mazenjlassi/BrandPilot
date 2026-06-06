import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoginComponent } from './login.component';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { HttpTestingController } from '@angular/common/http/testing';
import { Router } from '@angular/router';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authService: AuthService;
  let httpMock: HttpTestingController;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        AuthService
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('creates_component', () => {
    expect(component).toBeTruthy();
  });

  it('login_skips_when_fields_empty', () => {
    component.username = '';
    component.password = '';
    component.login();
    httpMock.expectNone('http://localhost:8081/auth/login');
  });

  it('login_skips_when_username_missing', () => {
    component.username = '';
    component.password = 'pass';
    component.login();
    httpMock.expectNone('http://localhost:8081/auth/login');
  });

  it('login_skips_when_password_missing', () => {
    component.username = 'user';
    component.password = '';
    component.login();
    httpMock.expectNone('http://localhost:8081/auth/login');
  });

  it('login_calls_auth_and_navigates', () => {
    const routerSpy = spyOn(router, 'navigate');

    component.username = 'user';
    component.password = 'pass';
    component.login();

    const req = httpMock.expectOne('http://localhost:8081/auth/login');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ username: 'user', password: 'pass' });
    req.flush({ token: 'jwt', role: 'ADMIN', name: 'Admin' });

    expect(component.loading).toBeFalse();
    expect(component.error).toBe('');
    expect(routerSpy).toHaveBeenCalledWith(['/dashboard']);
  });

  it('login_sets_error_on_failure', () => {
    component.username = 'user';
    component.password = 'wrong';
    component.login();

    const req = httpMock.expectOne('http://localhost:8081/auth/login');
    req.error(new ProgressEvent('error'));

    expect(component.loading).toBeFalse();
    expect(component.error).toBe('Invalid credentials');
  });

  it('login_sets_loading_during_request', () => {
    component.username = 'user';
    component.password = 'pass';
    component.login();

    expect(component.loading).toBeTrue();

    const req = httpMock.expectOne('http://localhost:8081/auth/login');
    req.flush({ token: 'jwt', role: 'ADMIN', name: 'Admin' });
  });
});
