import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule, UserPlus, Trash2, Ban, Unlock, Loader2 } from 'lucide-angular';
import { ConfirmDialogService } from '../../shared/confirm-dialog/confirm-dialog.service';
import { EmptyStateComponent } from '../../shared/empty-state/empty-state.component';

interface User {
  id: number;
  name: string;
  email: string;
  role: string;
  banned: boolean;
}

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule, EmptyStateComponent],
  templateUrl: './user-management.component.html',
  styleUrls: ['./user-management.component.css']
})
export class UserManagementComponent implements OnInit {
  icons = {
    userPlus: UserPlus,
    trash2: Trash2,
    ban: Ban,
    unlock: Unlock,
    loader2: Loader2
  };

  users: User[] = [];
  loading = false;
  error = '';
  
  // Create user form
  showCreateForm = false;
  newUser = {
    name: '',
    email: '',
    password: '',
    role: 'MARKETING'
  };
  creating = false;

  private apiUrl = 'http://localhost:8081';
  
  constructor(
    private http: HttpClient,
    private confirm: ConfirmDialogService
  ) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  private getHeaders() {
    const token = localStorage.getItem('token');
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  loadUsers() {
    this.loading = true;
    this.error = '';
    
    this.http.get<any[]>(`${this.apiUrl}/admin/users`, { headers: this.getHeaders() })
      .subscribe({
        next: (data) => {
          this.users = data;
          this.loading = false;
        },
        error: (err) => {
          this.error = 'Failed to load users';
          this.loading = false;
        }
      });
  }

  createUser() {
    if (!this.newUser.name || !this.newUser.email || !this.newUser.password) {
      return;
    }

    this.creating = true;
    this.http.post<User>(`${this.apiUrl}/admin/users`, this.newUser, { headers: this.getHeaders() })
      .subscribe({
        next: (user) => {
          this.users.push(user);
          this.showCreateForm = false;
          this.resetForm();
          this.creating = false;
        },
        error: (err) => {
          this.error = err.error?.message || 'Failed to create user';
          this.creating = false;
        }
      });
  }

  async deleteUser(id: number) {
    const ok = await this.confirm.confirm({ title: 'Delete User', message: 'Are you sure you want to delete this user?' });
    if (!ok) return;

    this.http.delete(`${this.apiUrl}/admin/users/${id}`, { headers: this.getHeaders() })
      .subscribe({
        next: () => {
          this.users = this.users.filter(u => u.id !== id);
        },
        error: (err) => {
          this.error = err.error?.message || 'Failed to delete user';
        }
      });
  }

  banUser(id: number) {
    this.http.put<User>(`${this.apiUrl}/admin/users/${id}/ban`, {}, { headers: this.getHeaders() })
      .subscribe({
        next: (user) => {
          const index = this.users.findIndex(u => u.id === id);
          if (index !== -1) {
            this.users[index] = user;
          }
        },
        error: (err) => {
          this.error = err.error?.message || 'Failed to ban user';
        }
      });
  }

  unbanUser(id: number) {
    this.http.put<User>(`${this.apiUrl}/admin/users/${id}/unban`, {}, { headers: this.getHeaders() })
      .subscribe({
        next: (user) => {
          const index = this.users.findIndex(u => u.id === id);
          if (index !== -1) {
            this.users[index] = user;
          }
        },
        error: (err) => {
          this.error = err.error?.message || 'Failed to unban user';
        }
      });
  }

  resetForm() {
    this.newUser = {
      name: '',
      email: '',
      password: '',
      role: 'MARKETING'
    };
  }
}