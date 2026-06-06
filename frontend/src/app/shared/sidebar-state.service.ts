import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class SidebarStateService {
  private _collapsed = false;

  get collapsed(): boolean {
    return this._collapsed;
  }

  toggle() {
    this._collapsed = !this._collapsed;
    localStorage.setItem('sidebar-collapsed', String(this._collapsed));
  }

  init() {
    const saved = localStorage.getItem('sidebar-collapsed');
    this._collapsed = saved === 'true';
  }
}