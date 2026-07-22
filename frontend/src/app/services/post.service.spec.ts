import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { PostService } from './post.service';
import { environment } from '../../environments/environment';

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
    const req = httpMock.expectOne(`${environment.apiUrl}/posts/latestPublished?limit=10`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('getDrafts_callsCorrectUrl', () => {
    service.getDrafts().subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/posts/drafts`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('getScheduled_callsCorrectUrl', () => {
    service.getScheduled().subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/posts/scheduled`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('getPublished_callsCorrectUrl', () => {
    service.getPublished().subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/posts/published`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('getPostStats_callsStats', () => {
    service.getPostStats().subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/posts/stats`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('updatePost_callsPut', () => {
    service.updatePost(1, { title: 'Updated' }).subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/posts/1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({ title: 'Updated' });
    req.flush('');
  });

  it('deletePost_callsDelete', () => {
    service.deletePost(1).subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/posts/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush('');
  });

  it('getCalendarEvents_callsWithDates', () => {
    service.getCalendarEvents('2024-01-01', '2024-01-31').subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/posts/calendar?start=2024-01-01&end=2024-01-31`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('uploadFile_callsPostWithFormData', () => {
    const file = new File(['fake'], 'test.png', { type: 'image/png' });
    service.uploadFile(file).subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/posts/upload`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body instanceof FormData).toBeTrue();
    req.flush({ url: 'https://img.com/test.png' });
  });

  it('generateImage_callsPost', () => {
    service.generateImage(1).subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/posts/1/generate-image`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({});
    req.flush({ imageUrl: 'https://img.com/ai.png' });
  });

  it('generateImage_withPrompt_sendsPrompt', () => {
    service.generateImage(1, 'a sunset').subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/posts/1/generate-image`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ prompt: 'a sunset' });
    req.flush({ imageUrl: 'https://img.com/ai.png' });
  });

  it('approvePost_callsPost', () => {
    service.approvePost(1).subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/posts/1/approve`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({});
    req.flush({ message: 'Post approved successfully' });
  });
});
