import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class PostService {

  private api = environment.apiUrl + '/posts';
  private campaignApi = environment.apiUrl + '/campaigns';

  constructor(private http: HttpClient) {}

  // ================= DASHBOARD =================

  getLatestPosts(limit: number = 15) {
    return this.http.get<any[]>(
      `${this.api}/latestPublished?limit=${limit}`
    );
  }

  getTopPosts(limit: number = 5) {
    return this.http.get<any[]>(
      `${this.api}/top?limit=${limit}`
    );
  }

  getPostStats() {
    return this.http.get<any>(`${this.api}/stats`);
  }

  // ================= FILTERS =================

  getDrafts() {
    return this.http.get<any[]>(`${this.api}/drafts`);
  }

  getScheduled() {
    return this.http.get<any[]>(`${this.api}/scheduled`);
  }

  getPublished() {
    return this.http.get<any[]>(`${this.api}/published`);
  }

  getPermanent() {
    return this.http.get<any[]>(`${this.api}/permanent`);
  }

  // ================= CAMPAIGN =================

  getByCampaign(campaignId: number) {
    return this.http.get<any[]>(
      `${this.campaignApi}/${campaignId}/posts`
    );
  }

  // ================= POST ACTIONS =================

  publishPost(postId: number) {
    return this.http.post(
      `${environment.apiUrl}/publish/${postId}`,
      {}
    );
  }

  getById(id: number) {
    return this.http.get<any>(`${this.api}/${id}`);
  }

  updatePost(id: number, data: any) {
    return this.http.put(`${this.api}/${id}`, data, {
      responseType: 'text'
    });
  }

  deletePost(id: number) {
    return this.http.delete(`${this.api}/${id}`, {
      responseType: 'text'
    });
  }

  generateImage(id: number, prompt?: string) {
    const body: any = {};
    if (prompt) body.prompt = prompt;
    return this.http.post(`${this.api}/${id}/generate-image`, body);
  }

  // ================= ✅ CREATE POST WITH IMAGE =================

  createPostWithImage(campaignId: number, data: any, image?: File, video?: File) {

    const formData = new FormData();

    // JSON part
    formData.append(
      'data',
      new Blob([JSON.stringify(data)], { type: 'application/json' })
    );

    // optional image or video (mutually exclusive)
    if (video) {
      formData.append('video', video);
    } else if (image) {
      formData.append('image', image);
    }

    return this.http.post(
      `${this.campaignApi}/${campaignId}/posts/with-image`,
      formData
    );
  }

  // ================= CALENDAR =================

  getCalendarEvents(start: string, end: string) {
    return this.http.get<any[]>(
      `${this.api}/calendar?start=${start}&end=${end}`
    );
  }

  // ================= TIMING ANALYSIS =================

  getTimingAnalysis() {
    return this.http.get<any>(`${this.api}/timing-analysis`);
  }

  // ================= WEEKLY COMPARISON =================

  getWeeklyComparison() {
    return this.http.get<any>(`${this.api}/weekly-comparison`);
  }

  getUpcomingScheduled(limit: number = 3) {
    return this.http.get<any[]>(`${this.api}/upcoming-scheduled?limit=${limit}`);
  }

  uploadFile(file: File) {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<{ url: string }>(`${this.api}/upload`, formData);
  }

  approvePost(id: number) {
    return this.http.post<any>(`${this.api}/${id}/approve`, {});
  }

  approveAllPosts() {
    return this.http.post<any>(`${this.api}/approve-all`, {});
  }

  regeneratePost(id: number) {
    return this.http.post<any>(`${this.api}/${id}/regenerate`, {});
  }
}