import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { PatternService, CompanyProfile } from '../../services/pattern.service';
import { ConfirmDialogService } from '../../shared/confirm-dialog/confirm-dialog.service';
import { LoadingSkeletonComponent } from '../../shared/loading-skeleton/loading-skeleton.component';
import { EmptyStateComponent } from '../../shared/empty-state/empty-state.component';
import { LucideAngularModule, Trash2, X, Loader2, Search } from 'lucide-angular';

@Component({
  selector: 'app-scraped-posts',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, LucideAngularModule, LoadingSkeletonComponent, EmptyStateComponent],
  templateUrl: './scraped-posts.component.html',
  styleUrls: ['./scraped-posts.component.css']
})
export class ScrapedPostsComponent implements OnInit {

  companies: CompanyProfile[] = [];
  selectedCompany: string = '';
  scrapedPosts: any[] = [];
  loading = false;
  scraperLoading = false;
  error = '';
  showAddModal = false;
  showCompanyModal = false;

  newPost: any = {
    companyName: '',
    platform: 'LINKEDIN',
    postText: '',
    postUrl: '',
    postedAt: ''
  };

  newCompany: CompanyProfile = {
    companyName: '',
    instagramUrl: '',
    facebookUrl: '',
    linkedinUrl: ''
  };

  constructor(
    private patternService: PatternService,
    private confirm: ConfirmDialogService
  ) {}

  ngOnInit() {
    this.loadCompanies();
  }

  get companyNames(): string[] {
    return this.companies.map(c => c.companyName);
  }

  loadCompanies() {
    this.patternService.getCompanyProfiles().subscribe({
      next: (res) => {
        this.companies = res;
        if (this.companies.length > 0 && !this.selectedCompany) {
          this.selectCompany(this.companies[0].companyName);
        }
      },
      error: () => {}
    });
  }

  selectCompany(name: string) {
    this.selectedCompany = name;
    this.loadScrapedPosts();
  }

  loadScrapedPosts() {
    if (!this.selectedCompany) return;
    this.loading = true;
    this.patternService.getScrapedPosts(this.selectedCompany).subscribe({
      next: (res) => {
        this.scrapedPosts = res;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.error = 'Failed to load scraped posts';
      }
    });
  }

  openAddModal() {
    this.newPost = {
      companyName: this.selectedCompany || '',
      platform: 'LINKEDIN',
      postText: '',
      postUrl: '',
      postedAt: ''
    };
    this.showAddModal = true;
  }

  createPost() {
    if (!this.newPost.postText || !this.newPost.companyName) return;

    const payload: any = {
      companyName: this.newPost.companyName,
      platform: this.newPost.platform,
      postText: this.newPost.postText,
    };
    if (this.newPost.postUrl) payload.postUrl = this.newPost.postUrl;
    if (this.newPost.postedAt) payload.postedAt = this.newPost.postedAt;

    this.patternService.createScrapedPost(payload).subscribe({
      next: (res) => {
        this.scrapedPosts.unshift(res);
        this.showAddModal = false;
        if (!this.companyNames.includes(res.companyName)) {
          this.loadCompanies();
        }
        if (!this.selectedCompany) {
          this.selectCompany(res.companyName);
        }
      },
      error: () => {
        this.error = 'Failed to create post';
      }
    });
  }

  openCompanyModal() {
    this.newCompany = {
      companyName: '',
      instagramUrl: '',
      facebookUrl: '',
      linkedinUrl: ''
    };
    this.showCompanyModal = true;
  }

  createCompany() {
    if (!this.newCompany.companyName || !this.newCompany.instagramUrl ||
        !this.newCompany.facebookUrl || !this.newCompany.linkedinUrl) return;

    this.patternService.createCompanyProfile(this.newCompany).subscribe({
      next: (res) => {
        this.companies.push(res);
        this.selectCompany(res.companyName);
        this.showCompanyModal = false;
      },
      error: (err) => {
        this.error = err.error || 'Failed to create company';
      }
    });
  }

  launchScraper() {
    if (!this.selectedCompany || this.scraperLoading) return;
    this.scraperLoading = true;
    this.error = '';
    this.patternService.triggerScrape(this.selectedCompany).subscribe({
      next: () => {
        this.scraperLoading = false;
        this.loadScrapedPosts();
      },
      error: (err) => {
        this.scraperLoading = false;
        this.error = err.error?.message || 'Scraping failed';
      }
    });
  }

  async deletePost(id: number) {
    const ok = await this.confirm.confirm({ title: 'Delete Post', message: 'Delete this scraped post?' });
    if (!ok) return;
    this.patternService.deleteScrapedPost(id).subscribe({
      next: () => {
        this.scrapedPosts = this.scrapedPosts.filter(p => p.id !== id);
      },
      error: () => {
        this.error = 'Failed to delete post';
      }
    });
  }

  async deleteCompany(profile: CompanyProfile) {
    const ok = await this.confirm.confirm({
      title: 'Delete Company',
      message: `Delete "${profile.companyName}" and all its posts?`
    });
    if (!ok) return;
    this.patternService.deleteCompanyProfile(profile.id!).subscribe({
      next: () => {
        this.companies = this.companies.filter(c => c.id !== profile.id);
        if (this.selectedCompany === profile.companyName) {
          this.selectedCompany = this.companies.length > 0 ? this.companies[0].companyName : '';
          if (this.selectedCompany) this.loadScrapedPosts();
        }
      },
      error: () => {
        this.error = 'Failed to delete company';
      }
    });
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
    trash2: Trash2,
    x: X,
    loader2: Loader2,
    search: Search
  };
}