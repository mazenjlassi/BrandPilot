import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class CommentService {

  private api = 'http://localhost:8081/comments';

  constructor(private http: HttpClient) {}

  // ================= ALL COMMENTS FOR A POST =================
  getByPost(postId: number) {
    return this.http.get<any[]>(`${this.api}/post/${postId}`);
  }

  // ================= FILTER BY SENTIMENT (PER POST) =================
  getByPostAndSentiment(postId: number, sentiment: string) {
    return this.http.get<any[]>(
      `${this.api}/post/${postId}/sentiment/${sentiment}`
    );
  }

  // ================= (OPTIONAL) CAMPAIGN =================
  getByCampaign(campaignId: number) {
    return this.http.get<any[]>(`${this.api}/campaign/${campaignId}`);
  }
}