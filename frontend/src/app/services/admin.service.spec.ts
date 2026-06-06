import { TestBed } from '@angular/core/testing';
import { AdminService } from './admin.service';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

describe('AdminService', () => {
  let service: AdminService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), AdminService]
    });
    service = TestBed.inject(AdminService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('getUserStats_callsCorrectUrl', () => {
    service.getUserStats().subscribe();
    const req = httpMock.expectOne('http://localhost:8081/admin/stats');
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('getCampaignsProgress_callsWithLimit', () => {
    service.getCampaignsProgress(5).subscribe();
    const req = httpMock.expectOne('http://localhost:8081/admin/campaigns/progress?limit=5');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });
});
