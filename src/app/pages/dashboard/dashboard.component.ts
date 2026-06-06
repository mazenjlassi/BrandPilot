import { Component, OnInit } from '@angular/core';

import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { LucideAngularModule, FileText, Send, Lightbulb, MessageSquare, TrendingUp, Users, PenTool, Rocket, Clock, Calendar, BarChart3, Activity, ArrowRight } from 'lucide-angular';
import { PostsChartComponent } from './components/posts-chart/posts-chart.component';

import { PostService } from '../../services/post.service';
import { CampaignService } from '../../services/campaign.service';
import { AuthService } from '../../services/auth.service';
import { AdminService } from '../../services/admin.service';
import { PatternService } from '../../services/pattern.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    LucideAngularModule,
    PostsChartComponent
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {

  posts: any[] = [];
  campaigns: any[] = [];
  stats: any = {
    totalPosts: 0,
    publishedPosts: 0,
    activeCampaigns: 0
  };

  loading = true;
  recentActivity: any[] = [];

  // Marketing features
  timingAnalysis: any = null;
  weeklyComparison: any = null;
  upcomingPosts: any[] = [];
  showMarketing = false;
  isMarketingUser = false;

  // Admin features
  userStats: any = null;
  campaignProgress: any[] = [];
  showAdmin = false;

  patternStats = {
    totalPatterns: 0,
    bestTopic: '',
    bestScore: 0
  };

  icons = {
    fileText: FileText,
    send: Send,
    lightbulb: Lightbulb,
    messageSquare: MessageSquare,
    trendingUp: TrendingUp,
    users: Users,
    penTool: PenTool,
    rocket: Rocket,
    clock: Clock,
    calendar: Calendar,
    barChart: BarChart3,
    activity: Activity,
    arrowRight: ArrowRight
  };

  constructor(
    private postService: PostService,
    private campaignService: CampaignService,
    private authService: AuthService,
    private adminService: AdminService,
    private patternService: PatternService
  ) {}

  ngOnInit() {
    this.loadPosts();
    this.loadStats();
    this.loadCampaigns();
    this.loadRecentActivity();

    this.isMarketingUser = this.authService.isMarketing();
    const isAdminUser = this.authService.isAdmin();

    if (this.isMarketingUser) {
      this.showMarketing = true;
      this.loadMarketingFeatures();
    }

    if (isAdminUser) {
      this.showAdmin = true;
      this.loadAdminFeatures();
    }
  }

  loadMarketingFeatures() {
    this.loadTimingAnalysis();
    this.loadWeeklyComparison();
    this.loadUpcomingScheduled();
  }

  loadAdminFeatures() {
    this.loadUserStats();
    this.loadCampaignProgress();
    this.loadPatternStats();
  }

  loadTimingAnalysis() {
    this.postService.getTimingAnalysis().subscribe({
      next: (res) => { this.timingAnalysis = res; },
      error: () => { this.timingAnalysis = null; }
    });
  }

  loadWeeklyComparison() {
    this.postService.getWeeklyComparison().subscribe({
      next: (res) => { this.weeklyComparison = res; },
      error: () => { this.weeklyComparison = null; }
    });
  }

  loadUpcomingScheduled() {
    this.postService.getUpcomingScheduled(3).subscribe({
      next: (res) => { this.upcomingPosts = res; },
      error: () => { this.upcomingPosts = []; }
    });
  }

  loadUserStats() {
    this.adminService.getUserStats().subscribe({
      next: (res) => { this.userStats = res; },
      error: () => { this.userStats = null; }
    });
  }

  loadCampaignProgress() {
    this.adminService.getCampaignsProgress(3).subscribe({
      next: (res) => { this.campaignProgress = res; },
      error: () => { this.campaignProgress = []; }
    });
  }

  loadPatternStats() {
    this.patternService.getAllPatterns().subscribe({
      next: (res) => {
        this.patternStats.totalPatterns = res.length;
        const withScores = res.filter(p => p.avgEngagementScore != null);
        if (withScores.length > 0) {
          const best = withScores.reduce((a, b) => (a.avgEngagementScore > b.avgEngagementScore ? a : b));
          this.patternStats.bestTopic = best.topic;
          this.patternStats.bestScore = best.avgEngagementScore;
        }
      },
      error: () => {}
    });
  }

  loadPosts() {
    this.postService.getTopPosts().subscribe({
      next: (res) => {
        this.posts = res;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  loadStats() {
    this.postService.getPostStats().subscribe({
      next: (res) => {
        this.stats = {
          totalPosts: res.totalPosts || res.total || 0,
          publishedPosts: res.publishedPosts || res.published || 0,
          activeCampaigns: res.campaigns || 0,
          draftPosts: res.draftPosts || 0,
          approvedPosts: res.approvedPosts || 0
        };
      },
      error: () => {
        this.stats = {
          totalPosts: 0,
          publishedPosts: 0,
          activeCampaigns: 0
        };
      }
    });
  }

  loadCampaigns() {
    this.campaignService.getAll().subscribe({
      next: (res) => {
        this.campaigns = res.slice(0, 4);
      },
      error: () => {}
    });
  }

  loadRecentActivity() {
    this.recentActivity = [];
  }

  getPostTrend(): number {
    if (!this.stats?.totalPosts || this.stats.totalPosts === 0) return 0;
    return 0;
  }

  getEngagementRate(): number {
    if (!this.stats?.publishedPosts || this.stats.publishedPosts === 0) return 0;
    return 0;
  }

  getQuickInsight(): string {
    if (!this.posts || this.posts.length === 0) {
      return 'No strong performing posts yet. Start publishing consistently to unlock insights and understand your audience better.';
    }

    const totalLikes = this.posts.reduce((sum, p: any) => sum + (p.likes || 0), 0);
    const totalComments = this.posts.reduce((sum, p: any) => sum + (p.commentsCount || 0), 0);

    const avgLikes = totalLikes / this.posts.length;
    const avgComments = totalComments / this.posts.length;

    if (avgLikes > 100 || avgComments > 20) {
      return 'Your content is performing strongly. Posts are generating solid engagement.';
    }

    if (avgLikes > 40 || avgComments > 5) {
      return 'Your content shows moderate engagement. Consistency can be improved.';
    }

    return 'Engagement levels are low. Focus on clearer messaging and more engaging content.';
  }

  get userName(): string {
    return this.authService.getName() || 'User';
  }
}