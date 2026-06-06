import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ScrapedPostsComponent } from './scraped-posts.component';
import { provideRouter } from '@angular/router';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController } from '@angular/common/http/testing';
import { PatternService } from '../../services/pattern.service';
import { ConfirmDialogService } from '../../shared/confirm-dialog/confirm-dialog.service';

describe('ScrapedPostsComponent', () => {
  let component: ScrapedPostsComponent;
  let fixture: ComponentFixture<ScrapedPostsComponent>;
  let httpMock: HttpTestingController;
  let confirmService: ConfirmDialogService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ScrapedPostsComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        PatternService,
        ConfirmDialogService
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ScrapedPostsComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    confirmService = TestBed.inject(ConfirmDialogService);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('creates_component', () => {
    expect(component).toBeTruthy();
  });

  it('loads_companies_on_init', () => {
    fixture.detectChanges();
    const req = httpMock.expectOne('http://localhost:8081/api/company-profiles');
    req.flush([{ companyName: 'NVIDIA', instagramUrl: '', facebookUrl: '', linkedinUrl: '' }, { companyName: 'Google', instagramUrl: '', facebookUrl: '', linkedinUrl: '' }]);

    const postsReq = httpMock.expectOne('http://localhost:8081/api/scraped-posts?companyName=NVIDIA');
    postsReq.flush([]);

    expect(component.companies.length).toBe(2);
  });

  it('selects_first_company_after_load', () => {
    fixture.detectChanges();
    const companiesReq = httpMock.expectOne('http://localhost:8081/api/company-profiles');
    companiesReq.flush([{ companyName: 'NVIDIA', instagramUrl: '', facebookUrl: '', linkedinUrl: '' }, { companyName: 'Google', instagramUrl: '', facebookUrl: '', linkedinUrl: '' }]);

    const postsReq = httpMock.expectOne('http://localhost:8081/api/scraped-posts?companyName=NVIDIA');
    postsReq.flush([]);

    expect(component.selectedCompany).toBe('NVIDIA');
  });

  it('companyNames_returns_names_array', () => {
    component.companies = [{ companyName: 'NVIDIA', instagramUrl: '', facebookUrl: '', linkedinUrl: '' }, { companyName: 'Google', instagramUrl: '', facebookUrl: '', linkedinUrl: '' }];
    expect(component.companyNames).toEqual(['NVIDIA', 'Google']);
  });

  it('selectCompany_loads_scrapedPosts', () => {
    component.selectedCompany = 'NVIDIA';
    component.selectCompany('NVIDIA');

    const req = httpMock.expectOne('http://localhost:8081/api/scraped-posts?companyName=NVIDIA');
    req.flush([{ id: 1, postText: 'Test post' }]);

    expect(component.scrapedPosts.length).toBe(1);
  });

  it('openAddModal_initializes_newPost', () => {
    component.selectedCompany = 'NVIDIA';
    component.openAddModal();

    expect(component.showAddModal).toBeTrue();
    expect(component.newPost.companyName).toBe('NVIDIA');
    expect(component.newPost.platform).toBe('LINKEDIN');
    expect(component.newPost.postText).toBe('');
  });

  it('createPost_sends_payload', () => {
    component.newPost = { companyName: 'NVIDIA', platform: 'LINKEDIN', postText: 'Hello', postUrl: '', postedAt: '' };
    component.createPost();

    const req = httpMock.expectOne('http://localhost:8081/api/scraped-posts');
    expect(req.request.method).toBe('POST');
    expect(req.request.body.companyName).toBe('NVIDIA');
    req.flush({ id: 1, companyName: 'NVIDIA', postText: 'Hello' });

    const companiesReq = httpMock.expectOne('http://localhost:8081/api/company-profiles');
    companiesReq.flush([]);
    const postsReq = httpMock.expectOne('http://localhost:8081/api/scraped-posts?companyName=NVIDIA');
    postsReq.flush([]);

    expect(component.showAddModal).toBeFalse();
  });

  it('createPost_skips_when_text_empty', () => {
    component.newPost = { companyName: 'NVIDIA', platform: 'LINKEDIN', postText: '', postUrl: '', postedAt: '' };
    component.createPost();
    httpMock.expectNone('http://localhost:8081/api/scraped-posts');
  });

  it('launchScraper_triggers_scrape', () => {
    component.selectedCompany = 'NVIDIA';
    component.launchScraper();

    const req = httpMock.expectOne('http://localhost:8081/api/scraper/trigger?companyName=NVIDIA');
    expect(req.request.method).toBe('POST');
    req.flush({});

    const postsReq = httpMock.expectOne('http://localhost:8081/api/scraped-posts?companyName=NVIDIA');
    postsReq.flush([]);
  });

  it('deletePost_confirms_and_removes', (done) => {
    component.scrapedPosts = [{ id: 1, postText: 'A' }, { id: 2, postText: 'B' }];
    spyOn(confirmService, 'confirm').and.returnValue(Promise.resolve(true));

    component.deletePost(1).then(() => {
      const req = httpMock.expectOne('http://localhost:8081/api/scraped-posts/1');
      expect(req.request.method).toBe('DELETE');
      req.flush({});

      expect(component.scrapedPosts.length).toBe(1);
      expect(component.scrapedPosts[0].id).toBe(2);
      done();
    });
  });

  it('deleteCompany_confirms_and_removes', (done) => {
    component.companies = [
      { id: 1, companyName: 'NVIDIA', instagramUrl: '', facebookUrl: '', linkedinUrl: '' },
      { id: 2, companyName: 'Google', instagramUrl: '', facebookUrl: '', linkedinUrl: '' }
    ];
    component.selectedCompany = 'NVIDIA';
    spyOn(confirmService, 'confirm').and.returnValue(Promise.resolve(true));

    component.deleteCompany({ id: 1, companyName: 'NVIDIA', instagramUrl: '', facebookUrl: '', linkedinUrl: '' }).then(() => {
      const req = httpMock.expectOne('http://localhost:8081/api/company-profiles/1');
      expect(req.request.method).toBe('DELETE');
      req.flush({});

      expect(component.companies.length).toBe(1);
      expect(component.selectedCompany).toBe('Google');

      const postsReq = httpMock.expectOne('http://localhost:8081/api/scraped-posts?companyName=Google');
      postsReq.flush([]);

      done();
    });
  });

  it('getPlatformColor_returns_correct_hex', () => {
    expect(component.getPlatformColor('linkedin')).toBe('#0A66C2');
    expect(component.getPlatformColor('instagram')).toBe('#E4405F');
    expect(component.getPlatformColor('facebook')).toBe('#1877F2');
    expect(component.getPlatformColor('unknown')).toBe('#64748B');
  });
});
