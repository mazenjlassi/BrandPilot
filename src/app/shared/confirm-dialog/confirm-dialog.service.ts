import { Injectable } from '@angular/core';

export interface ConfirmOptions {
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  danger?: boolean;
}

@Injectable({ providedIn: 'root' })
export class ConfirmDialogService {
  private resolver: ((value: boolean) => void) | null = null;

  confirm(options: ConfirmOptions): Promise<boolean> {
    return new Promise(resolve => {
      this.resolver = resolve;
      this.showDialog(options);
    });
  }

  private showDialog(options: ConfirmOptions) {
    const existing = document.querySelector('.confirm-overlay');
    if (existing) existing.remove();

    const overlay = document.createElement('div');
    overlay.className = 'confirm-overlay';

    const dangerClass = options.danger !== false ? 'confirm-danger' : '';

    overlay.innerHTML = `
      <div class="confirm-dialog">
        <h3 class="confirm-title">${options.title}</h3>
        <p class="confirm-msg">${options.message}</p>
        <div class="confirm-actions">
          <button class="confirm-cancel btn">${options.cancelText || 'Cancel'}</button>
          <button class="confirm-ok btn ${dangerClass}">${options.confirmText || 'Confirm'}</button>
        </div>
      </div>
    `;

    const cancelBtn = overlay.querySelector('.confirm-cancel')!;
    const okBtn = overlay.querySelector('.confirm-ok')!;

    const close = (result: boolean) => {
      overlay.remove();
      if (this.resolver) {
        this.resolver(result);
        this.resolver = null;
      }
    };

    cancelBtn.addEventListener('click', () => close(false));
    okBtn.addEventListener('click', () => close(true));
    overlay.addEventListener('click', (e) => {
      if (e.target === overlay) close(false);
    });

    document.body.appendChild(overlay);

    setTimeout(() => overlay.classList.add('visible'), 10);
  }
}