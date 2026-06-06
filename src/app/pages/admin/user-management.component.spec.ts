import { ComponentFixture, TestBed } from '@angular/core/testing';
import { UserManagementComponent } from './user-management.component';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { ConfirmDialogService } from '../../shared/confirm-dialog/confirm-dialog.service';

describe('UserManagementComponent', () => {
  let component: UserManagementComponent;
  let fixture: ComponentFixture<UserManagementComponent>;
  let httpMock: HttpTestingController;
  let confirmService: ConfirmDialogService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserManagementComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        ConfirmDialogService
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(UserManagementComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    confirmService = TestBed.inject(ConfirmDialogService);
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('creates_component', () => {
    expect(component).toBeTruthy();
  });

  it('loads_users_on_init', () => {
    fixture.detectChanges();
    const req = httpMock.expectOne('http://localhost:8081/admin/users');
    expect(req.request.method).toBe('GET');
    req.flush([{ id: 1, name: 'John', email: 'john@test.com', role: 'ADMIN', banned: false }]);

    expect(component.users.length).toBe(1);
    expect(component.users[0].name).toBe('John');
  });

  it('createUser_sends_post', () => {
    component.newUser = { name: 'Jane', email: 'jane@test.com', password: 'pass', role: 'MARKETING' };
    component.createUser();

    const req = httpMock.expectOne('http://localhost:8081/admin/users');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ name: 'Jane', email: 'jane@test.com', password: 'pass', role: 'MARKETING' });
    req.flush({ id: 2, name: 'Jane', email: 'jane@test.com', role: 'MARKETING', banned: false });

    expect(component.users.length).toBe(1);
    expect(component.showCreateForm).toBeFalse();
    expect(component.newUser.name).toBe('');
  });

  it('createUser_skips_when_fields_missing', () => {
    component.newUser = { name: '', email: '', password: '', role: 'MARKETING' };
    component.createUser();
    httpMock.expectNone('http://localhost:8081/admin/users');
  });

  it('deleteUser_confirms_and_deletes', (done) => {
    component.users = [{ id: 1, name: 'John', email: '', role: '', banned: false }, { id: 2, name: 'Jane', email: '', role: '', banned: false }];
    localStorage.setItem('token', 'test-token');
    spyOn(confirmService, 'confirm').and.returnValue(Promise.resolve(true));

    component.deleteUser(1).then(() => {
      const req = httpMock.expectOne('http://localhost:8081/admin/users/1');
      expect(req.request.method).toBe('DELETE');
      req.flush({});

      expect(component.users.length).toBe(1);
      expect(component.users[0].id).toBe(2);
      done();
    });
  });

  it('banUser_sends_ban_request', () => {
    component.users = [{ id: 1, name: 'John', banned: false, email: '', role: '' }];
    component.banUser(1);

    const req = httpMock.expectOne('http://localhost:8081/admin/users/1/ban');
    expect(req.request.method).toBe('PUT');
    req.flush({ id: 1, name: 'John', banned: true, email: '', role: '' });

    expect(component.users[0].banned).toBeTrue();
  });

  it('unbanUser_sends_unban_request', () => {
    component.users = [{ id: 1, name: 'John', banned: true, email: '', role: '' }];
    component.unbanUser(1);

    const req = httpMock.expectOne('http://localhost:8081/admin/users/1/unban');
    expect(req.request.method).toBe('PUT');
    req.flush({ id: 1, name: 'John', banned: false, email: '', role: '' });

    expect(component.users[0].banned).toBeFalse();
  });

  it('getHeaders_includes_auth_token', () => {
    localStorage.setItem('token', 'my-jwt');
    const headers = (component as any).getHeaders();
    expect(headers.get('Authorization')).toBe('Bearer my-jwt');
  });

  it('resetForm_clears_newUser', () => {
    component.newUser = { name: 'Test', email: 'test@test.com', password: 'pass', role: 'ADMIN' };
    component.resetForm();

    expect(component.newUser.name).toBe('');
    expect(component.newUser.email).toBe('');
    expect(component.newUser.password).toBe('');
    expect(component.newUser.role).toBe('MARKETING');
  });

  it('createUser_sets_creating_false_on_error', () => {
    component.newUser = { name: 'Jane', email: 'jane@test.com', password: 'pass', role: 'MARKETING' };
    component.createUser();

    expect(component.creating).toBeTrue();

    const req = httpMock.expectOne('http://localhost:8081/admin/users');
    req.error(new ProgressEvent('error'));

    expect(component.creating).toBeFalse();
  });
});
