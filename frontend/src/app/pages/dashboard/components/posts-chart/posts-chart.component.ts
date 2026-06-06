import {
  Component,
  AfterViewInit,
  ViewChild,
  ElementRef,
  OnDestroy
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { PostService } from '../../../../services/post.service';
import Chart from 'chart.js/auto';
import { LucideAngularModule, BarChart3 } from 'lucide-angular';

@Component({
  selector: 'app-posts-chart',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  template: `
    <div class="chart-container">
      <div *ngIf="empty" class="chart-empty">
        <lucide-icon [img]="barChart3" [size]="36" class="empty-icon"></lucide-icon>
        <p class="empty-title">No post data yet</p>
        <p class="empty-desc">Create and publish posts to see engagement analytics here</p>
      </div>
      <canvas #chartCanvas [class.hidden]="empty"></canvas>
    </div>
  `,
  styles: [`
    :host {
      display: block;
      width: 100%;
      height: 100%;
      min-height: 200px;
    }
    .chart-container {
      width: 100%;
      height: 100%;
      position: relative;
    }
    .chart-empty {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 100%;
      min-height: 200px;
      text-align: center;
      gap: 8px;
      padding: 40px 24px;
    }
    .empty-icon {
      color: rgba(255,255,255,0.12);
      margin-bottom: 4px;
    }
    body:not(.dark-mode) .empty-icon {
      color: rgba(0,0,0,0.12);
    }
    .empty-title {
      margin: 0;
      font-size: var(--text-base);
      font-weight: var(--font-semibold);
      color: var(--text-primary);
    }
    .empty-desc {
      margin: 0;
      font-size: var(--text-sm);
      color: var(--text-secondary);
      max-width: 280px;
      line-height: 1.5;
    }
    .hidden { display: none; }
  `]
})
export class PostsChartComponent implements AfterViewInit, OnDestroy {

  @ViewChild('chartCanvas') chartRef!: ElementRef<HTMLCanvasElement>;
  chart: any;
  empty = false;
  barChart3 = BarChart3;

  constructor(private postService: PostService) {}

  ngAfterViewInit() {
    setTimeout(() => this.loadChart(), 100);
  }

  ngOnDestroy() {
    if (this.chart) {
      this.chart.destroy();
    }
  }

  loadChart() {
    if (!this.chartRef?.nativeElement) return;

    this.postService.getLatestPosts(20).subscribe(posts => {
      if (!posts || posts.length === 0) {
        this.empty = true;
        return;
      }

      // Get posts sorted by date (most recent first)
      const sortedPosts = [...posts].sort((a, b) => {
        const dateA = new Date(a.publishedAt || a.createdAt || 0).getTime();
        const dateB = new Date(b.publishedAt || b.createdAt || 0).getTime();
        return dateB - dateA;
      });

      // Use last 7 posts for the chart
      const chartPosts = sortedPosts.slice(0, 7).reverse();
      
      const labels = chartPosts.map((p, i) => `Post ${i + 1}`);
      const likes = chartPosts.map(p => p.likes || 0);
      const comments = chartPosts.map(p => p.commentsCount || 0);

      const hasData = likes.some(l => l > 0) || comments.some(c => c > 0);

      if (hasData) {
        this.createAreaChart(labels, likes, comments);
      } else {
        this.empty = true;
      }
    });
  }

  createAreaChart(labels: string[], likes: number[], comments: number[]) {
    if (this.chart) {
      this.chart.destroy();
    }

    if (!this.chartRef?.nativeElement) return;

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
        interaction: {
          mode: 'index',
          intersect: false,
        },
        scales: {
          x: {
            grid: {
              display: false
            },
            ticks: {
              font: { size: 11 },
              color: '#64748B'
            }
          },
          y: {
            grid: {
              color: 'rgba(0, 0, 0, 0.05)'
            },
            ticks: {
              font: { size: 11 },
              color: '#64748B'
            },
            beginAtZero: true
          }
        },
        plugins: {
          legend: {
            position: 'top',
            align: 'end',
            labels: { 
              usePointStyle: true, 
              pointStyle: 'circle',
              padding: 20, 
              font: { size: 11 },
              color: '#64748B'
            }
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
        animation: {
          duration: 1500,
          easing: 'easeOutQuart'
        }
      }
    });
  }

}