import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CampaignService {

  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  // ================= CAMPAIGNS =================

  createCampaign(data: any) {
    return this.http.post(`${this.apiUrl}/campaigns`, data);
  }

  getAll() {
    return this.http.get<any[]>(`${this.apiUrl}/campaigns`);
  }

  getById(id: number) {
    return this.http.get<any>(`${this.apiUrl}/campaigns/${id}`);
  }

  getCampaignPosts(id: number) {
    return this.http.get<any[]>(`${this.apiUrl}/campaigns/${id}/posts`);
  }

  getCampaignInsights(id: number) {
    return this.http.get<any>(`${this.apiUrl}/insights/campaign/${id}`);
  }

  // ================= AI GENERATION =================

  generateCampaign(data: any) {
    return this.http.post(`${this.apiUrl}/campaigns/generate`, data);
  }

  // ================= RECENT CAMPAIGNS =================

  getRecent(limit: number = 5) {
    return this.http.get<any[]>(`${this.apiUrl}/campaigns/recent?limit=${limit}`);
  }

  // ================= GENERATE FOR EXISTING =================

  generateForExisting(campaignId: number) {
    return this.http.post<any[]>(`${this.apiUrl}/campaigns/${campaignId}/generate`, {});
  }

  // ================= DELETE =================

  deleteCampaign(id: number) {
    return this.http.delete(`${this.apiUrl}/campaigns/${id}`, { responseType: 'text' });
  }
}