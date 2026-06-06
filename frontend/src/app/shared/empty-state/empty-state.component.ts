import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule, Inbox, SearchX, FolderOpen, FileX, MessageSquareOff, CalendarX } from 'lucide-angular';

@Component({
  selector: 'app-empty-state',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  template: `
    <div class="empty-state">
      <lucide-icon [img]="iconMap[icon]" [size]="48" class="empty-icon"></lucide-icon>
      <h4>{{ heading }}</h4>
      <p>{{ message }}</p>
      <ng-content></ng-content>
    </div>
  `,
  styles: [`
    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 64px 24px;
      text-align: center;
      gap: 12px;
    }
    .empty-icon {
      color: rgba(255,255,255,0.15);
      margin-bottom: 8px;
    }
    body:not(.dark-mode) .empty-icon {
      color: rgba(0,0,0,0.15);
    }
    .empty-state h4 {
      margin: 0;
      font-size: var(--text-lg);
      font-weight: var(--font-semibold);
      color: var(--text-primary);
    }
    .empty-state p {
      margin: 0;
      font-size: var(--text-sm);
      color: var(--text-secondary);
      max-width: 400px;
      line-height: 1.5;
    }
  `]
})
export class EmptyStateComponent {
  @Input() icon: 'inbox' | 'search-x' | 'folder-open' | 'file-x' | 'message-off' | 'calendar-x' = 'inbox';
  @Input() heading = 'Nothing here yet';
  @Input() message = '';

  iconMap: Record<string, any> = {
    'inbox': Inbox,
    'search-x': SearchX,
    'folder-open': FolderOpen,
    'file-x': FileX,
    'message-off': MessageSquareOff,
    'calendar-x': CalendarX
  };
}