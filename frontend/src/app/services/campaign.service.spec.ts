import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { CampaignService } from './campaign.service';
import { environment } from '../../environments/environment';

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
    const req = httpMock.expectOne(`${environment.apiUrl}/campaigns`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ name: 'Test' });
    req.flush({});
  });

  it('getAll_callsGet', () => {
    service.getAll().subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/campaigns`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('getById_callsWithId', () => {
    service.getById(5).subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/campaigns/5`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('generateCampaign_callsGenerate', () => {
    service.generateCampaign({ topic: 'AI' }).subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/campaigns/generate`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ topic: 'AI' });
    req.flush({});
  });

  it('deleteCampaign_callsDelete', () => {
    service.deleteCampaign(3).subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/campaigns/3`);
    expect(req.request.method).toBe('DELETE');
    req.flush('');
  });
});
