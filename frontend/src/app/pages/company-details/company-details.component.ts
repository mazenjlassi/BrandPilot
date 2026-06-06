import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { PatternService, CompanyProfile } from '../../services/pattern.service';
import { ToastService } from '../../shared/toast/toast.service';
import { ConfirmDialogService } from '../../shared/confirm-dialog/confirm-dialog.service';
import { LoadingSkeletonComponent } from '../../shared/loading-skeleton/loading-skeleton.component';
import { EmptyStateComponent } from '../../shared/empty-state/empty-state.component';
import { LucideAngularModule, ArrowLeft, Save, Trash2, Loader2, Search, ChevronDown, ChevronUp, ExternalLink } from 'lucide-angular';

@Component({
  selector: 'app-company-details',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, LucideAngularModule, LoadingSkeletonComponent, EmptyStateComponent],
  templateUrl: './company-details.component.html',
  styleUrls: ['./company-details.component.css']
})
export class CompanyDetailsComponent implements OnInit {

  companyName = '';
  profile: CompanyProfile | null = null;
  scrapedPosts: any[] = [];
  patterns: any[] = [];
  loading = { profile: false, posts: false, patterns: false };
  scraperLoading = false;
  saving = false;
  error = '';
  expandedPatternId: number | null = null;

  // Editable form copy
  editForm = { instagramUrl: '', facebookUrl: '', linkedinUrl: '' };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private patternService: PatternService,
    private toast: ToastService,
    private confirm: ConfirmDialogService
  ) {}

  goBack() {
    this.router.navigate(['/scraped-posts']);
  }

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      this.companyName = params.get('companyName') || '';
      if (this.companyName) {
        this.loadAll();
      }
    });
  }

  loadAll() {
    this.loadProfile();
    this.loadScrapedPosts();
    this.loadPatterns();
  }

  loadProfile() {
    this.loading.profile = true;
    this.patternService.getCompanyProfileByName(this.companyName).subscribe({
      next: (res) => {
        this.profile = res;
        this.editForm = {
          instagramUrl: res.instagramUrl || '',
          facebookUrl: res.facebookUrl || '',
          linkedinUrl: res.linkedinUrl || ''
        };
        this.loading.profile = false;
      },
      error: () => {
        this.loading.profile = false;
        this.error = 'Company not found';
      }
    });
  }

  loadScrapedPosts() {
    this.loading.posts = true;
    this.patternService.getScrapedPosts(this.companyName).subscribe({
      next: (res) => {
        this.scrapedPosts = res;
        this.loading.posts = false;
      },
      error: () => this.loading.posts = false
    });
  }

  loadPatterns() {
    this.loading.patterns = true;
    this.patternService.getAllPatterns(this.companyName).subscribe({
      next: (res) => {
        this.patterns = res;
        this.loading.patterns = false;
      },
      error: () => this.loading.patterns = false
    });
  }

  saveProfile() {
    if (!this.profile?.id) return;
    this.saving = true;
    const updated: CompanyProfile = {
      id: this.profile.id,
      companyName: this.companyName,
      instagramUrl: this.editForm.instagramUrl,
      facebookUrl: this.editForm.facebookUrl,
      linkedinUrl: this.editForm.linkedinUrl
    };
    this.patternService.updateCompanyProfile(this.profile.id, updated).subscribe({
      next: () => {
        this.saving = false;
        this.toast.success('Company profile updated');
        this.loadProfile();
      },
      error: () => {
        this.saving = false;
        this.toast.error('Failed to update profile');
      }
    });
  }

  launchScraper() {
    if (this.scraperLoading) return;
    this.scraperLoading = true;
    this.error = '';
    this.patternService.triggerScrape(this.companyName).subscribe({
      next: () => {
        this.scraperLoading = false;
        this.toast.success('Scraping completed');
        this.loadScrapedPosts();
      },
      error: (err) => {
        this.scraperLoading = false;
        this.toast.error(err.error?.message || 'Scraping failed');
      }
    });
  }

  async deletePost(id: number) {
    const ok = await this.confirm.confirm({ title: 'Delete Post', message: 'Delete this scraped post?' });
    if (!ok) return;
    this.patternService.deleteScrapedPost(id).subscribe({
      next: () => {
        this.scrapedPosts = this.scrapedPosts.filter(p => p.id !== id);
        this.toast.success('Post deleted');
      },
      error: () => this.toast.error('Failed to delete post')
    });
  }

  async deleteCompany() {
    if (!this.profile?.id) return;
    const ok = await this.confirm.confirm({
      title: 'Delete Company',
      message: `Delete "${this.companyName}" and all its posts? This cannot be undone.`
    });
    if (!ok) return;
    this.patternService.deleteCompanyProfile(this.profile.id).subscribe({
      next: () => {
        this.toast.success('Company deleted');
        this.router.navigate(['/scraped-posts']);
      },
      error: () => this.toast.error('Failed to delete company')
    });
  }

  toggleExpand(id: number) {
    this.expandedPatternId = this.expandedPatternId === id ? null : id;
  }

  getScoreColor(score: number): string {
    if (score >= 0.05) return '#22C55E';
    if (score >= 0.02) return '#EAB308';
    return '#EF4444';
  }

  getScoreLabel(score: number): string {
    if (score >= 0.05) return 'High';
    if (score >= 0.02) return 'Medium';
    return 'Low';
  }

  getPlatformColor(platform: string): string {
    switch(platform?.toLowerCase()) {
      case 'linkedin': return '#0A66C2';
      case 'instagram': return '#E4405F';
      case 'facebook': return '#1877F2';
      default: return '#64748B';
    }
  }

  icons = {
    arrowLeft: ArrowLeft,
    save: Save,
    trash2: Trash2,
    loader2: Loader2,
    search: Search,
    chevronDown: ChevronDown,
    chevronUp: ChevronUp,
    externalLink: ExternalLink
  };
}
