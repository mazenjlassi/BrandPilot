import { TestBed } from '@angular/core/testing';
import { CommentService } from './comment.service';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

describe('CommentService', () => {
  let service: CommentService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), CommentService]
    });
    service = TestBed.inject(CommentService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('getByPost_callsCorrectUrl', () => {
    service.getByPost(1).subscribe();
    const req = httpMock.expectOne('http://localhost:8081/comments/post/1');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('getByPostAndSentiment_callsCorrectUrl', () => {
    service.getByPostAndSentiment(1, 'positive').subscribe();
    const req = httpMock.expectOne('http://localhost:8081/comments/post/1/sentiment/positive');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('getByCampaign_callsCorrectUrl', () => {
    service.getByCampaign(2).subscribe();
    const req = httpMock.expectOne('http://localhost:8081/comments/campaign/2');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });
});
