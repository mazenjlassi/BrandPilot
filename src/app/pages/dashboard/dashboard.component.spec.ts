import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { DashboardComponent } from './dashboard.component';
import { provideRouter } from '@angular/router';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { PostService } from '../../services/post.service';
import { CampaignService } from '../../services/campaign.service';
import { AuthService } from '../../services/auth.service';
import { AdminService } from '../../services/admin.service';
import { PatternService } from '../../services/pattern.service';

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let httpMock: HttpTestingController;
  let authService: AuthService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        PostService,
        CampaignService,
        AuthService,
        AdminService,
        PatternService
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    authService = TestBed.inject(AuthService);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('creates_component', () => {
    expect(component).toBeTruthy();
  });

  it('loads_basic_data_on_init', () => {
    fixture.detectChanges();
    const postsReq = httpMock.expectOne('http://localhost:8081/posts/top?limit=5');
    postsReq.flush([]);

    const statsReq = httpMock.expectOne('http://localhost:8081/posts/stats');
    statsReq.flush({ totalPosts: 10, publishedPosts: 5, campaigns: 3 });

    const campaignsReq = httpMock.expectOne('http://localhost:8081/campaigns');
    campaignsReq.flush([]);

    expect(component.stats.totalPosts).toBe(10);
    expect(component.stats.publishedPosts).toBe(5);
    expect(component.stats.activeCampaigns).toBe(3);
  });

  it('loads_marketing_features_when_marketing_user', () => {
    spyOn(authService, 'isMarketing').and.returnValue(true);
    spyOn(authService, 'isAdmin').and.returnValue(false);

    component.ngOnInit();

    const postsReq = httpMock.expectOne('http://localhost:8081/posts/top?limit=5');
    postsReq.flush([]);

    const statsReq = httpMock.expectOne('http://localhost:8081/posts/stats');
    statsReq.flush({});

    const campaignsReq = httpMock.expectOne('http://localhost:8081/campaigns');
    campaignsReq.flush([]);

    const timingReq = httpMock.expectOne('http://localhost:8081/posts/timing-analysis');
    timingReq.flush({});

    const weeklyReq = httpMock.expectOne('http://localhost:8081/posts/weekly-comparison');
    weeklyReq.flush({});

    const upcomingReq = httpMock.expectOne('http://localhost:8081/posts/upcoming-scheduled?limit=3');
    upcomingReq.flush([]);

    expect(component.showMarketing).toBeTrue();
  });

  it('loads_admin_features_when_admin_user', () => {
    spyOn(authService, 'isMarketing').and.returnValue(false);
    spyOn(authService, 'isAdmin').and.returnValue(true);

    component.ngOnInit();

    const postsReq = httpMock.expectOne('http://localhost:8081/posts/top?limit=5');
    postsReq.flush([]);

    const statsReq = httpMock.expectOne('http://localhost:8081/posts/stats');
    statsReq.flush({});

    const campaignsReq = httpMock.expectOne('http://localhost:8081/campaigns');
    campaignsReq.flush([]);

    const userStatsReq = httpMock.expectOne('http://localhost:8081/admin/stats');
    userStatsReq.flush({});

    const progressReq = httpMock.expectOne('http://localhost:8081/admin/campaigns/progress?limit=3');
    progressReq.flush([]);

    const patternsReq = httpMock.expectOne('http://localhost:8081/api/patterns');
    patternsReq.flush([]);

    expect(component.showAdmin).toBeTrue();
  });

  it('loadStats_maps_fields_correctly', () => {
    component.loadStats();

    const req = httpMock.expectOne('http://localhost:8081/posts/stats');
    req.flush({ totalPosts: 20, publishedPosts: 15, campaigns: 5, draftPosts: 3, approvedPosts: 2 });

    expect(component.stats.totalPosts).toBe(20);
    expect(component.stats.publishedPosts).toBe(15);
    expect(component.stats.activeCampaigns).toBe(5);
    expect(component.stats.draftPosts).toBe(3);
    expect(component.stats.approvedPosts).toBe(2);
  });

  it('loadCampaigns_slices_to_4', () => {
    component.loadCampaigns();

    const req = httpMock.expectOne('http://localhost:8081/campaigns');
    const manyCampaigns = Array.from({ length: 10 }, (_, i) => ({ id: i, name: `Campaign ${i}` }));
    req.flush(manyCampaigns);

    expect(component.campaigns.length).toBe(4);
  });

  it('loadTimingAnalysis_fetches_data', () => {
    component.loadTimingAnalysis();

    const req = httpMock.expectOne('http://localhost:8081/posts/timing-analysis');
    req.flush({ bestTime: '9am' });

    expect(component.timingAnalysis).toEqual({ bestTime: '9am' });
  });

  it('loadWeeklyComparison_fetches_data', () => {
    component.loadWeeklyComparison();

    const req = httpMock.expectOne('http://localhost:8081/posts/weekly-comparison');
    req.flush({ change: 15 });

    expect(component.weeklyComparison).toEqual({ change: 15 });
  });

  it('loadUpcomingScheduled_fetches_data', () => {
    component.loadUpcomingScheduled();

    const req = httpMock.expectOne('http://localhost:8081/posts/upcoming-scheduled?limit=3');
    req.flush([{ id: 1, title: 'Upcoming' }]);

    expect(component.upcomingPosts.length).toBe(1);
  });

  it('loadPatternStats_computes_best_pattern', () => {
    component.loadPatternStats();

    const req = httpMock.expectOne('http://localhost:8081/api/patterns');
    req.flush([
      { topic: 'AI', avgEngagementScore: 0.8 },
      { topic: 'ML', avgEngagementScore: 0.6 }
    ]);

    expect(component.patternStats.totalPatterns).toBe(2);
    expect(component.patternStats.bestTopic).toBe('AI');
    expect(component.patternStats.bestScore).toBe(0.8);
  });

  it('getQuickInsight_returns_no_posts_message', () => {
    component.posts = [];
    expect(component.getQuickInsight()).toContain('No strong performing posts');
  });

  it('getQuickInsight_returns_strong_message', () => {
    component.posts = [{ likes: 150, commentsCount: 30 }];
    expect(component.getQuickInsight()).toContain('performing strongly');
  });

  it('getQuickInsight_returns_moderate_message', () => {
    component.posts = [{ likes: 50, commentsCount: 10 }];
    expect(component.getQuickInsight()).toContain('moderate engagement');
  });

  it('getQuickInsight_returns_low_message', () => {
    component.posts = [{ likes: 5, commentsCount: 1 }];
    expect(component.getQuickInsight()).toContain('Engagement levels are low');
  });

  it('getPostTrend_returns_0', () => {
    expect(component.getPostTrend()).toBe(0);
  });

  it('getEngagementRate_returns_0', () => {
    expect(component.getEngagementRate()).toBe(0);
  });

  it('userName_returns_from_authService', () => {
    spyOn(authService, 'getName').and.returnValue('TestUser');
    expect(component.userName).toBe('TestUser');
  });
});
