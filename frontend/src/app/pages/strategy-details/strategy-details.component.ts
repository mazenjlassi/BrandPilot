import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import {
  LucideAngularModule,
  ArrowLeft,
  Check,
  X,
  Loader,
  PenLine,
  Sparkles
} from 'lucide-angular';
import { StrategyService } from '../../services/strategy.service';

@Component({
  selector: 'app-strategy-details',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, LucideAngularModule],
  templateUrl: './strategy-details.component.html',
  styleUrls: ['./strategy-details.component.css']
})
export class StrategyDetailsComponent implements OnInit {

  strategy: any = null;
  loading = true;
  editing = false;
  saving = false;
  error = '';

  editTitle = '';
  editSummary = '';
  editDescription = '';
  editDurationWeeks = 8;
  editStartDate = '';
  editEndDate = '';
  editCampaignCount = 0;
  editManagerNotes = '';

  icons = {
    arrowLeft: ArrowLeft,
    check: Check,
    x: X,
    loader: Loader,
    penLine: PenLine,
    sparkles: Sparkles
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private strategyService: StrategyService
  ) {}

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) this.loadStrategy(+id);
  }

  loadStrategy(id: number) {
    this.loading = true;
    this.strategyService.getById(id).subscribe({
      next: (res) => {
        this.strategy = res;
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  startEdit() {
    this.editing = true;
    this.editTitle = this.strategy.title;
    this.editSummary = this.strategy.summary;
    this.editDescription = this.strategy.description;
    this.editDurationWeeks = this.strategy.durationWeeks || 8;
    this.editStartDate = this.strategy.startDate || '';
    this.editEndDate = this.strategy.expectedEndDate || '';
    this.editCampaignCount = this.strategy.campaignCount || 0;
    this.editManagerNotes = this.strategy.managerNotes || '';
  }

  cancelEdit() {
    this.editing = false;
    this.error = '';
  }

  save() {
    this.saving = true;
    this.error = '';
    const payload: any = {
      title: this.editTitle,
      summary: this.editSummary,
      description: this.editDescription,
      durationWeeks: this.editDurationWeeks,
      managerNotes: this.editManagerNotes
    };
    if (this.editStartDate) payload.startDate = this.editStartDate;
    if (this.editEndDate) payload.expectedEndDate = this.editEndDate;
    if (this.editCampaignCount > 0) payload.campaignCount = this.editCampaignCount;
    this.strategyService.update(this.strategy.id, payload).subscribe({
      next: (res) => {
        this.strategy = res;
        this.editing = false;
        this.saving = false;
      },
      error: (err) => {
        this.saving = false;
        this.error = err.error?.message || 'Update failed';
      }
    });
  }

  approve() {
    this.strategyService.approve(this.strategy.id).subscribe(() => this.loadStrategy(this.strategy.id));
  }

  deactivate() {
    this.strategyService.deactivate(this.strategy.id).subscribe(() => this.loadStrategy(this.strategy.id));
  }

  getMapEntries(map: any): { key: string; value: any }[] {
    if (!map) return [];
    return Object.keys(map).map(key => ({ key, value: map[key] }));
  }
}
