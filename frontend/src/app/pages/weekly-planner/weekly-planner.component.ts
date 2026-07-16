import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule, Calendar, ChevronRight, Loader, Check } from 'lucide-angular';
import { StrategyService } from '../../services/strategy.service';

@Component({
  selector: 'app-weekly-planner',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  templateUrl: './weekly-planner.component.html',
  styleUrls: ['./weekly-planner.component.css']
})
export class WeeklyPlannerComponent implements OnInit {

  activeStrategy: any = null;
  loading = true;
  generating = false;
  lastResult: any = null;
  error = '';

  icons = { calendar: Calendar, chevronRight: ChevronRight, loader: Loader, check: Check };

  constructor(private strategyService: StrategyService) {}

  ngOnInit() {
    this.loadActiveStrategy();
  }

  loadActiveStrategy() {
    this.loading = true;
    this.strategyService.getActive().subscribe({
      next: (res) => { this.activeStrategy = res; this.loading = false; },
      error: () => { this.loading = false; }
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
