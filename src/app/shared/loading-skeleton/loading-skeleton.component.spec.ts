import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoadingSkeletonComponent } from './loading-skeleton.component';

describe('LoadingSkeletonComponent', () => {
  let component: LoadingSkeletonComponent;
  let fixture: ComponentFixture<LoadingSkeletonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoadingSkeletonComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(LoadingSkeletonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('creates_component', () => {
    expect(component).toBeTruthy();
  });

  it('default_type_is_text', () => {
    expect(component.type).toBe('text');
  });

  it('default_count_is_1', () => {
    expect(component.count).toBe(1);
  });

  it('accepts_card_type', () => {
    component.type = 'card';
    fixture.detectChanges();
    expect(component.type).toBe('card');
  });

  it('accepts_tableRow_type', () => {
    component.type = 'table-row';
    fixture.detectChanges();
    expect(component.type).toBe('table-row');
  });

  it('accepts_custom_count', () => {
    component.count = 3;
    fixture.detectChanges();
    expect(component.count).toBe(3);
  });
});
