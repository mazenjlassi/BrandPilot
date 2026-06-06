import { Component, OnInit } from '@angular/core';
import { RouterModule, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { LucideAngularModule, LayoutDashboard, PenTool, MessageCircle, FileText, Folder, Menu, X, Sun, Moon, Users, LogOut, Calendar, Search, PanelLeftClose, PanelLeftOpen } from 'lucide-angular';
import { AuthService } from '../../services/auth.service';
import { SidebarStateService } from '../sidebar-state.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterModule, CommonModule, LucideAngularModule],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent implements OnInit {
  icons = {
    layoutDashboard: LayoutDashboard,
    penTool: PenTool,
    messageCircle: MessageCircle,
    fileText: FileText,
    folder: Folder,
    menu: Menu,
    x: X,
    sun: Sun,
    moon: Moon,
    users: Users,
    logOut: LogOut,
    calendar: Calendar,
    search: Search,
    panelLeftClose: PanelLeftClose,
    panelLeftOpen: PanelLeftOpen
  };
  mobileMenuOpen = false;
  isDarkMode = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    public sidebarState: SidebarStateService
  ) {}

  ngOnInit(): void {
    this.sidebarState.init();
    const saved = localStorage.getItem('theme');
    this.isDarkMode = saved === 'dark';
    this.applyTheme();
  }

  toggleSidebar(): void {
    this.sidebarState.toggle();
  }

  get sidebarCollapsed(): boolean {
    return this.sidebarState.collapsed;
  }

  toggleMenu(): void {
    this.mobileMenuOpen = !this.mobileMenuOpen;
  }

  closeMenu(): void {
    this.mobileMenuOpen = false;
  }

  toggleTheme(): void {
    this.isDarkMode = !this.isDarkMode;
    localStorage.setItem('theme', this.isDarkMode ? 'dark' : 'light');
    this.applyTheme();
  }

  private applyTheme(): void {
    document.body.classList.toggle('dark-mode', this.isDarkMode);
  }

  isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  isMarketing(): boolean {
    return this.authService.isMarketing();
  }

  getUserName(): string {
    return this.authService.getName() || '';
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}