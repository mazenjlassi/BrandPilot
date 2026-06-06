import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { CampaignsComponent } from './pages/campaigns/campaigns.component';
import { PostsComponent } from './pages/posts/posts.component';
import { PostDetailsComponent } from './pages/posts/post-details/post-details.component';
import { CampaignListComponent } from './pages/campaign-list/campaign-list.component';
import { CampaignDetailsComponent } from './pages/campaign-list/campaign-details/campaign-details.component';
import { ChatComponent } from './pages/chat/chat.component';
import { UserManagementComponent } from './pages/admin/user-management.component';
import { CalendarComponent } from './pages/calendar/calendar.component';
import { ScrapedPostsComponent } from './pages/scraped-posts/scraped-posts.component';
import { PatternsComponent } from './pages/patterns/patterns.component';
import { CompanyDetailsComponent } from './pages/company-details/company-details.component';

export const routes: Routes = [
    { path: 'login', component: LoginComponent },
    { path: 'dashboard', component: DashboardComponent },
    { path: 'campaigns', component: CampaignsComponent },
    { path: 'posts', component: PostsComponent },
    {path: 'posts/:id', component: PostDetailsComponent} ,
    {path:'campaign-list', component: CampaignListComponent},
    {path: 'campaigns/:id', component: CampaignDetailsComponent },
    {path: 'chat', component: ChatComponent },
    {path: 'admin/users', component: UserManagementComponent },
    {path: 'scraped-posts', component: ScrapedPostsComponent },
    {path: 'company/:companyName', component: CompanyDetailsComponent },
    {path: 'patterns', component: PatternsComponent },
    {path: 'calendar', component: CalendarComponent },
    {path: '', redirectTo: '/dashboard', pathMatch: 'full' },
];
