import { TestBed } from '@angular/core/testing';
import { InsightService } from './insight.service';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

describe('InsightService', () => {
  let service: InsightService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), InsightService]
    });
    service = TestBed.inject(InsightService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('getByCampaign_callsCorrectUrl', () => {
    service.getByCampaign(1).subscribe();
    const req = httpMock.expectOne('http://localhost:8081/insights/campaign/1');
    expect(req.request.method).toBe('GET');
    req.flush({});
  });
});
