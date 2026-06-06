import { Component, OnInit } from '@angular/core';
import { CampaignService } from '../../services/campaign.service';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule, Search, AlertTriangle, FolderOpen, ArrowUpRight } from 'lucide-angular';

@Component({
  selector: 'app-campaign-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, LucideAngularModule],
  templateUrl: './campaign-list.component.html',
  styleUrls: ['./campaign-list.component.css']
})
export class CampaignListComponent implements OnInit {

  campaigns: any[] = [];
  loading = true;
  error = '';

  icons = {
    search: Search,
    alertTriangle: AlertTriangle,
    folderOpen: FolderOpen,
    arrowUpRight: ArrowUpRight
  };

  // ================= FILTERS =================

  searchQuery = '';
  statusFilter = '';
  sortBy = 'newest';

  // ================= FILTERED CAMPAIGNS =================

  get filteredCampaigns() {
    let result = this.campaigns;
    
    if (this.searchQuery) {
      const q = this.searchQuery.toLowerCase();
      result = result.filter(c => 
        c.name?.toLowerCase().includes(q) ||
        c.topic?.toLowerCase().includes(q)
      );
    }
    
    if (this.statusFilter) {
      result = result.filter(c => c.status?.toLowerCase() === this.statusFilter);
    }
    
    // Sort
    if (this.sortBy === 'newest') {
      result = [...result].sort((a, b) => {
        const dateA = new Date(a.createdAt).getTime();
        const dateB = new Date(b.createdAt).getTime();
        return dateB - dateA;
      });
    } else if (this.sortBy === 'oldest') {
      result = [...result].sort((a, b) => {
        const dateA = new Date(a.createdAt).getTime();
        const dateB = new Date(b.createdAt).getTime();
        return dateA - dateB;
      });
    } else if (this.sortBy === 'name') {
      result = [...result].sort((a, b) => a.name.localeCompare(b.name));
    }
    
    return result;
  }

  constructor(
    private campaignService: CampaignService,
    private router: Router // ✅ ADD THIS
  ) {}

  ngOnInit(): void {
    this.campaignService.getAll().subscribe({
      next: (res) => {
        this.campaigns = res;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load campaigns';
        this.loading = false;
      }
    });
  }

  openCampaign(event: Event, id: number) {
    event.stopPropagation();
    this.router.navigate(['/campaigns', id]);
  }
}