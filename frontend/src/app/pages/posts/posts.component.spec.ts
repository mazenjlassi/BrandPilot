import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PostsComponent } from './posts.component';
import { provideRouter } from '@angular/router';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController } from '@angular/common/http/testing';
import { PostService } from '../../services/post.service';
import { ToastService } from '../../shared/toast/toast.service';
import { ConfirmDialogService } from '../../shared/confirm-dialog/confirm-dialog.service';
import { Router } from '@angular/router';

describe('PostsComponent', () => {
  let component: PostsComponent;
  let fixture: ComponentFixture<PostsComponent>;
  let httpMock: HttpTestingController;
  let confirmService: ConfirmDialogService;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PostsComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        PostService,
        ToastService,
        ConfirmDialogService
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(PostsComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    confirmService = TestBed.inject(ConfirmDialogService);
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  afterEach(() => {
    try { httpMock.expectOne('http://localhost:8081/posts/drafts').flush([]); } catch {}
    httpMock.verify();
  });

  it('creates_component', () => {
    expect(component).toBeTruthy();
  });

  it('loads_drafts_on_init', () => {
    const req = httpMock.expectOne('http://localhost:8081/posts/drafts');
    expect(req.request.method).toBe('GET');
    req.flush([{ id: 1, content: 'Draft post' }]);
    expect(component.posts.length).toBe(1);
    expect(component.posts[0].id).toBe(1);
  });

  it('setTab_changes_activeTab_and_clears_search', () => {
    component.searchQuery = 'test';
    component.setTab('published');

    expect(component.activeTab).toBe('published');
    expect(component.searchQuery).toBe('');

    const req = httpMock.expectOne('http://localhost:8081/posts/published');
    req.flush([]);
  });

  it('setTab_scheduled_loads_scheduled', () => {
    component.setTab('scheduled');
    const req = httpMock.expectOne('http://localhost:8081/posts/scheduled');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('setTab_permanent_loads_permanent', () => {
    component.setTab('permanent');
    const req = httpMock.expectOne('http://localhost:8081/posts/permanent');
    req.flush([]);
  });

  it('filteredPosts_filters_by_searchQuery', () => {
    component.posts = [
      { content: 'Hello World', platform: 'LINKEDIN' },
      { content: 'Goodbye World', platform: 'INSTAGRAM' }
    ];
    component.searchQuery = 'hello';
    expect(component.filteredPosts.length).toBe(1);

    component.searchQuery = 'INSTAGRAM';
    expect(component.filteredPosts.length).toBe(1);

    component.searchQuery = 'nonexistent';
    expect(component.filteredPosts.length).toBe(0);
  });

  it('filteredPosts_returns_all_when_no_search', () => {
    component.posts = [{ content: 'A' }, { content: 'B' }];
    component.searchQuery = '';
    expect(component.filteredPosts.length).toBe(2);
  });

  it('viewDetails_navigates', () => {
    const routerSpy = spyOn(router, 'navigate');
    component.viewDetails(5);
    expect(routerSpy).toHaveBeenCalledWith(['/posts', 5]);
  });

  it('canEdit_returns_true_for_permanent', () => {
    expect(component.canEdit({ permanent: true, status: 'PUBLISHED' })).toBeTrue();
  });

  it('canEdit_returns_true_for_non_published', () => {
    expect(component.canEdit({ permanent: false, status: 'DRAFT' })).toBeTrue();
  });

  it('canEdit_returns_false_for_published_non_permanent', () => {
    expect(component.canEdit({ permanent: false, status: 'PUBLISHED' })).toBeFalse();
  });

  it('postNow_confirms_and_publishes', (done) => {
    const post = { id: 1, status: 'DRAFT' };
    const confirmSpy = spyOn(confirmService, 'confirm').and.returnValue(Promise.resolve(true));

    component.postNow(post).then(() => {
      const req = httpMock.expectOne('http://localhost:8081/publish/1');
      expect(req.request.method).toBe('POST');
      req.flush({});

      expect(post.status).toBe('PUBLISHED');
      done();
    });
  });
});
