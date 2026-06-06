import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EmptyStateComponent } from './empty-state.component';
import { Inbox, SearchX, FolderOpen, FileX, MessageSquareOff, CalendarX } from 'lucide-angular';

describe('EmptyStateComponent', () => {
  let component: EmptyStateComponent;
  let fixture: ComponentFixture<EmptyStateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EmptyStateComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(EmptyStateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('creates_component', () => {
    expect(component).toBeTruthy();
  });

  it('defaults_to_inbox_icon', () => {
    expect(component.icon).toBe('inbox');
  });

  it('defaults_heading', () => {
    expect(component.heading).toBe('Nothing here yet');
  });

  it('defaults_message_to_empty', () => {
    expect(component.message).toBe('');
  });

  it('accepts_custom_inputs', () => {
    component.icon = 'search-x';
    component.heading = 'Custom Heading';
    component.message = 'Custom message';
    fixture.detectChanges();

    expect(component.icon).toBe('search-x');
    expect(component.heading).toBe('Custom Heading');
    expect(component.message).toBe('Custom message');
  });

  it('iconMap_has_all_keys', () => {
    expect(component.iconMap['inbox']).toBe(Inbox);
    expect(component.iconMap['search-x']).toBe(SearchX);
    expect(component.iconMap['folder-open']).toBe(FolderOpen);
    expect(component.iconMap['file-x']).toBe(FileX);
    expect(component.iconMap['message-off']).toBe(MessageSquareOff);
    expect(component.iconMap['calendar-x']).toBe(CalendarX);
  });
});
