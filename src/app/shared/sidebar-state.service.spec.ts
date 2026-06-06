import { TestBed } from '@angular/core/testing';
import { SidebarStateService } from './sidebar-state.service';

describe('SidebarStateService', () => {
  let service: SidebarStateService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [SidebarStateService]
    });
    service = TestBed.inject(SidebarStateService);
    localStorage.clear();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('creates_service', () => {
    expect(service).toBeTruthy();
  });

  it('initial_state_is_not_collapsed', () => {
    expect(service.collapsed).toBeFalse();
  });

  it('toggle_switches_state', () => {
    expect(service.collapsed).toBeFalse();

    service.toggle();
    expect(service.collapsed).toBeTrue();

    service.toggle();
    expect(service.collapsed).toBeFalse();
  });

  it('init_restores_from_localStorage', () => {
    localStorage.setItem('sidebar-collapsed', 'true');
    service.init();
    expect(service.collapsed).toBeTrue();
  });

  it('init_defaults_to_false_when_no_stored_value', () => {
    service.init();
    expect(service.collapsed).toBeFalse();
  });

  it('toggle_persists_to_localStorage', () => {
    service.toggle();
    expect(localStorage.getItem('sidebar-collapsed')).toBe('true');

    service.toggle();
    expect(localStorage.getItem('sidebar-collapsed')).toBe('false');
  });
});
