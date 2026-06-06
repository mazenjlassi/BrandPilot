import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class InsightService {

  private api = 'http://localhost:8081/insights';

  constructor(private http: HttpClient) {}

  // 🔥 Campaign insights
  getByCampaign(campaignId: number) {
    return this.http.get<any>(`${this.api}/campaign/${campaignId}`);
  }
}