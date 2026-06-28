import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { PostService } from './post.service';

describe('PostService', () => {
  let service: PostService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        PostService
      ]
    });
    service = TestBed.inject(PostService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('getLatestPosts_callsCorrectUrl', () => {
    service.getLatestPosts(10).subscribe();
    const req = httpMock.expectOne('http://localhost:8081/posts/latestPublished?limit=10');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('getDrafts_callsCorrectUrl', () => {
    service.getDrafts().subscribe();
    const req = httpMock.expectOne('http://localhost:8081/posts/drafts');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('getScheduled_callsCorrectUrl', () => {
    service.getScheduled().subscribe();
    const req = httpMock.expectOne('http://localhost:8081/posts/scheduled');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('getPublished_callsCorrectUrl', () => {
    service.getPublished().subscribe();
    const req = httpMock.expectOne('http://localhost:8081/posts/published');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('getPostStats_callsStats', () => {
    service.getPostStats().subscribe();
    const req = httpMock.expectOne('http://localhost:8081/posts/stats');
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('updatePost_callsPut', () => {
    service.updatePost(1, { title: 'Updated' }).subscribe();
    const req = httpMock.expectOne('http://localhost:8081/posts/1');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({ title: 'Updated' });
    req.flush('');
  });

  it('deletePost_callsDelete', () => {
    service.deletePost(1).subscribe();
    const req = httpMock.expectOne('http://localhost:8081/posts/1');
    expect(req.request.method).toBe('DELETE');
    req.flush('');
  });

  it('getCalendarEvents_callsWithDates', () => {
    service.getCalendarEvents('2024-01-01', '2024-01-31').subscribe();
    const req = httpMock.expectOne('http://localhost:8081/posts/calendar?start=2024-01-01&end=2024-01-31');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('getTopPosts_usesDefaultLimit', () => {
    service.getTopPosts().subscribe();
    const req = httpMock.expectOne('http://localhost:8081/posts/top?limit=5');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('getTopPosts_usesCustomLimit', () => {
    service.getTopPosts(10).subscribe();
    const req = httpMock.expectOne('http://localhost:8081/posts/top?limit=10');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('getPermanent_callsCorrectUrl', () => {
    service.getPermanent().subscribe();
    const req = httpMock.expectOne('http://localhost:8081/posts/permanent');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('getByCampaign_callsWithCampaignId', () => {
    const mockPosts: any[] = [{ id: 1 }];
    service.getByCampaign(5).subscribe((posts) => {
      expect(posts).toEqual(mockPosts);
    });
    const req = httpMock.expectOne('http://localhost:8081/campaigns/5/posts');
    expect(req.request.method).toBe('GET');
    req.flush(mockPosts);
  });

  it('publishPost_callsPost', () => {
    service.publishPost(7).subscribe();
    const req = httpMock.expectOne('http://localhost:8081/publish/7');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({});
    req.flush({});
  });

  it('getById_callsWithId', () => {
    const mockPost = { id: 3, title: 'Test Post' };
    service.getById(3).subscribe((post) => {
      expect(post).toEqual(mockPost);
    });
    const req = httpMock.expectOne('http://localhost:8081/posts/3');
    expect(req.request.method).toBe('GET');
    req.flush(mockPost);
  });

  it('generateImage_callsPost', () => {
    service.generateImage(2).subscribe();
    const req = httpMock.expectOne('http://localhost:8081/posts/2/generate-image');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({});
    req.flush({});
  });

  it('generateImage_callsWithPrompt', () => {
    service.generateImage(2, 'sunset').subscribe();
    const req = httpMock.expectOne('http://localhost:8081/posts/2/generate-image');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ prompt: 'sunset' });
    req.flush({});
  });

  it('createPostWithImage_sendsFormData', () => {
    const data = { title: 'New Post', content: 'Hello' };
    const image = new File([''], 'test.png', { type: 'image/png' });

    service.createPostWithImage(1, data, image).subscribe();

    const req = httpMock.expectOne('http://localhost:8081/campaigns/1/posts/with-image');
    expect(req.request.method).toBe('POST');
    expect(req.request.body instanceof FormData).toBeTrue();
    expect(req.request.body.has('data')).toBeTrue();
    expect(req.request.body.has('image')).toBeTrue();
    req.flush({});
  });

  it('createPostWithImage_withoutImage', () => {
    service.createPostWithImage(1, { title: 'No Image' }).subscribe();

    const req = httpMock.expectOne('http://localhost:8081/campaigns/1/posts/with-image');
    expect(req.request.method).toBe('POST');
    expect(req.request.body instanceof FormData).toBeTrue();
    expect(req.request.body.has('data')).toBeTrue();
    expect(req.request.body.has('image')).toBeFalse();
    req.flush({});
  });

  it('getTimingAnalysis_callsCorrectUrl', () => {
    const mockData = { bestTimes: ['09:00'] };
    service.getTimingAnalysis().subscribe((data) => {
      expect(data).toEqual(mockData);
    });
    const req = httpMock.expectOne('http://localhost:8081/posts/timing-analysis');
    expect(req.request.method).toBe('GET');
    req.flush(mockData);
  });

  it('getWeeklyComparison_callsCorrectUrl', () => {
    const mockData = { thisWeek: 10, lastWeek: 7 };
    service.getWeeklyComparison().subscribe((data) => {
      expect(data).toEqual(mockData);
    });
    const req = httpMock.expectOne('http://localhost:8081/posts/weekly-comparison');
    expect(req.request.method).toBe('GET');
    req.flush(mockData);
  });

  it('getUpcomingScheduled_usesDefaultLimit', () => {
    service.getUpcomingScheduled().subscribe();
    const req = httpMock.expectOne('http://localhost:8081/posts/upcoming-scheduled?limit=3');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('getUpcomingScheduled_usesCustomLimit', () => {
    service.getUpcomingScheduled(5).subscribe();
    const req = httpMock.expectOne('http://localhost:8081/posts/upcoming-scheduled?limit=5');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });
});
