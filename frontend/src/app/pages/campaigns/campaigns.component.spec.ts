import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CampaignsComponent } from './campaigns.component';
import { provideRouter } from '@angular/router';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { CampaignService } from '../../services/campaign.service';
import { PostService } from '../../services/post.service';

describe('CampaignsComponent', () => {
  let component: CampaignsComponent;
  let fixture: ComponentFixture<CampaignsComponent>;
  let httpMock: HttpTestingController;

  const mockCampaigns = [
    { id: 1, name: 'AI Campaign', topic: 'Artificial Intelligence', status: 'active' },
    { id: 2, name: 'Social Push', topic: 'Marketing', status: 'active' }
  ];

  const mockPosts = [
    { id: 10, title: 'Post 1', content: 'Hello', platform: 'LINKEDIN', image: { imagePrompt: 'prompt1' } },
    { id: 11, title: 'Post 2', content: 'World', platform: 'TWITTER', image: null }
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CampaignsComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        CampaignService,
        PostService
      ]
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(CampaignsComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    httpMock.verify();
  });

  function flushRecentCampaigns() {
    const req = httpMock.match('http://localhost:8081/campaigns/recent?limit=5');
    req.forEach(r => r.flush(mockCampaigns));
  }

  describe('initialization', () => {
    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('loads recent campaigns on init', () => {
      fixture.detectChanges();
      const req = httpMock.expectOne('http://localhost:8081/campaigns/recent?limit=5');
      expect(req.request.method).toBe('GET');
      req.flush(mockCampaigns);
      expect(component.recentCampaigns.length).toBe(2);
    });

    it('handles recent campaigns load error', () => {
      fixture.detectChanges();
      const req = httpMock.expectOne('http://localhost:8081/campaigns/recent?limit=5');
      req.flush('Error', { status: 500, statusText: 'Server Error' });
      expect(component.recentCampaigns).toEqual([]);
    });
  });

  describe('mode toggling', () => {
    it('enableExistingMode sets state and reloads campaigns', () => {
      fixture.detectChanges();
      flushRecentCampaigns();
      component.enableExistingMode();
      expect(component.showForm).toBeTrue();
      expect(component.showExistingMode).toBeTrue();
      expect(component.campaignId).toBeNull();
      expect(component.posts).toEqual([]);
      const req = httpMock.expectOne('http://localhost:8081/campaigns/recent?limit=5');
      req.flush(mockCampaigns);
    });

    it('enableNewMode sets state', () => {
      component.enableNewMode();
      expect(component.showForm).toBeTrue();
      expect(component.showExistingMode).toBeFalse();
      expect(component.campaignId).toBeNull();
    });
  });

  describe('onCampaignSelect', () => {
    it('loads campaign posts when campaign is found', () => {
      component.recentCampaigns = mockCampaigns;
      const event = { target: { value: 'Social Push' } };
      component.onCampaignSelect(event);
      expect(component.campaignId).toBe(2);
      expect(component.selectedExistingCampaign).toEqual(mockCampaigns[1]);

      const req = httpMock.expectOne('http://localhost:8081/campaigns/2/posts');
      expect(req.request.method).toBe('GET');
      req.flush(mockPosts);
      expect(component.posts.length).toBe(2);
      expect(component.posts[0].imagePrompt).toBe('prompt1');
    });

    it('clears state when no campaign matches', () => {
      component.recentCampaigns = mockCampaigns;
      const event = { target: { value: 'NonExistent' } };
      component.onCampaignSelect(event);
      expect(component.campaignId).toBeNull();
      expect(component.selectedExistingCampaign).toBeNull();
    });

    it('handles posts load error', () => {
      component.recentCampaigns = mockCampaigns;
      const event = { target: { value: 'AI Campaign' } };
      component.onCampaignSelect(event);
      const req = httpMock.expectOne('http://localhost:8081/campaigns/1/posts');
      req.flush('Error', { status: 500, statusText: 'Server Error' });
      expect(component.posts).toEqual([]);
    });
  });

  describe('createManualCampaign', () => {
    it('opens manual post modal for existing campaign', () => {
      component.showExistingMode = true;
      component.campaignId = 1;
      component.createManualCampaign();
      expect(component.showManual).toBeTrue();
    });

    it('shows toast when name is missing', () => {
      component.name = '';
      component.topic = 'AI';
      spyOn(component['toast'], 'error');
      component.createManualCampaign();
      expect(component['toast'].error).toHaveBeenCalledWith('Name and topic are required');
    });

    it('shows toast when topic is missing', () => {
      component.name = 'Test';
      component.topic = '';
      spyOn(component['toast'], 'error');
      component.createManualCampaign();
      expect(component['toast'].error).toHaveBeenCalledWith('Name and topic are required');
    });

    it('creates campaign and opens manual post modal', () => {
      component.name = 'New Campaign';
      component.topic = 'AI';
      component.createManualCampaign();
      expect(component.loading).toBeTrue();

      const req = httpMock.expectOne('http://localhost:8081/campaigns');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ name: 'New Campaign', topic: 'AI' });
      req.flush({ id: 5 });

      expect(component.campaignId).toBe(5);
      expect(component.loading).toBeFalse();
      expect(component.showManual).toBeTrue();
    });

    it('handles create campaign error', () => {
      component.name = 'Fail';
      component.topic = 'Fail';
      component.createManualCampaign();
      const req = httpMock.expectOne('http://localhost:8081/campaigns');
      req.flush('Error', { status: 500, statusText: 'Server Error' });
      expect(component.loading).toBeFalse();
    });
  });

  describe('onFileSelected', () => {
    it('sets previewUrl when file is selected', () => {
      const file = new File([''], 'test.png', { type: 'image/png' });
      const event = { target: { files: [file] } };
      spyOn(URL, 'createObjectURL').and.returnValue('blob:test');
      component.onFileSelected(event);
      expect(component.selectedFile).toBe(file);
      expect(component.previewUrl).toBe('blob:test');
    });

    it('does nothing when no file', () => {
      const event = { target: { files: [] } };
      component.onFileSelected(event);
      expect(component.selectedFile).toBeUndefined();
    });

    it('clears selectedVideoFile when image is selected', () => {
      component.selectedVideoFile = { name: 'old.mp4' } as any;
      const file = new File([''], 'img.png', { type: 'image/png' });
      spyOn(URL, 'createObjectURL').and.returnValue('blob:img');
      component.onFileSelected({ target: { files: [file] } });
      expect(component.selectedVideoFile).toBeNull();
    });
  });

  describe('switchMediaMode', () => {
    it('switches to image mode and resets preview', () => {
      component.previewUrl = 'blob:old';
      component.selectedVideoFile = {} as any;
      component.switchMediaMode('image');
      expect(component.mediaMode).toBe('image');
      expect(component.previewUrl).toBeNull();
      expect(component.selectedVideoFile).toBeNull();
    });

    it('switches to video mode', () => {
      component.switchMediaMode('video');
      expect(component.mediaMode).toBe('video');
    });

    it('switches to generate mode', () => {
      component.switchMediaMode('generate');
      expect(component.mediaMode).toBe('generate');
    });
  });

  describe('onVideoSelected', () => {
    it('sets selectedVideoFile and previewUrl', () => {
      const file = new File([''], 'test.mp4', { type: 'video/mp4' });
      const event = { target: { files: [file] } };
      spyOn(URL, 'createObjectURL').and.returnValue('blob:video');
      component.onVideoSelected(event);
      expect(component.selectedVideoFile).toBe(file);
      expect(component.previewUrl).toBe('blob:video');
      expect(component.selectedFile).toBeNull();
    });

    it('clears image file when video selected', () => {
      component.selectedFile = { name: 'old.png' } as any;
      spyOn(URL, 'createObjectURL').and.returnValue('blob:v');
      component.onVideoSelected({ target: { files: [new File([''], 'v.mp4')] } });
      expect(component.selectedFile).toBeNull();
    });
  });

  describe('createManualPost', () => {
    it('returns early when no campaignId', () => {
      component.campaignId = null;
      component.createManualPost();
      expect(component.loading).toBeFalse();
    });

    it('creates post via generate mode with image generation', () => {
      component.campaignId = 1;
      component.mediaMode = 'generate';
      component.manualPost = { title: 'G', content: 'C', hashtags: '', platform: 'LINKEDIN', scheduledAt: null, permanent: false, approved: true, link: '', imagePrompt: 'sunset' };
      component.createManualPost();
      expect(component.loading).toBeTrue();

      const req = httpMock.expectOne('http://localhost:8081/campaigns/1/posts/with-image');
      expect(req.request.method).toBe('POST');
      req.flush({ id: 99, imagePrompt: 'sunset' });

      const genReq = httpMock.expectOne('http://localhost:8081/posts/99/generate-image');
      expect(genReq.request.method).toBe('POST');
      genReq.flush({ imageUrl: 'http://example.com/img.jpg' });

      expect(component.posts.length).toBe(1);
      expect(component.posts[0].mediaMode).toBe('generate');
      expect(component.posts[0].imageUrl).toBe('http://example.com/img.jpg');
      expect(component.loading).toBeFalse();
    });

    it('handles create post error', () => {
      component.campaignId = 1;
      component.manualPost = { title: 'Test', content: 'Content', hashtags: '', platform: 'LINKEDIN', scheduledAt: null, permanent: false, approved: true, link: '' };
      spyOn(component['toast'], 'error');
      component.createManualPost();
      const req = httpMock.expectOne('http://localhost:8081/campaigns/1/posts/with-image');
      req.flush('Error', { status: 500, statusText: 'Server Error' });
      expect(component.loading).toBeFalse();
      expect(component['toast'].error).toHaveBeenCalledWith('Failed to create post');
    });

    it('formats scheduledAt to ISO string', (done) => {
      component.campaignId = 1;
      component.manualPost = { title: 'T', content: 'C', hashtags: '', platform: 'LINKEDIN', scheduledAt: '2026-06-20T10:00', permanent: false, approved: true, link: '' };
      component.createManualPost();
      const req = httpMock.expectOne('http://localhost:8081/campaigns/1/posts/with-image');
      const body = req.request.body as FormData;
      const dataBlob = body.get('data') as Blob;
      const reader = new FileReader();
      reader.onload = () => {
        const parsed = JSON.parse(reader.result as string);
        expect(parsed.scheduledAt).toMatch(/^2026-06-20T/);
        done();
      };
      reader.readAsText(dataBlob);
      req.flush({});
    });

    it('creates post in generate mode (no image upload)', () => {
      component.campaignId = 1;
      component.mediaMode = 'generate';
      component.selectedFile = undefined as any;
      component.selectedVideoFile = undefined as any;
      component.manualPost = { title: 'T', content: 'C', hashtags: '', platform: 'LINKEDIN' };
      component.createManualPost();
      const req = httpMock.expectOne('http://localhost:8081/campaigns/1/posts/with-image');
      const body = req.request.body as FormData;
      expect(body.has('image')).toBeFalse();
      expect(body.has('video')).toBeFalse();
      req.flush({ id: 99 });
    });

    it('creates post with video file', () => {
      component.campaignId = 1;
      component.mediaMode = 'video';
      const videoFile = new File([''], 'clip.mp4', { type: 'video/mp4' });
      component.selectedVideoFile = videoFile;
      component.manualPost = { title: 'V', content: 'V', hashtags: '', platform: 'LINKEDIN' };
      component.createManualPost();
      const req = httpMock.expectOne('http://localhost:8081/campaigns/1/posts/with-image');
      const body = req.request.body as FormData;
      const videoPart = body.get('video');
      expect(videoPart).toBeTruthy();
      expect((videoPart as any).name).toBe('clip.mp4');
      req.flush({});
    });
  });

  describe('delete', () => {
    it('deletes post after confirmation', async () => {
      const post = { id: 1, title: 'To Delete' };
      component.posts = [post];
      const confirmSpy = spyOn(component['confirm'], 'confirm').and.resolveTo(true);
      await component.delete(post);
      expect(confirmSpy).toHaveBeenCalledWith({ title: 'Delete Post', message: 'Delete this post permanently?' });
      const req = httpMock.expectOne('http://localhost:8081/posts/1');
      expect(req.request.method).toBe('DELETE');
      req.flush('');
      expect(component.posts.length).toBe(0);
    });

    it('skips delete when confirmation is cancelled', async () => {
      spyOn(component['confirm'], 'confirm').and.resolveTo(false);
      await component.delete({ id: 1 });
      httpMock.expectNone('http://localhost:8081/posts/1');
      expect(true).toBeTrue();
    });
  });

  describe('editPost', () => {
    it('toggles editing flag', () => {
      const post: any = {};
      component.editPost(post);
      expect(post.editing).toBeTrue();
      component.editPost(post);
      expect(post.editing).toBeFalse();
    });
  });

  describe('edit media mode', () => {
    it('switchEditMediaMode changes class-level editMediaMode', () => {
      component.switchEditMediaMode({}, 'video');
      expect(component.editMediaMode).toBe('video');
      expect(component.editSelectedFile).toBeNull();
      expect(component.editSelectedVideoFile).toBeNull();
      expect(component.editPreviewUrl).toBeNull();
      component.switchEditMediaMode({}, 'generate');
      expect(component.editMediaMode).toBe('generate');
    });

    it('switchEditMediaMode sets editImagePrompt in generate mode', () => {
      const post: any = {};
      component.switchEditMediaMode(post, 'generate');
      expect(post.editImagePrompt).toBe('');
    });

    it('switchEditMediaMode resets image/video selection', () => {
      component.editSelectedFile = { name: 'old.png' } as any;
      component.switchEditMediaMode({}, 'video');
      expect(component.editSelectedFile).toBeNull();
    });

    it('switchEditMediaMode "generate" resets video selection', () => {
      component.editSelectedVideoFile = { name: 'old.mp4' } as any;
      component.editPreviewUrl = 'blob:x';
      component.switchEditMediaMode({}, 'generate');
      expect(component.editSelectedVideoFile).toBeNull();
      expect(component.editPreviewUrl).toBeNull();
    });

    it('onEditFileSelected sets editSelectedFile and editPreviewUrl', () => {
      const file = new File([''], 'edit.png', { type: 'image/png' });
      spyOn(URL, 'createObjectURL').and.returnValue('blob:edit');
      component.onEditFileSelected({ target: { files: [file] } });
      expect(component.editSelectedFile).toBe(file);
      expect(component.editPreviewUrl).toBe('blob:edit');
      expect(component.editSelectedVideoFile).toBeNull();
    });

    it('onEditVideoSelected sets editSelectedVideoFile and editPreviewUrl', () => {
      const file = new File([''], 'edit.mp4', { type: 'video/mp4' });
      spyOn(URL, 'createObjectURL').and.returnValue('blob:ev');
      component.onEditVideoSelected({ target: { files: [file] } });
      expect(component.editSelectedVideoFile).toBe(file);
      expect(component.editPreviewUrl).toBe('blob:ev');
      expect(component.editSelectedFile).toBeNull();
    });

    it('uploadEditMedia uploads video and updates post', () => {
      const post: any = { id: 7, editing: true };
      component.editSelectedVideoFile = new File([''], 'up.mp4', { type: 'video/mp4' });
      spyOn(component['toast'], 'success');
      component.uploadEditMedia(post);
      expect(post.uploadingMedia).toBeTrue();
      const req = httpMock.expectOne('http://localhost:8081/posts/upload');
      expect(req.request.method).toBe('POST');
      const body = req.request.body as FormData;
      expect((body.get('file') as File).name).toBe('up.mp4');
      req.flush({ url: 'http://example.com/vid.mp4' });
      expect(post.videoUrl).toBe('http://example.com/vid.mp4');
      expect(post.uploadingMedia).toBeFalse();
      expect(component.editSelectedVideoFile).toBeNull();
      expect(component.editPreviewUrl).toBeNull();
      expect(component['toast'].success).toHaveBeenCalledWith('Media uploaded');
    });

    it('uploadEditMedia returns early when no file selected', () => {
      component.editSelectedFile = null;
      component.editSelectedVideoFile = null;
      component.uploadEditMedia({});
      httpMock.expectNone('http://localhost:8081/posts/upload');
    });

    it('uploadEditMedia handles error', () => {
      const post: any = {};
      component.editSelectedFile = new File([''], 'bad.png');
      spyOn(component['toast'], 'error');
      component.uploadEditMedia(post);
      const req = httpMock.expectOne('http://localhost:8081/posts/upload');
      req.flush('Error', { status: 500, statusText: 'Err' });
      expect(post.uploadingMedia).toBeFalse();
      expect(component['toast'].error).toHaveBeenCalledWith('Failed to upload media');
    });
  });

  describe('checkRiskyKeywords', () => {
    it('returns true for risky keywords', () => {
      expect(component.checkRiskyKeywords('Show a person smiling')).toBeTrue();
      expect(component.checkRiskyKeywords('face closeup')).toBeTrue();
      expect(component.checkRiskyKeywords('human figure')).toBeTrue();
      expect(component.checkRiskyKeywords('raise hand')).toBeTrue();
      expect(component.checkRiskyKeywords('finger pointing')).toBeTrue();
      expect(component.checkRiskyKeywords('photo realistic')).toBeTrue();
    });

    it('returns false for safe keywords', () => {
      expect(component.checkRiskyKeywords('Abstract shapes')).toBeFalse();
      expect(component.checkRiskyKeywords('Mountain landscape')).toBeFalse();
      expect(component.checkRiskyKeywords('')).toBeFalse();
    });

    it('is case-insensitive', () => {
      expect(component.checkRiskyKeywords('PERSON')).toBeTrue();
      expect(component.checkRiskyKeywords('FACE')).toBeTrue();
    });
  });

  describe('post media (display mode)', () => {
    it('switchPostMediaMode sets mode and clears state', () => {
      const post: any = { previewUrl: 'blob:x', selectedFile: { name: 'f.png' }, selectedVideoFile: { name: 'f.mp4' } };
      component.switchPostMediaMode(post, 'video');
      expect(post.mediaMode).toBe('video');
      expect(post.previewUrl).toBeNull();
      expect(post.selectedFile).toBeNull();
      expect(post.selectedVideoFile).toBeNull();
    });

    it('onPostFileSelected sets selectedFile and previewUrl', () => {
      const post: any = {};
      const file = new File([''], 'post.png', { type: 'image/png' });
      spyOn(URL, 'createObjectURL').and.returnValue('blob:post');
      component.onPostFileSelected({ target: { files: [file] } }, post);
      expect(post.selectedFile).toBe(file);
      expect(post.previewUrl).toBe('blob:post');
      expect(post.selectedVideoFile).toBeNull();
    });

    it('onPostVideoSelected sets selectedVideoFile and previewUrl', () => {
      const post: any = {};
      const file = new File([''], 'post.mp4', { type: 'video/mp4' });
      spyOn(URL, 'createObjectURL').and.returnValue('blob:pv');
      component.onPostVideoSelected({ target: { files: [file] } }, post);
      expect(post.selectedVideoFile).toBe(file);
      expect(post.previewUrl).toBe('blob:pv');
      expect(post.selectedFile).toBeNull();
    });

    it('uploadPostMedia uploads image and updates post', () => {
      const post: any = {};
      post.selectedFile = new File([''], 'up.png', { type: 'image/png' });
      spyOn(component['toast'], 'success');
      component.uploadPostMedia(post);
      expect(post.uploadingMedia).toBeTrue();
      const req = httpMock.expectOne('http://localhost:8081/posts/upload');
      expect(req.request.method).toBe('POST');
      req.flush({ url: 'http://example.com/img.jpg' });
      expect(post.imageUrl).toBe('http://example.com/img.jpg');
      expect(post.uploadingMedia).toBeFalse();
      expect(post.previewUrl).toBeNull();
      expect(post.selectedFile).toBeNull();
      expect(component['toast'].success).toHaveBeenCalledWith('Media uploaded');
    });

    it('uploadPostMedia uploads video and updates post', () => {
      const post: any = {};
      post.selectedVideoFile = new File([''], 'up.mp4', { type: 'video/mp4' });
      spyOn(component['toast'], 'success');
      component.uploadPostMedia(post);
      const req = httpMock.expectOne('http://localhost:8081/posts/upload');
      req.flush({ url: 'http://example.com/vid.mp4' });
      expect(post.videoUrl).toBe('http://example.com/vid.mp4');
      expect(component['toast'].success).toHaveBeenCalledWith('Media uploaded');
    });

    it('uploadPostMedia returns early when no file', () => {
      const post: any = {};
      component.uploadPostMedia(post);
      httpMock.expectNone('http://localhost:8081/posts/upload');
    });

    it('uploadPostMedia handles error', () => {
      const post: any = {};
      post.selectedFile = new File([''], 'bad.png');
      spyOn(component['toast'], 'error');
      component.uploadPostMedia(post);
      const req = httpMock.expectOne('http://localhost:8081/posts/upload');
      req.flush('Error', { status: 500, statusText: 'Err' });
      expect(post.uploadingMedia).toBeFalse();
      expect(component['toast'].error).toHaveBeenCalledWith('Failed to upload media');
    });
  });

  describe('generateImage', () => {
    it('returns early when post has no content', () => {
      const post: any = { content: '' };
      component.generateImage(post);
      expect(post.generatingImage).toBeUndefined();
    });

    it('calls generateImage service with prompt and updates post', () => {
      const post: any = { id: 5, content: 'Hello', imagePrompt: 'sunset' };
      component.generateImage(post);
      expect(post.generatingImage).toBeTrue();

      const req = httpMock.expectOne('http://localhost:8081/posts/5/generate-image');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ prompt: 'sunset' });
      req.flush({ imageUrl: 'http://example.com/img.jpg' });

      expect(post.imageUrl).toBe('http://example.com/img.jpg');
      expect(post.generatingImage).toBeFalse();
    });

    it('handles generate image error', () => {
      const post: any = { id: 5, content: 'Hello' };
      component.generateImage(post);
      const req = httpMock.expectOne('http://localhost:8081/posts/5/generate-image');
      req.flush('Error', { status: 500, statusText: 'Server Error' });
      expect(post.generatingImage).toBeFalse();
    });
  });

  describe('save', () => {
    it('calls updatePost and shows success toast', () => {
      const post: any = { id: 1, title: 'T', content: 'C', hashtags: '#t', platform: 'LINKEDIN', scheduledAt: null, permanent: false, approved: true, link: '', imageUrl: '', videoUrl: undefined };
      spyOn(component['toast'], 'success');
      component.save(post);
      expect(component.savingPostId).toBe(1);

      const req = httpMock.expectOne('http://localhost:8081/posts/1');
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual({
        title: 'T', content: 'C', hashtags: '#t', platform: 'LINKEDIN',
        scheduledAt: null, permanent: false, approved: true, link: '', imageUrl: '', videoUrl: undefined
      });
      req.flush('');
      expect(component.savingPostId).toBeNull();
      expect(component['toast'].success).toHaveBeenCalledWith('Saved!');
    });

    it('handles save error', () => {
      const post: any = { id: 1, title: 'T', content: 'C', hashtags: '', platform: 'LINKEDIN', scheduledAt: null, permanent: false, approved: true, link: '', imageUrl: '', videoUrl: undefined };
      spyOn(component['toast'], 'error');
      component.save(post);
      const req = httpMock.expectOne('http://localhost:8081/posts/1');
      req.flush('Error', { status: 500, statusText: 'Server Error' });
      expect(component.savingPostId).toBeNull();
      expect(component['toast'].error).toHaveBeenCalledWith('Failed to save');
    });
  });

  describe('publishNow', () => {
    it('calls publishPost and updates status', () => {
      const post: any = { id: 2, status: 'DRAFT' };
      spyOn(component['toast'], 'success');
      component.publishNow(post);
      expect(component.publishingPostId).toBe(2);

      const req = httpMock.expectOne('http://localhost:8081/publish/2');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({});
      req.flush({});

      expect(post.status).toBe('PUBLISHED');
      expect(post.publishedAt).toBeTruthy();
      expect(component.publishingPostId).toBeNull();
      expect(component['toast'].success).toHaveBeenCalledWith('Published!');
    });

    it('handles publish error', () => {
      const post: any = { id: 2, status: 'DRAFT' };
      spyOn(component['toast'], 'error');
      component.publishNow(post);
      const req = httpMock.expectOne('http://localhost:8081/publish/2');
      req.flush('Error', { status: 500, statusText: 'Server Error' });
      expect(component.publishingPostId).toBeNull();
      expect(component['toast'].error).toHaveBeenCalledWith('Failed to publish');
    });
  });

  describe('generate', () => {
    it('generates for existing campaign and appends posts', () => {
      fixture.detectChanges();
      flushRecentCampaigns();

      component.showExistingMode = true;
      component.campaignId = 1;
      component.posts = [{ id: 10, content: 'Existing' }];
      component.generate();
      expect(component.loading).toBeTrue();

      const req = httpMock.expectOne('http://localhost:8081/campaigns/1/generate');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({});
      req.flush([{ id: 20, content: 'New', image: { imagePrompt: 'gen' } }]);

      expect(component.posts.length).toBe(2);
      expect(component.posts[1].imagePrompt).toBe('gen');
      expect(component.loading).toBeFalse();
    });

    it('handles existing campaign generate error', () => {
      fixture.detectChanges();
      flushRecentCampaigns();

      component.showExistingMode = true;
      component.campaignId = 1;
      spyOn(component['toast'], 'error');
      component.generate();
      const req = httpMock.expectOne('http://localhost:8081/campaigns/1/generate');
      req.flush('Error', { status: 500, statusText: 'Server Error' });
      expect(component.loading).toBeFalse();
      expect(component['toast'].error).toHaveBeenCalledWith('Error');
    });

    it('generates new campaign via generateCampaign', () => {
      component.name = 'Gen Campaign';
      component.topic = 'AI';
      component.generate();
      expect(component.loading).toBeTrue();

      const req = httpMock.expectOne('http://localhost:8081/campaigns/generate');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ name: 'Gen Campaign', topic: 'AI' });
      req.flush(mockPosts.map((p: any) => ({ ...p, imagePrompt: p.image?.imagePrompt || '' })));

      expect(component.posts.length).toBe(2);
      expect(component.loading).toBeFalse();
    });

    it('handles empty response for generateCampaign', () => {
      component.name = 'Empty';
      component.topic = 'Empty';
      component.generate();
      const req = httpMock.expectOne('http://localhost:8081/campaigns/generate');
      req.flush(null);
      expect(component.posts).toEqual([]);
    });

    it('handles generateCampaign error', () => {
      component.name = 'Fail';
      component.topic = 'Fail';
      spyOn(component['toast'], 'error');
      component.generate();
      const req = httpMock.expectOne('http://localhost:8081/campaigns/generate');
      req.flush('Error', { status: 500, statusText: 'Server Error' });
      expect(component.loading).toBeFalse();
      expect(component['toast'].error).toHaveBeenCalledWith('Error');
    });
  });
});
