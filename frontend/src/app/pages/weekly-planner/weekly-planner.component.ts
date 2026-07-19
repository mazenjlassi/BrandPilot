import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import {
  LucideAngularModule,
  Calendar,
  ChevronRight,
  Loader,
  Check,
  ArrowUpRight,
  CheckCircle,
  FolderOpen,
  FileText,
  AlertTriangle
} from 'lucide-angular';
import { StrategyService } from '../../services/strategy.service';
import { CampaignService } from '../../services/campaign.service';

@Component({
  selector: 'app-weekly-planner',
  standalone: true,
  imports: [CommonModule, RouterModule, LucideAngularModule],
  templateUrl: './weekly-planner.component.html',
  styleUrls: ['./weekly-planner.component.css']
})
export class WeeklyPlannerComponent implements OnInit {

  activeStrategy: any = null;
  campaigns: any[] = [];
  loading = true;
  generating = false;
  lastResult: any = null;
  error = '';

  icons = {
    calendar: Calendar,
    chevronRight: ChevronRight,
    loader: Loader,
    check: Check,
    arrowUpRight: ArrowUpRight,
    checkCircle: CheckCircle,
    folderOpen: FolderOpen,
    fileText: FileText,
    alertTriangle: AlertTriangle
  };

  constructor(
    private strategyService: StrategyService,
    private campaignService: CampaignService
  ) {}

  ngOnInit() {
    this.loadActiveStrategy();
  }

  get totalPosts(): number {
    return this.campaigns.reduce((sum: number, c: any) => sum + (c.postCount || 0), 0);
  }

  get progressPercent(): number {
    if (!this.activeStrategy?.durationWeeks) return 0;
    return Math.round((this.calculateWeek() / this.activeStrategy.durationWeeks) * 100);
  }

  loadActiveStrategy() {
    this.loading = true;
    this.strategyService.getActive().subscribe({
      next: (res) => {
        this.activeStrategy = res;
        this.loading = false;
        if (res?.id) {
          this.loadCampaigns(res.id);
        }
      },
      error: () => { this.loading = false; }
    });
  }

  loadCampaigns(strategyId: number) {
    this.strategyService.getStrategyCampaigns(strategyId).subscribe({
      next: (res) => { this.campaigns = res; },
      error: () => {}
    });
  }

  generateWeek() {
    if (!this.activeStrategy) return;
    this.generating = true;
    this.error = '';
    this.lastResult = null;
    this.strategyService.generateWeek(this.activeStrategy.id).subscribe({
      next: (res) => {
        this.lastResult = res;
        this.campaigns = res.campaigns || [];
        this.generating = false;
        this.loadActiveStrategy();
      },
      error: (err) => {
        this.generating = false;
        this.error = 'Generation failed: ' + (err.error?.message || err.message);
      }
    });
  }

  calculateWeek(): number {
    if (!this.activeStrategy?.startDate || !this.activeStrategy?.durationWeeks) return 0;
    const start = new Date(this.activeStrategy.startDate);
    const now = new Date();
    const diff = Math.floor((now.getTime() - start.getTime()) / (7 * 24 * 60 * 60 * 1000)) + 1;
    return Math.max(1, Math.min(diff, this.activeStrategy.durationWeeks));
  }
}
