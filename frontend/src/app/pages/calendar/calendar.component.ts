import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FullCalendarModule } from '@fullcalendar/angular';
import { CalendarOptions, EventClickArg } from '@fullcalendar/core';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction';
import { PostService } from '../../services/post.service';

@Component({
  selector: 'app-calendar',
  standalone: true,
  imports: [CommonModule, FullCalendarModule],
  templateUrl: './calendar.component.html',
  styleUrls: ['./calendar.component.css']
})
export class CalendarComponent implements OnInit, OnDestroy {
  calendarOptions: CalendarOptions = {
    plugins: [timeGridPlugin, interactionPlugin],
    initialView: 'timeGridWeek',
    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: ''
    },
    slotMinTime: '07:00:00',
    slotMaxTime: '23:59:00',
    slotDuration: '01:00:00',
    allDaySlot: false,
    nowIndicator: true,
    dayMaxEvents: 3,
    events: [],
    eventClick: this.handleEventClick.bind(this),
    height: '100%'
  };

  loading = true;
  private themeCheckInterval: any;

  constructor(
    private postService: PostService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadCalendarEvents();
    this.themeCheckInterval = setInterval(() => this.checkThemeChange(), 500);
  }

  ngOnDestroy() {
    if (this.themeCheckInterval) {
      clearInterval(this.themeCheckInterval);
    }
  }

  checkThemeChange() {
    const isDark = document.body.classList.contains('dark-mode');
    this.calendarOptions = {
      ...this.calendarOptions,
      themeSystem: 'standard'
    };
  }

  loadCalendarEvents() {
    const now = new Date();
    const start = new Date(now.getFullYear(), now.getMonth(), now.getDate() - 7);
    const end = new Date(now.getFullYear(), now.getMonth(), now.getDate() + 14);

    this.postService.getCalendarEvents(
      start.toISOString(),
      end.toISOString()
    ).subscribe({
      next: (events: any[]) => {
        const calendarEvents = events.map(event => this.mapToCalendarEvent(event));
        this.calendarOptions = {
          ...this.calendarOptions,
          events: calendarEvents
        };
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  mapToCalendarEvent(event: any): any {
    const platformColors: { [key: string]: string } = {
      'INSTAGRAM': '#E1306C',
      'FACEBOOK': '#1877F2',
      'LINKEDIN': '#0A66C2',
      'X': '#1DA1F2'
    };

    const statusBorders: { [key: string]: string } = {
      'DRAFT': '#6b7280',
      'SCHEDULED': '#f97316',
      'PUBLISHED': '#22c55e'
    };

    const platform = event.platform || 'UNKNOWN';
    const status = event.status || 'DRAFT';
    const color = platformColors[platform] || '#6b7280';

    const eventTime = event.scheduledAt || event.publishedAt;
    const displayTime = eventTime ? new Date(eventTime) : new Date();

    return {
      id: event.id.toString(),
      title: event.title || 'Untitled Post',
      start: displayTime,
      backgroundColor: color,
      borderColor: statusBorders[status] || '#6b7280',
      textColor: '#ffffff',
      extendedProps: {
        platform: platform,
        status: status,
        content: event.content,
        campaignName: event.campaignName
      }
    };
  }

  handleEventClick(clickInfo: EventClickArg) {
    const eventId = clickInfo.event.id;
    this.router.navigate(['/posts', eventId]);
  }
}