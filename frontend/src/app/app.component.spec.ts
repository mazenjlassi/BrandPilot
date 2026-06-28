import { TestBed } from '@angular/core/testing';
import { AppComponent } from './app.component';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter, Router, Routes } from '@angular/router';
import { SidebarStateService } from './shared/sidebar-state.service';
import { Component } from '@angular/core';

@Component({ template: '' })
class DummyComponent {}

describe('AppComponent', () => {
  let routes: Routes;

  beforeEach(async () => {
    routes = [
      { path: 'login', component: DummyComponent },
      { path: 'dashboard', component: DummyComponent },
    ];
    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [
        provideRouter(routes),
        provideHttpClient(),
        provideHttpClientTesting(),
        SidebarStateService
      ]
    }).compileComponents();
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('sets isLoginRoute to true on login route', async () => {
    const router = TestBed.inject(Router);
    await router.navigate(['/login']);
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    expect(fixture.componentInstance.isLoginRoute).toBeTrue();
  });

  it('sets isLoginRoute to false on non-login route', async () => {
    const router = TestBed.inject(Router);
    await router.navigate(['/dashboard']);
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    expect(fixture.componentInstance.isLoginRoute).toBeFalse();
  });

  it('updates isLoginRoute on NavigationEnd', async () => {
    const router = TestBed.inject(Router);
    await router.navigate(['/dashboard']);
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    expect(fixture.componentInstance.isLoginRoute).toBeFalse();
    await router.navigate(['/login']);
    fixture.detectChanges();
    expect(fixture.componentInstance.isLoginRoute).toBeTrue();
  });

  it('exposes sidebarCollapsed from SidebarStateService', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const sidebar = TestBed.inject(SidebarStateService);
    expect(fixture.componentInstance.sidebarCollapsed).toBe(sidebar.collapsed);
  });
});
