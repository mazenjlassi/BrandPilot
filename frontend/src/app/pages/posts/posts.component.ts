import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PostService } from '../../services/post.service';
import { Router } from '@angular/router';
import { ToastService } from '../../shared/toast/toast.service';
import { ConfirmDialogService } from '../../shared/confirm-dialog/confirm-dialog.service';
import { LucideAngularModule, Eye, Clock, CheckCircle, Send, FileText, Image, Search, Loader2 } from 'lucide-angular';

@Component({
  selector: 'app-posts',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './posts.component.html',
  styleUrls: ['./posts.component.css']
})
export class PostsComponent implements OnInit {

  posts: any[] = [];
  activeTab: string = 'drafts';
  loading = false;
  
  // Search
  searchQuery = '';
  
  // Filtered posts based on search
  get filteredPosts() {
    if (!this.searchQuery) return this.posts;
    
    const q = this.searchQuery.toLowerCase();
    return this.posts.filter(p => 
      p.content?.toLowerCase().includes(q) ||
      p.platform?.toLowerCase().includes(q)
    );
  }

  constructor(
    private service: PostService,
    private router: Router,
    private toast: ToastService,
    private confirm: ConfirmDialogService
  ) {}

  ngOnInit() {
    this.loadPosts();
  }

  setTab(tab: string) {
    this.activeTab = tab;
    this.searchQuery = ''; // Clear search on tab change
    this.loadPosts();
  }

  loadPosts() {
    this.loading = true;

    let request;

    if (this.activeTab === 'drafts') {
      request = this.service.getDrafts();
    } else if (this.activeTab === 'scheduled') {
      request = this.service.getScheduled();
    } else if (this.activeTab === 'permanent') {
      request = this.service.getPermanent();
    } else {
      request = this.service.getPublished();
    }

    request.subscribe({
      next: (res: any) => {
        console.log('Posts loaded:', this.activeTab, res);
        this.posts = res;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading posts:', err);
        this.loading = false;
      }
    });
  }

  viewDetails(id: number) {
    this.router.navigate(['/posts', id]);
  }

  canEdit(post: any): boolean {
    return post.permanent || post.status !== 'PUBLISHED';
  }

  async postNow(post: any) {
    const ok = await this.confirm.confirm({ title: 'Publish Post', message: 'Post this to social media now?' });
    if (!ok) return;

    this.service.publishPost(post.id).subscribe({
      next: () => {
        post.status = 'PUBLISHED';
        this.toast.success('Posted successfully!');
      },
      error: () => {
        this.toast.error('Failed to post');
      }
    });
  }

  icons = {
    eye: Eye,
    clock: Clock,
    checkCircle: CheckCircle,
    send: Send,
    fileText: FileText,
    image: Image,
    search: Search,
    loader2: Loader2
  };
}