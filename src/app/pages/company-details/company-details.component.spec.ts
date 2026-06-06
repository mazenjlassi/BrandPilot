import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CompanyDetailsComponent } from './company-details.component';
import { provideRouter } from '@angular/router';
import { ActivatedRoute, Router } from '@angular/router';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { of } from 'rxjs';
import { PatternService } from '../../services/pattern.service';
import { ToastService } from '../../shared/toast/toast.service';
import { ConfirmDialogService } from '../../shared/confirm-dialog/confirm-dialog.service';

describe('CompanyDetailsComponent', () => {
  let component: CompanyDetailsComponent;
  let fixture: ComponentFixture<CompanyDetailsComponent>;
  let httpMock: HttpTestingController;
  let confirmService: ConfirmDialogService;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CompanyDetailsComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: ActivatedRoute,
          useValue: { paramMap: of({ get: (key: string) => key === 'companyName' ? 'NVIDIA' : null }) }
        },
        PatternService,
        ToastService,
        ConfirmDialogService
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CompanyDetailsComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    confirmService = TestBed.inject(ConfirmDialogService);
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('creates_component', () => {
    expect(component).toBeTruthy();
  });

  it('loads_all_data_on_init', () => {
    fixture.detectChanges();
    const profileReq = httpMock.expectOne('http://localhost:8081/api/company-profiles/by-name/NVIDIA');
    profileReq.flush({ id: 1, companyName: 'NVIDIA', instagramUrl: '', facebookUrl: '', linkedinUrl: '' });

    const postsReq = httpMock.expectOne('http://localhost:8081/api/scraped-posts?companyName=NVIDIA');
    postsReq.flush([{ id: 1, postText: 'Test' }]);

    const patternsReq = httpMock.expectOne('http://localhost:8081/api/patterns?companyName=NVIDIA');
    patternsReq.flush([]);

    expect(component.companyName).toBe('NVIDIA');
    expect(component.scrapedPosts.length).toBe(1);
    expect(component.patterns.length).toBe(0);
  });

  it('loadProfile_sets_editForm', () => {
    component.companyName = 'NVIDIA';
    component.loadProfile();

    const req = httpMock.expectOne('http://localhost:8081/api/company-profiles/by-name/NVIDIA');
    req.flush({ id: 1, companyName: 'NVIDIA', instagramUrl: 'https://instagram.com/nvidia', facebookUrl: '', linkedinUrl: '' });

    expect(component.editForm.instagramUrl).toBe('https://instagram.com/nvidia');
  });

  it('saveProfile_calls_update', () => {
    component.companyName = 'NVIDIA';
    component.profile = { id: 1, companyName: 'NVIDIA', instagramUrl: '', facebookUrl: '', linkedinUrl: '' };
    component.editForm = { instagramUrl: 'https://instagram.com/nvidia', facebookUrl: '', linkedinUrl: '' };

    component.saveProfile();

    const req = httpMock.expectOne('http://localhost:8081/api/company-profiles/1');
    expect(req.request.method).toBe('PUT');
    req.flush({ id: 1, companyName: 'NVIDIA' });

    const getReq = httpMock.expectOne('http://localhost:8081/api/company-profiles/by-name/NVIDIA');
    getReq.flush({ id: 1, companyName: 'NVIDIA', instagramUrl: '', facebookUrl: '', linkedinUrl: '' });

    expect(component.saving).toBeFalse();
  });

  it('launchScraper_triggers_and_reloads', () => {
    component.companyName = 'NVIDIA';
    component.launchScraper();

    const scrapeReq = httpMock.expectOne('http://localhost:8081/api/scraper/trigger?companyName=NVIDIA');
    scrapeReq.flush({});

    const postsReq = httpMock.expectOne('http://localhost:8081/api/scraped-posts?companyName=NVIDIA');
    postsReq.flush([]);

    expect(component.scraperLoading).toBeFalse();
  });

  it('deletePost_confirms_and_removes', (done) => {
    component.scrapedPosts = [{ id: 1, postText: 'A' }, { id: 2, postText: 'B' }];
    spyOn(confirmService, 'confirm').and.returnValue(Promise.resolve(true));

    component.deletePost(1).then(() => {
      const req = httpMock.expectOne('http://localhost:8081/api/scraped-posts/1');
      req.flush({});

      expect(component.scrapedPosts.length).toBe(1);
      done();
    });
  });

  it('deleteCompany_confirms_and_navigates', (done) => {
    component.profile = { id: 1, companyName: 'NVIDIA', instagramUrl: '', facebookUrl: '', linkedinUrl: '' };
    component.companyName = 'NVIDIA';
    spyOn(confirmService, 'confirm').and.returnValue(Promise.resolve(true));
    const routerSpy = spyOn(router, 'navigate');

    component.deleteCompany().then(() => {
      const req = httpMock.expectOne('http://localhost:8081/api/company-profiles/1');
      req.flush({});

      expect(routerSpy).toHaveBeenCalledWith(['/scraped-posts']);
      done();
    });
  });

  it('toggleExpand_expands_and_collapses', () => {
    component.expandedPatternId = 1;
    component.toggleExpand(1);
    expect(component.expandedPatternId).toBeNull();

    component.toggleExpand(2);
    expect(component.expandedPatternId).toBe(2);
  });

  it('getScoreColor_returns_correct', () => {
    expect(component.getScoreColor(0.05)).toBe('#22C55E');
    expect(component.getScoreColor(0.02)).toBe('#EAB308');
    expect(component.getScoreColor(0.01)).toBe('#EF4444');
  });

  it('getScoreLabel_returns_correct', () => {
    expect(component.getScoreLabel(0.05)).toBe('High');
    expect(component.getScoreLabel(0.02)).toBe('Medium');
    expect(component.getScoreLabel(0)).toBe('Low');
  });

  it('getPlatformColor_returns_correct', () => {
    expect(component.getPlatformColor('linkedin')).toBe('#0A66C2');
    expect(component.getPlatformColor('instagram')).toBe('#E4405F');
    expect(component.getPlatformColor('facebook')).toBe('#1877F2');
    expect(component.getPlatformColor('unknown')).toBe('#64748B');
  });

  it('goBack_navigates_to_scrapedPosts', () => {
    const routerSpy = spyOn(router, 'navigate');
    component.goBack();
    expect(routerSpy).toHaveBeenCalledWith(['/scraped-posts']);
  });
});
