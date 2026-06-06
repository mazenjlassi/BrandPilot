import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CampaignDetailsComponent } from './campaign-details.component';
import { provideRouter } from '@angular/router';
import { ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { CampaignService } from '../../../services/campaign.service';
import { PostService } from '../../../services/post.service';
import { InsightService } from '../../../services/insight.service';
import { CommentService } from '../../../services/comment.service';
import { ConfirmDialogService } from '../../../shared/confirm-dialog/confirm-dialog.service';

describe('CampaignDetailsComponent', () => {
  let component: CampaignDetailsComponent;
  let fixture: ComponentFixture<CampaignDetailsComponent>;
  let campaignSvc: jasmine.SpyObj<CampaignService>;
  let postSvc: jasmine.SpyObj<PostService>;
  let insightSvc: jasmine.SpyObj<InsightService>;
  let commentSvc: jasmine.SpyObj<CommentService>;
  let confirmSpy: jasmine.SpyObj<ConfirmDialogService>;

  const mockCampaign = { id: 1, name: 'AI Campaign', topic: 'AI', status: 'active' };
  const mockPosts = [{ id: 1, title: 'Post 1', status: 'DRAFT' }];
  const mockInsights = { totalLikes: 100, totalComments: 50 };
  const mockComments = [{ id: 1, commentText: 'Great!', sentiment: 'POSITIVE' }];

  beforeEach(async () => {
    campaignSvc = jasmine.createSpyObj('CampaignService', ['getById', 'deleteCampaign', 'getCampaignPosts']);
    campaignSvc.getById.and.returnValue(of(mockCampaign));
    campaignSvc.deleteCampaign.and.returnValue(of('Deleted'));

    postSvc = jasmine.createSpyObj('PostService', ['getByCampaign', 'updatePost']);
    postSvc.getByCampaign.and.returnValue(of(mockPosts));
    postSvc.updatePost.and.returnValue(of('Updated'));

    insightSvc = jasmine.createSpyObj('InsightService', ['getByCampaign']);
    insightSvc.getByCampaign.and.returnValue(of(mockInsights));

    commentSvc = jasmine.createSpyObj('CommentService', ['getByCampaign']);
    commentSvc.getByCampaign.and.returnValue(of(mockComments));

    confirmSpy = jasmine.createSpyObj('ConfirmDialogService', ['confirm']);

    await TestBed.configureTestingModule({
      imports: [CampaignDetailsComponent],
      providers: [
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { snapshot: { params: { id: '1' } } } },
        { provide: CampaignService, useValue: campaignSvc },
        { provide: PostService, useValue: postSvc },
        { provide: InsightService, useValue: insightSvc },
        { provide: CommentService, useValue: commentSvc },
        { provide: ConfirmDialogService, useValue: confirmSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CampaignDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load campaign, posts, insights, and comments on init', () => {
    expect(campaignSvc.getById).toHaveBeenCalledWith(1);
    expect(postSvc.getByCampaign).toHaveBeenCalledWith(1);
    expect(insightSvc.getByCampaign).toHaveBeenCalledWith(1);
    expect(commentSvc.getByCampaign).toHaveBeenCalledWith(1);

    expect(component.campaign).toEqual(mockCampaign);
    expect(component.posts).toEqual(mockPosts);
    expect(component.insights).toEqual(mockInsights);
    expect(component.campaignComments).toEqual(mockComments);
    expect(component.loading).toBeFalse();
  });

  it('should handle load posts error', () => {
    postSvc.getByCampaign.and.returnValue(throwError(() => new Error('fail')));

    component.loadPosts();

    expect(component.loading).toBeFalse();
  });

  it('should handle load campaign error gracefully', () => {
    campaignSvc.getById.and.returnValue(throwError(() => new Error('fail')));

    component.loadCampaign();

    expect(component.campaign).toEqual(mockCampaign);
  });

  it('should handle empty comments gracefully', () => {
    commentSvc.getByCampaign.and.returnValue(of([]));

    component.loadCampaignComments();

    expect(component.campaignComments).toEqual([]);
  });

  it('reloadInsights should call loadInsights', () => {
    insightSvc.getByCampaign.calls.reset();
    component.reloadInsights();
    expect(insightSvc.getByCampaign).toHaveBeenCalled();
  });

  it('deleteCampaign should confirm then delete', async () => {
    confirmSpy.confirm.and.resolveTo(true);
    const routerSpy = spyOn(component['router'], 'navigate');

    await component.deleteCampaign();

    expect(campaignSvc.deleteCampaign).toHaveBeenCalledWith(1);
    expect(routerSpy).toHaveBeenCalledWith(['/campaign-list']);
  });

  it('deleteCampaign should skip if not confirmed', async () => {
    confirmSpy.confirm.and.resolveTo(false);
    await component.deleteCampaign();
    expect(campaignSvc.deleteCampaign).not.toHaveBeenCalled();
  });

  it('openPost should navigate', () => {
    const routerSpy = spyOn(component['router'], 'navigate');
    component.openPost(5);
    expect(routerSpy).toHaveBeenCalledWith(['/posts', 5]);
  });

  it('editPost should toggle editing flag', () => {
    const post = { editing: false };
    component.editPost(post);
    expect(post.editing).toBeTrue();

    component.editPost(post);
    expect(post.editing).toBeFalse();
  });

  it('publishPost should update status', () => {
    const post = { id: 1, status: 'DRAFT' };
    component.publishPost(post);

    expect(postSvc.updatePost).toHaveBeenCalledWith(1, { status: 'PUBLISHED' });
    expect(post.status).toBe('PUBLISHED');
  });

  it('goBack should navigate to campaign list', () => {
    const routerSpy = spyOn(component['router'], 'navigate');
    component.goBack();
    expect(routerSpy).toHaveBeenCalledWith(['/campaign-list']);
  });

  it('getSentimentClass should return correct class', () => {
    expect(component.getSentimentClass('POSITIVE')).toBe('positive');
    expect(component.getSentimentClass('NEGATIVE')).toBe('negative');
    expect(component.getSentimentClass('NEUTRAL')).toBe('neutral');
    expect(component.getSentimentClass('')).toBe('neutral');
  });
});
