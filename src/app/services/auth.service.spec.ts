import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        AuthService
      ]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('login_storesTokenInLocalStorage', () => {
    service.login('user', 'pass').subscribe(() => {
      expect(localStorage.getItem('token')).toBe('jwt-token');
    });

    const req = httpMock.expectOne('http://localhost:8081/auth/login');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ username: 'user', password: 'pass' });
    req.flush({ token: 'jwt-token', role: 'ADMIN', name: 'Admin' });
  });

  it('login_storesRoleAndName', () => {
    service.login('user', 'pass').subscribe(() => {
      expect(localStorage.getItem('role')).toBe('ADMIN');
      expect(localStorage.getItem('name')).toBe('Admin');
    });

    const req = httpMock.expectOne('http://localhost:8081/auth/login');
    req.flush({ token: 'jwt-token', role: 'ADMIN', name: 'Admin' });
  });

  it('getToken_returnsFromLocalStorage', () => {
    localStorage.setItem('token', 'my-token');
    expect(service.getToken()).toBe('my-token');
  });

  it('getRole_returnsFromLocalStorage', () => {
    localStorage.setItem('role', 'MARKETING');
    expect(service.getRole()).toBe('MARKETING');
  });

  it('isAdmin_returnsTrue_whenAdmin', () => {
    localStorage.setItem('role', 'ADMIN');
    expect(service.isAdmin()).toBeTrue();
    expect(service.isMarketing()).toBeFalse();
  });

  it('isMarketing_returnsTrue_whenMarketing', () => {
    localStorage.setItem('role', 'MARKETING');
    expect(service.isMarketing()).toBeTrue();
    expect(service.isAdmin()).toBeFalse();
  });

  it('isAuthenticated_returnsTrue_whenTokenExists', () => {
    localStorage.setItem('token', 'some-token');
    expect(service.isAuthenticated()).toBeTrue();
  });

  it('logout_clearsLocalStorage', () => {
    localStorage.setItem('token', 't');
    localStorage.setItem('role', 'r');
    localStorage.setItem('name', 'n');
    service.logout();
    expect(localStorage.getItem('token')).toBeNull();
    expect(localStorage.getItem('role')).toBeNull();
    expect(localStorage.getItem('name')).toBeNull();
  });
});
