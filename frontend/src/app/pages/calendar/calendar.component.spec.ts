import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CalendarComponent } from './calendar.component';
import { provideRouter } from '@angular/router';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController } from '@angular/common/http/testing';
import { PostService } from '../../services/post.service';

describe('CalendarComponent', () => {
  let component: CalendarComponent;
  let fixture: ComponentFixture<CalendarComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CalendarComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        PostService
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CalendarComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
    httpMock.expectOne((req) => req.url.includes('/posts/calendar')).flush([]);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('creates_component', () => {
    expect(component).toBeTruthy();
  });

  it('loads_events_on_init', () => {
    expect(component.loading).toBeFalse();
  });

  it('mapToCalendarEvent_creates_correct_structure', () => {
    const event = { id: 1, title: 'My Post', platform: 'LINKEDIN', status: 'SCHEDULED', scheduledAt: '2026-06-01T10:00:00Z' };
    const result = component.mapToCalendarEvent(event);

    expect(result.id).toBe('1');
    expect(result.title).toBe('My Post');
    expect(result.extendedProps.platform).toBe('LINKEDIN');
    expect(result.extendedProps.status).toBe('SCHEDULED');
  });

  it('mapToCalendarEvent_applies_platform_colors', () => {
    expect(component.mapToCalendarEvent({ id: 1, platform: 'INSTAGRAM', status: 'DRAFT', scheduledAt: null }).backgroundColor).toBe('#E1306C');
    expect(component.mapToCalendarEvent({ id: 2, platform: 'FACEBOOK', status: 'DRAFT', scheduledAt: null }).backgroundColor).toBe('#1877F2');
    expect(component.mapToCalendarEvent({ id: 3, platform: 'LINKEDIN', status: 'DRAFT', scheduledAt: null }).backgroundColor).toBe('#0A66C2');
    expect(component.mapToCalendarEvent({ id: 4, platform: 'X', status: 'DRAFT', scheduledAt: null }).backgroundColor).toBe('#1DA1F2');
  });

  it('mapToCalendarEvent_applies_status_borders', () => {
    expect(component.mapToCalendarEvent({ id: 1, platform: 'LINKEDIN', status: 'DRAFT', scheduledAt: null }).borderColor).toBe('#6b7280');
    expect(component.mapToCalendarEvent({ id: 2, platform: 'LINKEDIN', status: 'SCHEDULED', scheduledAt: null }).borderColor).toBe('#f97316');
    expect(component.mapToCalendarEvent({ id: 3, platform: 'LINKEDIN', status: 'PUBLISHED', scheduledAt: null }).borderColor).toBe('#22c55e');
  });

  it('mapToCalendarEvent_uses_publishedAt_when_no_scheduledAt', () => {
    const result = component.mapToCalendarEvent({ id: 1, platform: 'LINKEDIN', status: 'PUBLISHED', publishedAt: '2026-05-01T10:00:00Z', scheduledAt: null });
    expect(result.start).toBeDefined();
  });

  it('ngOnDestroy_clears_interval', () => {
    const spy = spyOn(window, 'clearInterval');
    component.ngOnDestroy();
    expect(spy).toHaveBeenCalled();
  });

  it('ngOnDestroy_skips_clear_when_no_interval', () => {
    component.ngOnDestroy();
    (component as any).themeCheckInterval = null;
    const spy = spyOn(window, 'clearInterval');
    component.ngOnDestroy();
    expect(spy).not.toHaveBeenCalled();
  });
});
