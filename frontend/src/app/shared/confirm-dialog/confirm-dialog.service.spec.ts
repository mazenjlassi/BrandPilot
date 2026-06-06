import { TestBed } from '@angular/core/testing';
import { ConfirmDialogService } from './confirm-dialog.service';

describe('ConfirmDialogService', () => {
  let service: ConfirmDialogService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ConfirmDialogService]
    });
    service = TestBed.inject(ConfirmDialogService);
    document.querySelectorAll('.confirm-overlay').forEach(el => el.remove());
  });

  afterEach(() => {
    document.querySelectorAll('.confirm-overlay').forEach(el => el.remove());
  });

  it('creates_service', () => {
    expect(service).toBeTruthy();
  });

  it('confirm_shows_dialog', () => {
    service.confirm({ title: 'Test', message: 'Are you sure?' });
    const overlay = document.querySelector('.confirm-overlay');
    expect(overlay).toBeTruthy();
  });

  it('confirm_shows_title_and_message', () => {
    service.confirm({ title: 'Delete', message: 'Delete this item?' });
    const dialog = document.querySelector('.confirm-dialog');
    expect(dialog?.textContent).toContain('Delete');
    expect(dialog?.textContent).toContain('Delete this item?');
  });

  it('clicking_ok_resolves_true', (done) => {
    const promise = service.confirm({ title: 'Test', message: 'OK?' });
    const okBtn = document.querySelector('.confirm-ok') as HTMLElement;
    okBtn.click();

    promise.then(result => {
      expect(result).toBeTrue();
      done();
    });
  });

  it('clicking_cancel_resolves_false', (done) => {
    const promise = service.confirm({ title: 'Test', message: 'Cancel?' });
    const cancelBtn = document.querySelector('.confirm-cancel') as HTMLElement;
    cancelBtn.click();

    promise.then(result => {
      expect(result).toBeFalse();
      done();
    });
  });

  it('clicking_overlay_resolves_false', (done) => {
    const promise = service.confirm({ title: 'Test', message: 'Overlay?' });
    const overlay = document.querySelector('.confirm-overlay') as HTMLElement;
    overlay.click();

    promise.then(result => {
      expect(result).toBeFalse();
      done();
    });
  });

  it('danger_shows_danger_class', () => {
    service.confirm({ title: 'Danger', message: 'Delete?', danger: true });
    const okBtn = document.querySelector('.confirm-ok');
    expect(okBtn?.classList).toContain('confirm-danger');
  });

  it('uses_default_button_text_when_not_specified', () => {
    service.confirm({ title: 'Test', message: 'Test' });
    const dialog = document.querySelector('.confirm-dialog');
    expect(dialog?.textContent).toContain('Cancel');
    expect(dialog?.textContent).toContain('Confirm');
  });

  it('uses_custom_button_text_when_specified', () => {
    service.confirm({ title: 'Test', message: 'Test', confirmText: 'Yes', cancelText: 'No' });
    const dialog = document.querySelector('.confirm-dialog');
    expect(dialog?.textContent).toContain('Yes');
    expect(dialog?.textContent).toContain('No');
  });

  it('removes_existing_dialog_before_showing_new', () => {
    service.confirm({ title: 'First', message: 'First' });
    expect(document.querySelectorAll('.confirm-overlay').length).toBe(1);

    service.confirm({ title: 'Second', message: 'Second' });
    expect(document.querySelectorAll('.confirm-overlay').length).toBe(1);
  });
});
