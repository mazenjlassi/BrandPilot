import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HeaderComponent } from './header.component';
import { provideRouter } from '@angular/router';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth.service';
import { SidebarStateService } from '../sidebar-state.service';
import { Router } from '@angular/router';

describe('HeaderComponent', () => {
  let component: HeaderComponent;
  let fixture: ComponentFixture<HeaderComponent>;
  let authService: AuthService;
  let sidebarState: SidebarStateService;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HeaderComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        AuthService,
        SidebarStateService
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(HeaderComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService);
    sidebarState = TestBed.inject(SidebarStateService);
    router = TestBed.inject(Router);
    localStorage.clear();
    fixture.detectChanges();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('creates_component', () => {
    expect(component).toBeTruthy();
  });

  it('toggleSidebar_calls_service', () => {
    const spy = spyOn(sidebarState, 'toggle');
    component.toggleSidebar();
    expect(spy).toHaveBeenCalled();
  });

  it('sidebarCollapsed_returns_state', () => {
    expect(component.sidebarCollapsed).toBe(sidebarState.collapsed);
    sidebarState.toggle();
    expect(component.sidebarCollapsed).toBe(sidebarState.collapsed);
  });

  it('toggleTheme_switches_darkMode', () => {
    component.isDarkMode = false;
    component.toggleTheme();
    expect(component.isDarkMode).toBeTrue();
    expect(localStorage.getItem('theme')).toBe('dark');

    component.toggleTheme();
    expect(component.isDarkMode).toBeFalse();
    expect(localStorage.getItem('theme')).toBe('light');
  });

  it('toggleTheme_applies_theme_to_body', () => {
    document.body.classList.remove('dark-mode');
    component.isDarkMode = false;

    component.toggleTheme();
    expect(document.body.classList.contains('dark-mode')).toBeTrue();

    component.toggleTheme();
    expect(document.body.classList.contains('dark-mode')).toBeFalse();
  });

  it('toggleMenu_toggles_mobileMenuOpen', () => {
    expect(component.mobileMenuOpen).toBeFalse();
    component.toggleMenu();
    expect(component.mobileMenuOpen).toBeTrue();
    component.toggleMenu();
    expect(component.mobileMenuOpen).toBeFalse();
  });

  it('closeMenu_sets_mobileMenuOpen_false', () => {
    component.mobileMenuOpen = true;
    component.closeMenu();
    expect(component.mobileMenuOpen).toBeFalse();
  });

  it('isAdmin_delegates_to_auth', () => {
    spyOn(authService, 'isAdmin').and.returnValue(true);
    expect(component.isAdmin()).toBeTrue();

    authService.isAdmin = jasmine.createSpy().and.returnValue(false);
    expect(component.isAdmin()).toBeFalse();
  });

  it('isMarketing_delegates_to_auth', () => {
    spyOn(authService, 'isMarketing').and.returnValue(true);
    expect(component.isMarketing()).toBeTrue();
  });

  it('getUserName_delegates_to_auth', () => {
    spyOn(authService, 'getName').and.returnValue('John');
    expect(component.getUserName()).toBe('John');
  });

  it('getUserName_returns_empty_when_null', () => {
    spyOn(authService, 'getName').and.returnValue(null);
    expect(component.getUserName()).toBe('');
  });

  it('logout_calls_auth_and_navigates', () => {
    const routerSpy = spyOn(router, 'navigate');
    spyOn(authService, 'logout');

    component.logout();

    expect(authService.logout).toHaveBeenCalled();
    expect(routerSpy).toHaveBeenCalledWith(['/login']);
  });

  it('init_applies_saved_theme', () => {
    localStorage.setItem('theme', 'dark');
    component.ngOnInit();
    expect(component.isDarkMode).toBeTrue();
  });
});
