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

@Component({
  selector: 'app-platform-chart',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="chart-container">
      <canvas #chartCanvas></canvas>
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
      display: flex;
      align-items: center;
      justify-content: center;
    }
  `]
})
export class PlatformChartComponent implements AfterViewInit, OnDestroy {

  @ViewChild('chartCanvas') chartRef!: ElementRef<HTMLCanvasElement>;
  chart: any;

  constructor(private postService: PostService) {}

  ngAfterViewInit() {
    setTimeout(() => this.loadData(), 100);
  }

  ngOnDestroy() {
    if (this.chart) {
      this.chart.destroy();
      this.chart = null;
    }
  }

  loadData() {
    if (!this.chartRef?.nativeElement) return;
    
    this.postService.getPostStats().subscribe(stats => {
      const fb = stats.facebookPosts || 0;
      const ig = stats.instagramPosts || 0;
      const li = stats.linkedinPosts || 0;
      const total = fb + ig + li;
      this.createDonutChart(fb, ig, li, total);
    });
  }

  createDonutChart(fb: number, ig: number, li: number, total: number) {
    if (this.chart) {
      this.chart.destroy();
    }

    if (!this.chartRef?.nativeElement) return;

    const platformColors = ['#1877F2', '#E1306C', '#0A66C2'];

    this.chart = new Chart(this.chartRef.nativeElement, {
      type: 'doughnut',
      data: {
        labels: ['Facebook', 'Instagram', 'LinkedIn'],
        datasets: [{
          data: [fb || 1, ig || 1, li || 1],
          backgroundColor: platformColors,
          borderWidth: 0,
          hoverOffset: 10
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        cutout: '60%',
        plugins: {
          legend: {
            position: 'bottom',
            labels: { 
              usePointStyle: true, 
              padding: 20, 
              font: { size: 11 },
              color: '#64748B'
            }
          },
          tooltip: {
            backgroundColor: 'rgba(30, 167, 255, 0.95)',
            titleFont: { size: 12, weight: 'bold' },
            bodyFont: { size: 11 },
            padding: 12,
            cornerRadius: 8,
            callbacks: {
              label: (context: any) => {
                const value = context.raw as number;
                const percentage = total > 0 ? ((value / total) * 100).toFixed(1) : 0;
                return ` ${value} posts (${percentage}%)`;
              }
            }
          }
        },
        animation: {
          animateRotate: true,
          animateScale: true,
          duration: 1000,
          easing: 'easeOutQuart'
        }
      },
      plugins: [{
        id: 'platformCenterText',
        beforeDraw: (chart: any) => {
          const { ctx, width, height } = chart;
          ctx.restore();
          const fontSize = (height / 140).toFixed(2);
          ctx.font = `bold ${fontSize}em sans-serif`;
          ctx.textBaseline = 'middle';
          ctx.fillStyle = '#0F52D6';
          const text = total.toString();
          const textWidth = ctx.measureText(text).width;
          ctx.fillText(text, width / 2 - textWidth / 2, height / 2 - 10);
          
          ctx.font = `${parseFloat(fontSize) * 0.5}em sans-serif`;
          ctx.fillStyle = '#64748B';
          const label = 'Total Posts';
          const labelWidth = ctx.measureText(label).width;
          ctx.fillText(label, width / 2 - labelWidth / 2, height / 2 + 15);
          ctx.save();
        }
      }]
    });
  }
}