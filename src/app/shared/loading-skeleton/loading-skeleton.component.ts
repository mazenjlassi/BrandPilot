import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-loading-skeleton',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="skeleton-container" [class]="type">
      <ng-container [ngSwitch]="type">
        <ng-container *ngSwitchCase="'card'">
          <div class="skeleton-card">
            <div class="skeleton-line skeleton-thumbnail"></div>
            <div class="skeleton-line skeleton-title"></div>
            <div class="skeleton-line skeleton-text"></div>
            <div class="skeleton-line skeleton-text short"></div>
          </div>
        </ng-container>
        <ng-container *ngSwitchCase="'table-row'">
          <div class="skeleton-table-row">
            <div class="skeleton-line skeleton-cell"></div>
            <div class="skeleton-line skeleton-cell"></div>
            <div class="skeleton-line skeleton-cell short"></div>
            <div class="skeleton-line skeleton-cell-icon"></div>
          </div>
        </ng-container>
        <ng-container *ngSwitchDefault>
          <div class="skeleton-text-block">
            <div class="skeleton-line skeleton-title"></div>
            <div class="skeleton-line skeleton-text"></div>
            <div class="skeleton-line skeleton-text short"></div>
          </div>
        </ng-container>
      </ng-container>
    </div>
  `,
  styles: [`
    .skeleton-container {
      padding: 16px;
    }
    .skeleton-line {
      background: linear-gradient(90deg, rgba(255,255,255,0.06) 25%, rgba(255,255,255,0.12) 50%, rgba(255,255,255,0.06) 75%);
      background-size: 200% 100%;
      animation: shimmer 1.5s infinite;
      border-radius: 8px;
      margin-bottom: 12px;
    }
    body:not(.dark-mode) .skeleton-line {
      background: linear-gradient(90deg, rgba(0,0,0,0.06) 25%, rgba(0,0,0,0.1) 50%, rgba(0,0,0,0.06) 75%);
      background-size: 200% 100%;
    }
    .skeleton-thumbnail { height: 160px; width: 100%; border-radius: 12px; }
    .skeleton-title { height: 20px; width: 70%; }
    .skeleton-text { height: 14px; width: 100%; }
    .skeleton-text.short { width: 50%; }
    .skeleton-table-row {
      display: flex;
      gap: 16px;
      align-items: center;
      padding: 16px 0;
      border-bottom: 1px solid rgba(255,255,255,0.06);
    }
    body:not(.dark-mode) .skeleton-table-row {
      border-bottom: 1px solid rgba(0,0,0,0.08);
    }
    .skeleton-cell { height: 18px; flex: 1; }
    .skeleton-cell.short { width: 80px; flex: none; }
    .skeleton-cell-icon { width: 36px; height: 36px; border-radius: 8px; flex: none; }
    .skeleton-text-block { padding: 0; }
    .skeleton-card {
      background: var(--card);
      border: 1px solid var(--border);
      border-radius: 16px;
      padding: 20px;
    }
    @keyframes shimmer {
      0% { background-position: -200% 0; }
      100% { background-position: 200% 0; }
    }
    @media (prefers-reduced-motion: reduce) {
      .skeleton-line { animation: none; }
    }
  `]
})
export class LoadingSkeletonComponent {
  @Input() type: 'card' | 'table-row' | 'text' = 'text';
  @Input() count: number = 1;
}