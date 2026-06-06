import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { PlatformChartComponent } from './platform-chart.component';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController } from '@angular/common/http/testing';
import { PostService } from '../../../../services/post.service';

describe('PlatformChartComponent', () => {
  let component: PlatformChartComponent;
  let fixture: ComponentFixture<PlatformChartComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PlatformChartComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        PostService
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(PlatformChartComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    if (component.chart) {
      component.chart.destroy();
    }
  });

  it('creates_component', () => {
    expect(component).toBeTruthy();
  });

  it('loadData_fetches_stats', fakeAsync(() => {
    fixture.detectChanges();
    tick(100);

    const req = httpMock.expectOne('http://localhost:8081/posts/stats');
    expect(req.request.method).toBe('GET');
    req.flush({ facebookPosts: 5, instagramPosts: 3, linkedinPosts: 2 });

    expect(component.chart).toBeTruthy();
  }));

  it('ngOnDestroy_destroys_chart', fakeAsync(() => {
    fixture.detectChanges();
    tick(100);
    const req = httpMock.expectOne('http://localhost:8081/posts/stats');
    req.flush({ facebookPosts: 1, instagramPosts: 1, linkedinPosts: 1 });

    expect(component.chart).toBeTruthy();
    component.ngOnDestroy();
    expect(component.chart).toBeNull();
  }));
});
