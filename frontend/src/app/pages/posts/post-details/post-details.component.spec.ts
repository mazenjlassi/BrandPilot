import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { PostDetailsComponent } from './post-details.component';
import { provideRouter } from '@angular/router';
import { ActivatedRoute } from '@angular/router';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { PostService } from '../../../services/post.service';
import { CommentService } from '../../../services/comment.service';
import { ConfirmDialogService } from '../../../shared/confirm-dialog/confirm-dialog.service';
import { Location } from '@angular/common';

describe('PostDetailsComponent', () => {
  let component: PostDetailsComponent;
  let fixture: ComponentFixture<PostDetailsComponent>;
  let httpMock: HttpTestingController;
  let confirmSpy: jasmine.SpyObj<ConfirmDialogService>;
  let locationSpy: jasmine.SpyObj<Location>;

  const mockPost = {
    id: 1,
    title: 'Test Post',
    content: 'Test content',
    status: 'DRAFT',
    platform: 'INSTAGRAM',
    hashtags: '#test',
    approved: false,
    scheduledAt: '2026-06-10T12:00:00.000Z',
    createdAt: '2026-06-01T10:00:00.000Z',
    imageUrl: null,
    likes: 0,
    commentsCount: 0,
    shares: 0
  };

  beforeEach(async () => {
    confirmSpy = jasmine.createSpyObj('ConfirmDialogService', ['confirm']);
    locationSpy = jasmine.createSpyObj('Location', ['back']);

    await TestBed.configureTestingModule({
      imports: [PostDetailsComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: { get: () => '1' } } } },
        { provide: ConfirmDialogService, useValue: confirmSpy },
        { provide: Location, useValue: locationSpy },
        PostService, CommentService
      ]
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(PostDetailsComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load post on init', () => {
    fixture.detectChanges();

    const req = httpMock.expectOne('http://localhost:8081/posts/1');
    expect(req.request.method).toBe('GET');
    req.flush(mockPost);

    expect(component.post).toEqual(mockPost);
    expect(component.loading).toBeFalse();
  });

  it('should handle load post error', () => {
    fixture.detectChanges();

    const req = httpMock.expectOne('http://localhost:8081/posts/1');
    req.flush('Error', { status: 500, statusText: 'Server Error' });

    expect(component.errorMessage).toBe('Failed to load post');
    expect(component.loading).toBeFalse();
  });

  it('should load comments and metrics for PUBLISHED post', () => {
    const publishedPost = { ...mockPost, status: 'PUBLISHED' };
    fixture.detectChanges();

    const postReq = httpMock.expectOne('http://localhost:8081/posts/1');
    postReq.flush(publishedPost);

    const commentReq = httpMock.expectOne('http://localhost:8081/comments/post/1');
    expect(commentReq.request.method).toBe('GET');
    commentReq.flush([{ id: 1, commentText: 'Nice!', sentiment: 'POSITIVE' }]);

    const metricsReq = httpMock.expectOne('http://localhost:8081/metrics/post/1');
    expect(metricsReq.request.method).toBe('GET');
    metricsReq.flush([{ collectedAt: '2026-06-01T10:00:00Z', likes: 10, comments: 2 }]);

    expect(component.comments.length).toBe(1);
    expect(component.metrics.length).toBe(1);
  });

  it('should not load comments for non-PUBLISHED post', () => {
    fixture.detectChanges();

    const req = httpMock.expectOne('http://localhost:8081/posts/1');
    req.flush(mockPost);

    httpMock.expectNone('http://localhost:8081/comments/post/1');
    httpMock.expectNone('http://localhost:8081/metrics/post/1');
  });

  it('formatDatetimeLocal should format date string', () => {
    const result = component.formatDatetimeLocal('2026-06-10T14:30:00.000Z');
    expect(result).toMatch(/2026-06-10T\d{2}:30/);
  });

  it('formatDatetimeLocal should return empty for falsy input', () => {
    expect(component.formatDatetimeLocal(null)).toBe('');
    expect(component.formatDatetimeLocal('')).toBe('');
  });

  it('formatDatetimeLocal should return empty for invalid date', () => {
    expect(component.formatDatetimeLocal('not-a-date')).toBe('');
  });

  it('canUpdate should return true for non-PUBLISHED', () => {
    expect(component.canUpdate()).toBeTrue();
  });

  it('canUpdate should return false for PUBLISHED', () => {
    component.post = { status: 'PUBLISHED' };
    expect(component.canUpdate()).toBeFalse();
  });

  it('openEdit should set editMode and clear messages', () => {
    component.editMode = false;
    component.errorMessage = 'err';
    component.successMessage = 'ok';
    component.openEdit();
    expect(component.editMode).toBeTrue();
    expect(component.errorMessage).toBe('');
    expect(component.successMessage).toBe('');
  });

  it('updatePost should send PUT request', () => {
    component.post = { ...mockPost };
    component.scheduledDate = '2026-06-10T14:30';
    component.updatePost();

    const req = httpMock.expectOne('http://localhost:8081/posts/1');
    expect(req.request.method).toBe('PUT');
    req.flush('Updated');

    expect(component.successMessage).toBe('Post updated successfully');
    expect(component.editMode).toBeFalse();
    expect(component.saving).toBeFalse();
  });

  it('updatePost should handle error', () => {
    component.post = { ...mockPost, id: 1 };
    component.updatePost();

    const req = httpMock.expectOne('http://localhost:8081/posts/1');
    req.flush('Error', { status: 400, statusText: 'Bad Request' });

    expect(component.saving).toBeFalse();
    expect(component.errorMessage).toBeTruthy();
  });

  it('deletePost should confirm then delete', async () => {
    confirmSpy.confirm.and.resolveTo(true);
    component.post = { id: 1 };

    await component.deletePost();

    const req = httpMock.expectOne('http://localhost:8081/posts/1');
    expect(req.request.method).toBe('DELETE');
    req.flush('Deleted');
  });

  it('deletePost should skip if not confirmed', async () => {
    confirmSpy.confirm.and.resolveTo(false);
    component.post = { id: 1 };

    await component.deletePost();

    httpMock.expectNone('http://localhost:8081/posts/1');
  });

  it('generateImage should POST and update imageUrl', () => {
    component.post = { id: 1 };
    component.generateImage();

    const req = httpMock.expectOne('http://localhost:8081/posts/1/generate-image');
    expect(req.request.method).toBe('POST');
    req.flush({ imageUrl: 'http://example.com/img.png' });

    expect(component.post.imageUrl).toBe('http://example.com/img.png');
    expect(component.generatingImage).toBeFalse();
    expect(component.successMessage).toBe('Image generated successfully');
  });

  it('generateImage should handle error', () => {
    component.post = { id: 1 };
    component.generateImage();

    const req = httpMock.expectOne('http://localhost:8081/posts/1/generate-image');
    req.flush('Error', { status: 500, statusText: 'Server Error' });

    expect(component.generatingImage).toBeFalse();
    expect(component.errorMessage).toBe('Image generation failed');
  });

  it('loadComments should fetch comments', () => {
    component.loadComments(1);

    const req = httpMock.expectOne('http://localhost:8081/comments/post/1');
    req.flush([{ id: 1, commentText: 'Great!' }]);

    expect(component.comments.length).toBe(1);
    expect(component.loadingComments).toBeFalse();
  });

  it('filterSentiment with empty should reload all comments', () => {
    component.post = { id: 1 };
    component.filterSentiment('');

    const req = httpMock.expectOne('http://localhost:8081/comments/post/1');
    req.flush([]);
  });

  it('filterSentiment with value should filter by sentiment', () => {
    component.post = { id: 1 };
    component.filterSentiment('POSITIVE');

    const req = httpMock.expectOne('http://localhost:8081/comments/post/1/sentiment/POSITIVE');
    req.flush([{ id: 1, commentText: 'Great!', sentiment: 'POSITIVE' }]);

    expect(component.sentimentFilter).toBe('POSITIVE');
    expect(component.loadingComments).toBeFalse();
  });

  it('filterSentiment should skip if no post id', () => {
    component.post = null;
    component.filterSentiment('POSITIVE');
    httpMock.expectNone('http://localhost:8081/comments/post/1/sentiment/POSITIVE');
  });

  it('resetComments should reload comments', () => {
    component.post = { id: 1 };
    component.resetComments();

    const req = httpMock.expectOne('http://localhost:8081/comments/post/1');
    req.flush([]);
  });

  it('getSentimentClass should return correct class', () => {
    expect(component.getSentimentClass('POSITIVE')).toBe('positive');
    expect(component.getSentimentClass('NEGATIVE')).toBe('negative');
    expect(component.getSentimentClass('NEUTRAL')).toBe('neutral');
    expect(component.getSentimentClass('')).toBe('neutral');
  });

  it('goBack should call location.back', () => {
    component.goBack();
    expect(locationSpy.back).toHaveBeenCalled();
  });

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
