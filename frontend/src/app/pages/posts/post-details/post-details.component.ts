import { Component, OnInit, AfterViewInit, ViewChild, ElementRef, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { PostService } from '../../../services/post.service';
import { CommentService } from '../../../services/comment.service';
import { CommonModule, Location } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import Chart from 'chart.js/auto';
import { LucideAngularModule, ArrowLeft, Trash2, Edit3, Lightbulb, AlertTriangle } from 'lucide-angular';
import { ConfirmDialogService } from '../../../shared/confirm-dialog/confirm-dialog.service';

@Component({
  selector: 'app-post-details',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './post-details.component.html',
  styleUrls: ['./post-details.component.css']
})
export class PostDetailsComponent implements OnInit, AfterViewInit, OnDestroy {

  @ViewChild('metricsChart') chartRef!: ElementRef<HTMLCanvasElement>;
  chart: any;

  post: any;
  loading = true;
  editMode = false;

  saving = false;
  errorMessage = '';
  successMessage = '';
  scheduledDate: string = '';

  comments: any[] = [];
  loadingComments = false;
  sentimentFilter = '';
  metrics: any[] = [];

  constructor(
    private route: ActivatedRoute,
    private service: PostService,
    private commentService: CommentService,
    private http: HttpClient,
    private location: Location,
    private router: Router,
    private confirm: ConfirmDialogService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');

    if (id) {
      const postId = +id;

      this.service.getById(postId).subscribe({
        next: (res: any) => {
          this.post = res;
          this.scheduledDate = this.formatDatetimeLocal(this.post.scheduledAt);
          this.loading = false;

          if (this.post.status === 'PUBLISHED') {
            this.loadComments(postId);
            this.loadMetrics(postId);
          }
        },
        error: () => {
          this.errorMessage = 'Failed to load post';
          this.loading = false;
        }
      });
    }
  }

  ngAfterViewInit() {}

  ngOnDestroy() {
    if (this.chart) {
      this.chart.destroy();
    }
  }

  loadMetrics(postId: number) {
    this.http.get<any[]>(`http://localhost:8081/metrics/post/${postId}`).subscribe({
      next: (res) => {
        this.metrics = res || [];
        setTimeout(() => this.buildChart(), 200);
      },
      error: () => {}
    });
  }

  buildChart() {
    if (!this.chartRef?.nativeElement || this.metrics.length < 2) return;
    if (this.chart) this.chart.destroy();

    const sorted = [...this.metrics].sort((a, b) =>
      new Date(a.collectedAt).getTime() - new Date(b.collectedAt).getTime()
    );

    const labels = sorted.map(m =>
      new Date(m.collectedAt).toLocaleDateString('en-US', { month: 'short', day: 'numeric', hour: '2-digit' })
    );
    const likes = sorted.map(m => m.likes || 0);
    const comments = sorted.map(m => m.comments || 0);

    this.chart = new Chart(this.chartRef.nativeElement, {
      type: 'line',
      data: {
        labels,
        datasets: [
          {
            label: 'Likes',
            data: likes,
            borderColor: '#0F52D6',
            backgroundColor: 'rgba(15, 82, 214, 0.15)',
            borderWidth: 2,
            fill: true,
            tension: 0.4,
            pointBackgroundColor: '#0F52D6',
            pointBorderColor: '#fff',
            pointBorderWidth: 2,
            pointRadius: 4,
            pointHoverRadius: 6,
          },
          {
            label: 'Comments',
            data: comments,
            borderColor: '#8B5CF6',
            backgroundColor: 'rgba(139, 92, 246, 0.15)',
            borderWidth: 2,
            fill: true,
            tension: 0.4,
            pointBackgroundColor: '#8B5CF6',
            pointBorderColor: '#fff',
            pointBorderWidth: 2,
            pointRadius: 4,
            pointHoverRadius: 6,
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        interaction: { mode: 'index', intersect: false },
        scales: {
          x: {
            grid: { display: false },
            ticks: { font: { size: 11 }, color: '#64748B', maxTicksLimit: 8 }
          },
          y: {
            grid: { color: 'rgba(0, 0, 0, 0.05)' },
            ticks: { font: { size: 11 }, color: '#64748B' },
            beginAtZero: true
          }
        },
        plugins: {
          legend: {
            position: 'top',
            align: 'end',
            labels: { usePointStyle: true, pointStyle: 'circle', padding: 20, font: { size: 11 }, color: '#64748B' }
          },
          tooltip: {
            backgroundColor: 'rgba(15, 82, 214, 0.95)',
            titleFont: { size: 12, weight: 'bold' as any },
            bodyFont: { size: 11 },
            padding: 12,
            cornerRadius: 8,
            displayColors: true,
            boxPadding: 6
          }
        },
        animation: { duration: 1500, easing: 'easeOutQuart' }
      }
    });
  }

  // ================= COMMENTS =================

  loadComments(postId: number) {
    this.loadingComments = true;

    this.commentService.getByPost(postId).subscribe({
      next: (res) => {
        this.comments = res;
        this.loadingComments = false;
      },
      error: () => {
        this.loadingComments = false;
      }
    });
  }

  filterSentiment(sentiment: string) {
    this.sentimentFilter = sentiment;
    if (!this.post?.id) return;

    this.loadingComments = true;

    if (sentiment) {
      this.commentService
        .getByPostAndSentiment(this.post.id, sentiment)
        .subscribe({
          next: (res) => {
            this.comments = res;
            this.loadingComments = false;
          },
          error: () => {
            this.loadingComments = false;
          }
        });
    } else {
      this.loadComments(this.post.id);
    }
  }

  resetComments() {
    if (!this.post?.id) return;
    this.loadComments(this.post.id);
  }

  getSentimentClass(sentiment: string): string {
    if (sentiment === 'POSITIVE') return 'positive';
    if (sentiment === 'NEGATIVE') return 'negative';
    return 'neutral';
  }

  // ================= POST =================

  formatDatetimeLocal = (date: any): string => {
    if (!date) return '';
    const d = new Date(date);
    if (isNaN(d.getTime())) return '';
    const pad = (n: number) => n.toString().padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
  }

  canUpdate(): boolean {
    return this.post?.status !== 'PUBLISHED';
  }

  openEdit() {
    this.editMode = true;
    this.errorMessage = '';
    this.successMessage = '';
  }

  updatePost() {
    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';

    const payload = {
      title: this.post.title,
      content: this.post.content,
      hashtags: this.post.hashtags,
      approved: this.post.approved,
      scheduledAt: this.scheduledDate ? new Date(this.scheduledDate).toISOString() : null
    };

    this.service.updatePost(this.post.id, payload).subscribe({
      next: () => {
        this.successMessage = 'Post updated successfully';
        this.editMode = false;
        this.saving = false;
      },
      error: (err) => {
        this.saving = false;
        this.errorMessage = err?.error?.message || 'Update failed';
      }
    });
  }

  async deletePost() {
    const ok = await this.confirm.confirm({ title: 'Delete Post', message: 'Delete this post permanently?' });
    if (!ok) return;
    this.service.deletePost(this.post.id).subscribe({
      next: () => {
        this.router.navigate(['/posts']);
      },
      error: () => {
        this.errorMessage = 'Failed to delete post';
      }
    });
  }

  generatingImage = false;

  imagePrompt = '';

  RISKY_KEYWORDS = ['face', 'person', 'people', 'human', 'hand', 'finger', 'text', 'words', 'realistic', 'photo'];

  riskyKeywordsWarning = false;

  checkRiskyKeywords() {
    const lower = this.imagePrompt.toLowerCase();
    this.riskyKeywordsWarning = this.RISKY_KEYWORDS.some(kw => lower.includes(kw));
  }

  generateImage() {
    this.generatingImage = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.service.generateImage(this.post.id, this.imagePrompt || undefined).subscribe({
      next: (res: any) => {
        this.post.imageUrl = res.imageUrl;
        this.generatingImage = false;
        this.successMessage = 'Image generated successfully';
      },
      error: () => {
        this.generatingImage = false;
        this.errorMessage = 'Image generation failed';
      }
    });
  }

  goBack() {
    this.location.back();
  }

  icons = {
    arrowLeft: ArrowLeft,
    trash2: Trash2,
    edit3: Edit3,
    lightbulb: Lightbulb,
    alertTriangle: AlertTriangle
  };
}
