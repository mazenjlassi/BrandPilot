import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { StrategyService } from './strategy.service';

describe('StrategyService', () => {
  let service: StrategyService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        StrategyService
      ]
    });
    service = TestBed.inject(StrategyService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('getAll_callsCorrectUrl', () => {
    service.getAll().subscribe();
    const req = httpMock.expectOne('http://localhost:8081/marketing-strategies');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('getActive_callsCorrectUrl', () => {
    service.getActive().subscribe();
    const req = httpMock.expectOne('http://localhost:8081/marketing-strategies/active');
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('getById_callsCorrectUrl', () => {
    service.getById(1).subscribe();
    const req = httpMock.expectOne('http://localhost:8081/marketing-strategies/1');
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('generate_callsPostWithBody', () => {
    service.generate({ topic: 'AI', durationWeeks: 8 }).subscribe();
    const req = httpMock.expectOne('http://localhost:8081/marketing-strategies/generate');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ topic: 'AI', durationWeeks: 8 });
    req.flush({});
  });

  it('generateAuto_callsPost', () => {
    service.generateAuto().subscribe();
    const req = httpMock.expectOne('http://localhost:8081/marketing-strategies/generate-auto');
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('setAutoGenerate_callsPut', () => {
    service.setAutoGenerate(1, true).subscribe();
    const req = httpMock.expectOne('http://localhost:8081/marketing-strategies/1/auto-generate');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({ autoGenerate: true });
    req.flush({});
  });

  it('getStrategyCampaigns_callsCorrectUrl', () => {
    service.getStrategyCampaigns(1).subscribe();
    const req = httpMock.expectOne('http://localhost:8081/marketing-strategies/1/campaigns');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('generateWeek_callsPost', () => {
    service.generateWeek(1).subscribe();
    const req = httpMock.expectOne('http://localhost:8081/marketing-strategies/1/generate-week');
    expect(req.request.method).toBe('POST');
    req.flush([]);
  });
});
