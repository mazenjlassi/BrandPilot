import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { LucideAngularModule, Image, Loader2, FolderPlus, X, FileText, Edit3, Calendar, Check, Infinity, Save, Send, Trash2, AlertTriangle } from 'lucide-angular';

import { CampaignService } from '../../services/campaign.service';
import { PostService } from '../../services/post.service';
import { ToastService } from '../../shared/toast/toast.service';
import { ConfirmDialogService } from '../../shared/confirm-dialog/confirm-dialog.service';

@Component({
  selector: 'app-campaigns',
  standalone: true,
  imports: [FormsModule, CommonModule, LucideAngularModule],
  templateUrl: './campaigns.component.html',
  styleUrls: ['./campaigns.component.css']
})
export class CampaignsComponent implements OnInit {

  icons = {
    image: Image,
    loader2: Loader2,
    folderPlus: FolderPlus,
    x: X,
    fileText: FileText,
    edit3: Edit3,
    calendar: Calendar,
    check: Check,
    infinity: Infinity,
    save: Save,
    send: Send,
    trash2: Trash2,
    alertTriangle: AlertTriangle
  };

  // ================= FORM STATE =================

  showForm = false;
  showExistingMode = false;

  // ================= RECENT CAMPAIGNS =================

  recentCampaigns: any[] = [];
  selectedExistingCampaign: any = null;

  // ================= FILTERS =================

  sortBy = 'newest';

  // ================= CAMPAIGN =================

  name = '';
  topic = '';

  campaignId: number | null = null;

  showManual = false;
  loading = false;

  // ================= POSTS =================

  posts: any[] = [];

  savingPostId: number | null = null;
  publishingPostId: number | null = null;

  // ================= MEDIA =================

  mediaMode: 'generate' | 'image' | 'video' = 'image';

  previewUrl: string | null = null;
  selectedFile!: File;
  selectedVideoFile: File | null = null;

  // ================= MANUAL POST =================

  manualPost: any = {
    title: '',
    content: '',
    hashtags: '',
    platform: 'LINKEDIN',
    scheduledAt: null,
    permanent: false,
    approved: true,
    link: '',
    imagePrompt: ''
  };

  constructor(
    private campaignService: CampaignService,
    private postService: PostService,
    private toast: ToastService,
    private confirm: ConfirmDialogService
  ) {}

  ngOnInit() {
    this.loadRecentCampaigns();
  }

  // ================= RECENT CAMPAIGNS =================

  loadRecentCampaigns() {
    this.campaignService.getRecent(5).subscribe({
      next: (res) => {
        this.recentCampaigns = res;
      },
      error: () => {}
    });
  }

  onCampaignSelect(event: any) {
    const selectedName = event.target.value;
    const campaign = this.recentCampaigns.find(c => c.name === selectedName);
    
    if (campaign) {
      this.selectedExistingCampaign = campaign;
      this.campaignId = campaign.id;
      this.topic = campaign.topic || '';
      
      // Load posts for this campaign
      this.loadCampaignPosts(campaign.id);
    } else {
      this.selectedExistingCampaign = null;
      this.campaignId = null;
      this.topic = '';
    }
  }

  loadCampaignPosts(campaignId: number) {
    this.campaignService.getCampaignPosts(campaignId).subscribe({
      next: (res: any) => {
        this.posts = Array.isArray(res) ? res.map((p: any) => ({ ...p, imagePrompt: p.image?.imagePrompt || '', mediaMode: 'generate' })) : [];
      },
      error: () => {
        this.posts = [];
      }
    });
  }

  enableExistingMode() {
    this.showForm = true;
    this.showExistingMode = true;
    this.name = '';
    this.topic = '';
    this.campaignId = null;
    this.selectedExistingCampaign = null;
    this.posts = [];
    this.loadRecentCampaigns();
  }

  enableNewMode() {
    this.showForm = true;
    this.showExistingMode = false;
    this.name = '';
    this.topic = '';
    this.campaignId = null;
    this.selectedExistingCampaign = null;
    this.posts = [];
  }

  switchMediaMode(mode: 'generate' | 'image' | 'video') {
    this.mediaMode = mode;
    this.previewUrl = null;
    this.selectedFile = null as any;
    this.selectedVideoFile = null;
  }

  // ================= CREATE MANUAL CAMPAIGN =================

  createManualCampaign() {
    if (this.showExistingMode && this.campaignId) {
      // Adding to existing campaign - just open manual post modal
      this.showManual = true;
      return;
    }

    if (!this.name || !this.topic) {
      this.toast.error('Name and topic are required');
      return;
    }

    this.loading = true;

    this.campaignService.createCampaign({
      name: this.name,
      topic: this.topic
    }).subscribe({
      next: (res: any) => {
        this.campaignId = res.id;
        this.loading = false;
        this.showManual = true;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  // ================= FILE =================

  onFileSelected(event: any) {
    this.selectedFile = event.target.files[0];
    this.selectedVideoFile = null;
    if (this.selectedFile) {
      this.previewUrl = URL.createObjectURL(this.selectedFile);
    }
  }

  onVideoSelected(event: any) {
    this.selectedVideoFile = event.target.files[0];
    this.selectedFile = null as any;
    this.previewUrl = null;
    if (this.selectedVideoFile) {
      this.previewUrl = URL.createObjectURL(this.selectedVideoFile);
    }
  }

  // ================= CREATE MANUAL POST =================

  createManualPost() {
    if (!this.campaignId) return;

    this.loading = true;

    const data = {
      ...this.manualPost,
      scheduledAt: this.manualPost.scheduledAt
        ? new Date(this.manualPost.scheduledAt).toISOString()
        : null
    };

    if (this.mediaMode === 'generate' && this.manualPost.imagePrompt) {
      this.postService.createPostWithImage(this.campaignId, data).subscribe({
        next: (res: any) => {
          const newPost = { ...res, imagePrompt: res.image?.imagePrompt || '', mediaMode: 'generate' };
          this.postService.generateImage(res.id, this.manualPost.imagePrompt).subscribe({
            next: (imgRes: any) => {
              newPost.imageUrl = imgRes.imageUrl || imgRes.url;
              this.posts.push(newPost);
              this.resetManualForm();
              this.loading = false;
            },
            error: () => {
              this.posts.push(newPost);
              this.resetManualForm();
              this.loading = false;
            }
          });
        },
        error: () => {
          this.loading = false;
          this.toast.error('Failed to create post');
        }
      });
      return;
    }

    this.postService
      .createPostWithImage(
        this.campaignId,
        data,
        this.mediaMode === 'image' ? this.selectedFile : undefined,
        this.mediaMode === 'video' ? this.selectedVideoFile ?? undefined : undefined
      )
      .subscribe({
        next: (res: any) => {
          this.posts.push({ ...res, imagePrompt: res.image?.imagePrompt || '' });
          this.resetManualForm();
          this.loading = false;
        },
        error: (err) => {
          console.error(err);
          this.loading = false;
          this.toast.error('Failed to create post');
        }
      });
  }

  private resetManualForm() {
    this.manualPost = {
      title: '',
      content: '',
      hashtags: '',
      platform: 'LINKEDIN',
      scheduledAt: null,
      permanent: false,
      approved: true,
      link: '',
      imagePrompt: ''
    };
    this.previewUrl = null;
    this.selectedFile = null as any;
    this.selectedVideoFile = null;
  }

  // ================= DELETE =================

  async delete(post: any) {
    const ok = await this.confirm.confirm({ title: 'Delete Post', message: 'Delete this post permanently?' });
    if (!ok) return;

    this.postService.deletePost(post.id).subscribe({
      next: () => {
        this.posts = this.posts.filter(p => p.id !== post.id);
      }
    });
  }

  // ================= EDIT POST =================

  editPost(post: any) {
    post.editing = !post.editing;
  }

  // ================= UPLOAD MEDIA (EDIT MODE) =================

  editMediaMode: 'generate' | 'image' | 'video' | null = null;
  editSelectedFile: File | null = null;
  editSelectedVideoFile: File | null = null;
  editPreviewUrl: string | null = null;

  switchEditMediaMode(post: any, mode: 'generate' | 'image' | 'video') {
    this.editMediaMode = mode;
    this.editSelectedFile = null;
    this.editSelectedVideoFile = null;
    this.editPreviewUrl = null;
    if (mode === 'generate') {
      post.editImagePrompt = post.editImagePrompt || post.imagePrompt || '';
    }
  }

  onEditFileSelected(event: any) {
    this.editSelectedFile = event.target.files[0];
    this.editSelectedVideoFile = null;
    if (this.editSelectedFile) {
      this.editPreviewUrl = URL.createObjectURL(this.editSelectedFile);
    }
  }

  onEditVideoSelected(event: any) {
    this.editSelectedVideoFile = event.target.files[0];
    this.editSelectedFile = null;
    if (this.editSelectedVideoFile) {
      this.editPreviewUrl = URL.createObjectURL(this.editSelectedVideoFile);
    }
  }

  uploadEditMedia(post: any) {
    const file = this.editSelectedFile || this.editSelectedVideoFile;
    if (!file) return;

    post.uploadingMedia = true;
    this.postService.uploadFile(file).subscribe({
      next: (res) => {
        if (this.editSelectedFile) {
          post.imageUrl = res.url;
        } else {
          post.videoUrl = res.url;
        }
        post.uploadingMedia = false;
        this.editPreviewUrl = null;
        this.editSelectedFile = null;
        this.editSelectedVideoFile = null;
        this.toast.success('Media uploaded');
      },
      error: () => {
        post.uploadingMedia = false;
        this.toast.error('Failed to upload media');
      }
    });
  }

  // ================= POST MEDIA (DISPLAY MODE) =================

  switchPostMediaMode(post: any, mode: 'generate' | 'image' | 'video') {
    post.mediaMode = mode;
    post.previewUrl = null;
    post.selectedFile = null;
    post.selectedVideoFile = null;
  }

  onPostFileSelected(event: any, post: any) {
    post.selectedFile = event.target.files[0];
    post.selectedVideoFile = null;
    if (post.selectedFile) {
      post.previewUrl = URL.createObjectURL(post.selectedFile);
    }
  }

  onPostVideoSelected(event: any, post: any) {
    post.selectedVideoFile = event.target.files[0];
    post.selectedFile = null;
    post.previewUrl = null;
    if (post.selectedVideoFile) {
      post.previewUrl = URL.createObjectURL(post.selectedVideoFile);
    }
  }

  uploadPostMedia(post: any) {
    const file = post.selectedFile || post.selectedVideoFile;
    if (!file) return;

    post.uploadingMedia = true;
    this.postService.uploadFile(file).subscribe({
      next: (res) => {
        if (post.selectedFile) {
          post.imageUrl = res.url;
        } else {
          post.videoUrl = res.url;
        }
        post.uploadingMedia = false;
        post.previewUrl = null;
        post.selectedFile = null;
        post.selectedVideoFile = null;
        this.toast.success('Media uploaded');
      },
      error: () => {
        post.uploadingMedia = false;
        this.toast.error('Failed to upload media');
      }
    });
  }

  // ================= GENERATE IMAGE =================

  RISKY_KEYWORDS = ['face', 'person', 'people', 'human', 'hand', 'finger', 'text', 'words', 'realistic', 'photo'];

  checkRiskyKeywords(prompt: string): boolean {
    const lower = prompt.toLowerCase();
    return this.RISKY_KEYWORDS.some(kw => lower.includes(kw));
  }

  generateImage(post: any) {
    if (!post.content) return;
    
    post.generatingImage = true;

    this.postService.generateImage(post.id, post.imagePrompt || undefined).subscribe({
      next: (res: any) => {
        post.imageUrl = res.imageUrl || res.url;
        post.generatingImage = false;
      },
      error: () => {
        post.generatingImage = false;
      }
    });
  }

  // ================= SAVE =================

  save(post: any) {
    this.savingPostId = post.id;

    this.postService.updatePost(post.id, {
      title: post.title,
      content: post.content,
      hashtags: post.hashtags,
      platform: post.platform,
      scheduledAt: post.scheduledAt,
      permanent: post.permanent,
      approved: post.approved,
      link: post.link,
      imageUrl: post.imageUrl,
      videoUrl: post.videoUrl
    }).subscribe({
      next: () => {
        this.savingPostId = null;
        this.toast.success('Saved!');
      },
      error: () => {
        this.savingPostId = null;
        this.toast.error('Failed to save');
      }
    });
  }

  // ================= PUBLISH =================

  publishNow(post: any) {
    this.publishingPostId = post.id;

    this.postService.publishPost(post.id).subscribe({
      next: () => {
        post.status = 'PUBLISHED';
        post.publishedAt = new Date();
        this.publishingPostId = null;
        this.toast.success('Published!');
      },
      error: () => {
        this.publishingPostId = null;
        this.toast.error('Failed to publish');
      }
    });
  }

  // ================= AI GENERATION =================

  generate() {
    if (this.showExistingMode && this.campaignId) {
      this.loading = true;
      this.campaignService.generateForExisting(this.campaignId).subscribe({
        next: (res: any) => {
          const arr = Array.isArray(res) ? res : [];
          this.posts = [...this.posts, ...arr.map((p: any) => ({ ...p, imagePrompt: p.image?.imagePrompt || '', mediaMode: 'generate' }))];
          this.loading = false;
        },
        error: (err) => {
          this.loading = false;
          this.toast.error(err.error?.error || err.error?.message || (typeof err.error === 'string' ? err.error : null) || 'Failed to generate posts');
        }
      });
      return;
    }

    this.loading = true;

    this.campaignService.generateCampaign({
      name: this.name,
      topic: this.topic
    }).subscribe({
      next: (res: any) => {
        this.posts = Array.isArray(res) ? res.map((p: any) => ({ ...p, imagePrompt: p.image?.imagePrompt || '', mediaMode: 'generate' })) : [];
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.toast.error(err.error?.error || err.error?.message || (typeof err.error === 'string' ? err.error : null) || 'Failed to generate posts');
      }
    });
  }
}