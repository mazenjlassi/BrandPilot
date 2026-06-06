import { Injectable } from '@angular/core';

export interface Toast {
  id: number;
  message: string;
  type: 'success' | 'error' | 'info' | 'warning';
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private counter = 0;
  private containerEl: HTMLElement | null = null;
  private toasts: Toast[] = [];

  private ensureContainer() {
    if (!this.containerEl) {
      this.containerEl = document.createElement('div');
      this.containerEl.className = 'toast-container';
      document.body.appendChild(this.containerEl);
    }
    return this.containerEl;
  }

  private show(message: string, type: Toast['type'], duration = 3500) {
    const id = ++this.counter;
    const toast: Toast = { id, message, type };
    this.toasts.push(toast);

    const el = this.ensureContainer();
    const toastEl = document.createElement('div');
    toastEl.className = `toast toast-${type}`;
    toastEl.style.animation = 'toastSlideIn 0.3s ease forwards';

    const icons: Record<string, string> = {
      success: '&#10003;',
      error: '&#10007;',
      info: '&#8505;',
      warning: '&#9888;'
    };

    toastEl.innerHTML = `
      <span class="toast-icon">${icons[type]}</span>
      <span class="toast-msg">${message}</span>
    `;
    el.appendChild(toastEl);

    setTimeout(() => {
      toastEl.style.animation = 'toastSlideOut 0.3s ease forwards';
      setTimeout(() => {
        toastEl.remove();
        this.toasts = this.toasts.filter(t => t.id !== id);
      }, 300);
    }, duration);

    return id;
  }

  success(message: string) { return this.show(message, 'success'); }
  error(message: string) { return this.show(message, 'error'); }
  info(message: string) { return this.show(message, 'info'); }
  warning(message: string) { return this.show(message, 'warning'); }
}