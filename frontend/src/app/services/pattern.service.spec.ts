import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { PatternService } from './pattern.service';

describe('PatternService', () => {
  let service: PatternService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), PatternService]
    });
    service = TestBed.inject(PatternService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('getScrapedPosts_callsCorrectUrl', () => {
    service.getScrapedPosts().subscribe();
    const req = httpMock.expectOne('http://localhost:8081/api/scraped-posts');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('getScrapedPosts_withCompanyName', () => {
    service.getScrapedPosts('NVIDIA').subscribe();
    const req = httpMock.expectOne('http://localhost:8081/api/scraped-posts?companyName=NVIDIA');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('triggerScrape_callsPost', () => {
    service.triggerScrape('NVIDIA').subscribe();
    const req = httpMock.expectOne('http://localhost:8081/api/scraper/trigger?companyName=NVIDIA');
    expect(req.request.method).toBe('POST');
    req.flush({});
  });
});
