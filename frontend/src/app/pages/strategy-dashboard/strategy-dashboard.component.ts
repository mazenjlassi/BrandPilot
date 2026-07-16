import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { LucideAngularModule, Check, X, TriangleAlert } from 'lucide-angular';
import { StrategyService } from '../../services/strategy.service';

@Component({
  selector: 'app-strategy-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, LucideAngularModule],
  templateUrl: './strategy-dashboard.component.html',
  styleUrls: ['./strategy-dashboard.component.css']
})
export class StrategyDashboardComponent implements OnInit {

  strategies: any[] = [];
  loading = true;
  error = '';
  loadError = '';

  icons = { check: Check, x: X, triangleAlert: TriangleAlert };

  constructor(private strategyService: StrategyService) {}

  ngOnInit() {
    this.loadStrategies();
  }

  loadStrategies() {
    this.loading = true;
    this.strategyService.getAll().subscribe({
      next: (res) => { this.strategies = res; this.loading = false; this.loadError = ''; },
      error: (err) => { this.loading = false; this.loadError = err.error?.message || 'Failed to load strategies'; }
    });
  }

  approve(id: number) {
    this.error = '';
    this.strategyService.approve(id).subscribe({
      next: () => this.loadStrategies(),
      error: (err) => { this.error = err.error?.message || 'Approval failed'; }
    });
  }

  deactivate(id: number) {
    this.error = '';
    this.strategyService.deactivate(id).subscribe({
      next: () => this.loadStrategies(),
      error: (err) => { this.error = err.error?.message || 'Deactivation failed'; }
    });
  }

  statusClass(status: string): string {
    return status?.toLowerCase() || '';
  }
}
