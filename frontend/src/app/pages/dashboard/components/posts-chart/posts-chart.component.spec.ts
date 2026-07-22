import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { PostsChartComponent } from './posts-chart.component';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { PostService } from '../../../../services/post.service';
import { environment } from '../../../../../environments/environment';

describe('PostsChartComponent', () => {
  let component: PostsChartComponent;
  let fixture: ComponentFixture<PostsChartComponent>;
  let httpMock: HttpTestingController;

  const mockPosts = [
    { id: 1, title: 'Post 1', likes: 10, commentsCount: 2, publishedAt: '2026-06-10T10:00:00Z' },
    { id: 2, title: 'Post 2', likes: 20, commentsCount: 5, publishedAt: '2026-06-09T10:00:00Z' },
    { id: 3, title: 'Post 3', likes: 5, commentsCount: 1, publishedAt: '2026-06-08T10:00:00Z' },
    { id: 4, title: 'Post 4', likes: 0, commentsCount: 0, publishedAt: '2026-06-07T10:00:00Z' }
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PostsChartComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        PostService
      ]
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(PostsChartComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set empty=true when no posts returned', fakeAsync(() => {
    fixture.detectChanges();
    tick(100);

    const req = httpMock.expectOne(`${environment.apiUrl}/posts/latestPublished?limit=20`);
    req.flush([]);

    expect(component.empty).toBeTrue();
  }));

  it('should set empty=true when posts have no engagement data', fakeAsync(() => {
    fixture.detectChanges();
    tick(100);

    const req = httpMock.expectOne(`${environment.apiUrl}/posts/latestPublished?limit=20`);
    req.flush(mockPosts.slice(3));

    expect(component.empty).toBeTrue();
  }));

  it('should create chart when posts have engagement data', fakeAsync(() => {
    const chartSpy = jasmine.createSpy('Chart');
    const origChart = (window as any).Chart;
    (window as any).Chart = jasmine.createSpy().and.callFake(() => chartSpy);

    fixture.detectChanges();
    tick(100);

    const req = httpMock.expectOne(`${environment.apiUrl}/posts/latestPublished?limit=20`);
    req.flush(mockPosts);

    expect(component.chart).toBeTruthy();
    expect(component.empty).toBeFalse();

    (window as any).Chart = origChart;
  }));

  it('ngOnDestroy should destroy chart if present', () => {
    const destroySpy = jasmine.createSpy('destroy');
    component.chart = { destroy: destroySpy };
    component.ngOnDestroy();
    expect(destroySpy).toHaveBeenCalled();
  });

  it('ngOnDestroy should not fail without chart', () => {
    component.chart = null;
    expect(() => component.ngOnDestroy()).not.toThrow();
  });
});
