import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { LucideAngularModule, ArrowLeft, RefreshCw, Trash2, FileText, Heart, MessageCircle, Eye, Edit3, Send, TrendingUp, TrendingDown, Minus, Lightbulb, Sparkles, Check } from 'lucide-angular';
import { CampaignService } from '../../../services/campaign.service';
import { PostService } from '../../../services/post.service';
import { InsightService } from '../../../services/insight.service';
import { CommentService } from '../../../services/comment.service';
import { ConfirmDialogService } from '../../../shared/confirm-dialog/confirm-dialog.service';

@Component({
  selector: 'app-campaign-details',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  templateUrl: './campaign-details.component.html',
  styleUrls: ['./campaign-details.component.css']
})
export class CampaignDetailsComponent implements OnInit {

  icons = {
    arrowLeft: ArrowLeft,
    refreshCw: RefreshCw,
    trash2: Trash2,
    fileText: FileText,
    heart: Heart,
    messageCircle: MessageCircle,
    eye: Eye,
    edit3: Edit3,
    send: Send,
    trendingUp: TrendingUp,
    trendingDown: TrendingDown,
    minus: Minus,
    lightbulb: Lightbulb,
    sparkles: Sparkles,
    check: Check
  };

  campaign: any = null;
  posts: any[] = [];
  insights: any = null;
  campaignComments: any[] = [];

  loading = true;

  private campaignId!: number;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private campaignService: CampaignService,
    private postService: PostService,
    private insightService: InsightService,
    private commentService: CommentService,
    private confirm: ConfirmDialogService
  ) {}

  ngOnInit(): void {
    this.campaignId = Number(this.route.snapshot.params['id']);

    this.loadCampaign();
    this.loadPosts();
    this.loadInsights();
    this.loadCampaignComments();
  }

  // ================= LOADERS =================

  loadCampaign() {
    this.campaignService.getById(this.campaignId).subscribe({
      next: (res) => this.campaign = res,
      error: () => console.error('Failed to load campaign')
    });
  }

  loadPosts() {
    this.postService.getByCampaign(this.campaignId).subscribe({
      next: (res) => {
        this.posts = res;
        this.loading = false;
      },
      error: () => {
        console.error('Failed to load posts');
        this.loading = false;
      }
    });
  }

  loadInsights() {
    this.insightService.getByCampaign(this.campaignId).subscribe({
      next: (res) => this.insights = res,
      error: () => console.error('Failed to load insights')
    });
  }

  loadCampaignComments() {
    this.commentService.getByCampaign(this.campaignId).subscribe({
      next: (res) => {
        this.campaignComments = res || [];
      },
      error: () => {}
    });
  }

  // 🔄 OPTIONAL: reload insights (useful for button later)
  reloadInsights() {
    this.loadInsights();
  }

  // ================= DELETE =================

  async deleteCampaign() {
    const ok = await this.confirm.confirm({ title: 'Delete Campaign', message: 'Delete this campaign and all its posts permanently?' });
    if (!ok) return;
    this.campaignService.deleteCampaign(this.campaignId).subscribe({
      next: () => {
        this.router.navigate(['/campaign-list']);
      },
      error: () => {
        console.error('Failed to delete campaign');
      }
    });
  }

  // ================= NAVIGATION =================

  openPost(id: number) {
    this.router.navigate(['/posts', id]);
  }

  editPost(post: any) {
    post.editing = !post.editing;
  }

  publishPost(post: any) {
    this.postService.updatePost(post.id, { status: 'PUBLISHED' }).subscribe({
      next: () => {
        post.status = 'PUBLISHED';
      }
    });
  }

  goBack() {
    this.router.navigate(['/campaign-list']);
  }

  getSentimentClass(sentiment: string): string {
    if (sentiment === 'POSITIVE') return 'positive';
    if (sentiment === 'NEGATIVE') return 'negative';
    return 'neutral';
  }
}
