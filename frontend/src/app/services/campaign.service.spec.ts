import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { CampaignService } from './campaign.service';

describe('CampaignService', () => {
  let service: CampaignService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        CampaignService
      ]
    });
    service = TestBed.inject(CampaignService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('createCampaign_callsPost', () => {
    service.createCampaign({ name: 'Test' }).subscribe();
    const req = httpMock.expectOne('http://localhost:8081/campaigns');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ name: 'Test' });
    req.flush({});
  });

  it('getAll_callsGet', () => {
    service.getAll().subscribe();
    const req = httpMock.expectOne('http://localhost:8081/campaigns');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('getById_callsWithId', () => {
    service.getById(5).subscribe();
    const req = httpMock.expectOne('http://localhost:8081/campaigns/5');
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('generateCampaign_callsGenerate', () => {
    service.generateCampaign({ topic: 'AI' }).subscribe();
    const req = httpMock.expectOne('http://localhost:8081/campaigns/generate');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ topic: 'AI' });
    req.flush({});
  });

  it('deleteCampaign_callsDelete', () => {
    service.deleteCampaign(3).subscribe();
    const req = httpMock.expectOne('http://localhost:8081/campaigns/3');
    expect(req.request.method).toBe('DELETE');
    req.flush('');
  });

  it('getCampaignPosts_callsWithCampaignId', () => {
    const mockPosts: any[] = [{ id: 1, content: 'post1' }];
    service.getCampaignPosts(2).subscribe((posts) => {
      expect(posts).toEqual(mockPosts);
    });
    const req = httpMock.expectOne('http://localhost:8081/campaigns/2/posts');
    expect(req.request.method).toBe('GET');
    req.flush(mockPosts);
  });

  it('getCampaignInsights_callsWithCampaignId', () => {
    const mockInsights = { likes: 100, shares: 20 };
    service.getCampaignInsights(3).subscribe((insights) => {
      expect(insights).toEqual(mockInsights);
    });
    const req = httpMock.expectOne('http://localhost:8081/insights/campaign/3');
    expect(req.request.method).toBe('GET');
    req.flush(mockInsights);
  });

  it('getRecent_usesDefaultLimit', () => {
    service.getRecent().subscribe();
    const req = httpMock.expectOne('http://localhost:8081/campaigns/recent?limit=5');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('getRecent_usesCustomLimit', () => {
    service.getRecent(10).subscribe();
    const req = httpMock.expectOne('http://localhost:8081/campaigns/recent?limit=10');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('generateForExisting_callsPostOnCampaign', () => {
    const mockPosts: any[] = [{ id: 1 }];
    service.generateForExisting(4).subscribe((posts) => {
      expect(posts).toEqual(mockPosts);
    });
    const req = httpMock.expectOne('http://localhost:8081/campaigns/4/generate');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({});
    req.flush(mockPosts);
  });
});
