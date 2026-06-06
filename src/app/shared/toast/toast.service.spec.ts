import { TestBed } from '@angular/core/testing';
import { ToastService } from './toast.service';

describe('ToastService', () => {
  let service: ToastService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ToastService]
    });
    service = TestBed.inject(ToastService);
    document.querySelectorAll('.toast-container').forEach(el => el.remove());
  });

  afterEach(() => {
    document.querySelectorAll('.toast-container').forEach(el => el.remove());
  });

  it('creates_service', () => {
    expect(service).toBeTruthy();
  });

  it('success_adds_toast_to_dom', () => {
    service.success('Operation successful');
    const toast = document.querySelector('.toast');
    expect(toast).toBeTruthy();
    expect(toast?.textContent).toContain('Operation successful');
  });

  it('success_adds_toast_success_class', () => {
    service.success('OK');
    const toast = document.querySelector('.toast-success');
    expect(toast).toBeTruthy();
  });

  it('error_adds_error_type_toast', () => {
    service.error('Failed');
    const toast = document.querySelector('.toast-error');
    expect(toast).toBeTruthy();
    expect(toast?.textContent).toContain('Failed');
  });

  it('info_adds_info_type_toast', () => {
    service.info('Information');
    const toast = document.querySelector('.toast-info');
    expect(toast).toBeTruthy();
  });

  it('warning_adds_warning_type_toast', () => {
    service.warning('Warning');
    const toast = document.querySelector('.toast-warning');
    expect(toast).toBeTruthy();
  });

  it('returns_unique_ids', () => {
    const id1 = service.success('First');
    const id2 = service.error('Second');
    expect(id1).not.toBe(id2);
  });

  it('toast_is_removed_after_duration', (done) => {
    service.success('Temporary');
    expect(document.querySelector('.toast')).toBeTruthy();

    setTimeout(() => {
      const toast = document.querySelector('.toast');
      expect(toast).toBeFalsy();
      done();
    }, 4000);
  });
});
