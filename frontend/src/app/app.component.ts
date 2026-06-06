import { Component, OnInit } from '@angular/core';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HeaderComponent } from "./shared/header/header.component";
import { FooterComponent } from "./shared/footer/footer.component";
import { filter } from 'rxjs';
import { SidebarStateService } from './shared/sidebar-state.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, CommonModule, HeaderComponent, FooterComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  title = 'metatry-front';
  isLoginRoute = false;

  constructor(
    private router: Router,
    public sidebarState: SidebarStateService
  ) {}

  ngOnInit() {
    this.sidebarState.init();
    this.router.events.pipe(
      filter(e => e instanceof NavigationEnd)
    ).subscribe((e: NavigationEnd) => {
      this.isLoginRoute = e.url === '/login';
    });
    this.isLoginRoute = this.router.url === '/login';
  }

  get sidebarCollapsed(): boolean {
    return this.sidebarState.collapsed;
  }
}
