import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PatternService } from '../../services/pattern.service';
import { ConfirmDialogService } from '../../shared/confirm-dialog/confirm-dialog.service';
import { LoadingSkeletonComponent } from '../../shared/loading-skeleton/loading-skeleton.component';
import { EmptyStateComponent } from '../../shared/empty-state/empty-state.component';
import { LucideAngularModule, Play, Trash2, ChevronUp, ChevronDown } from 'lucide-angular';

@Component({
  selector: 'app-patterns',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule, LoadingSkeletonComponent, EmptyStateComponent],
  templateUrl: './patterns.component.html',
  styleUrls: ['./patterns.component.css']
})
export class PatternsComponent implements OnInit {

  companies: string[] = [];
  selectedCompany: string = '';
  patterns: any[] = [];
  expandedId: number | null = null;
  loading = false;
  error = '';
  batchResult = '';
  unanalyzedCount = 0;

  constructor(
    private patternService: PatternService,
    private confirm: ConfirmDialogService
  ) {}

  ngOnInit() {
    this.loadCompanies();
  }

  loadCompanies() {
    this.patternService.getCompanies().subscribe({
      next: (res) => {
        this.companies = res;
        if (this.companies.length > 0 && !this.selectedCompany) {
          this.selectCompany(this.companies[0]);
        }
      },
      error: () => {}
    });
  }

  selectCompany(name: string) {
    this.selectedCompany = name;
    this.batchResult = '';
    this.loadPatterns();
    this.loadUnanalyzedCount();
  }

  loadPatterns() {
    if (!this.selectedCompany) return;
    this.loading = true;
    this.patternService.getAllPatterns(this.selectedCompany).subscribe({
      next: (res) => {
        this.patterns = res;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.error = 'Failed to load patterns';
      }
    });
  }

  loadUnanalyzedCount() {
    if (!this.selectedCompany) return;
    this.patternService.getScrapedCount(this.selectedCompany).subscribe({
      next: (res) => {
        this.unanalyzedCount = 0;
      },
      error: () => {}
    });
  }

  toggleExpand(id: number) {
    this.expandedId = this.expandedId === id ? null : id;
  }

  runBatchAnalysis() {
    if (!this.selectedCompany) return;
    this.batchResult = 'Running...';
    this.patternService.runBatchAnalysis(this.selectedCompany).subscribe({
      next: (res) => {
        this.batchResult = typeof res === 'string' ? res : 'Batch analysis complete';
        this.loadPatterns();
      },
      error: () => {
        this.batchResult = 'Batch analysis failed';
      }
    });
  }

  async deletePattern(id: number) {
    const ok = await this.confirm.confirm({ title: 'Delete Pattern', message: 'Delete this pattern?' });
    if (!ok) return;
    this.patternService.deletePattern(id).subscribe({
      next: () => {
        this.patterns = this.patterns.filter(p => p.id !== id);
      },
      error: () => {
        this.error = 'Failed to delete pattern';
      }
    });
  }

  parsePlatformBreakdown(breakdown: any): {key: string, value: any}[] {
    if (!breakdown) return [];
    try {
      const obj = typeof breakdown === 'string' ? JSON.parse(breakdown) : breakdown;
      return Object.entries(obj).map(([key, value]) => ({ key, value }));
    } catch {
      return [];
    }
  }

  parseUsedPostIds(ids: any): number[] {
    if (!ids) return [];
    try {
      return typeof ids === 'string' ? JSON.parse(ids) : ids;
    } catch {
      return [];
    }
  }

  getScoreColor(score: number): string {
    if (score >= 0.7) return '#16A34A';
    if (score >= 0.4) return '#F59E0B';
    return '#EF4444';
  }

  getScoreLabel(score: number): string {
    if (score >= 0.7) return 'High';
    if (score >= 0.4) return 'Medium';
    return 'Low';
  }

  icons = {
    play: Play,
    trash2: Trash2,
    chevronUp: ChevronUp,
    chevronDown: ChevronDown
  };
}
