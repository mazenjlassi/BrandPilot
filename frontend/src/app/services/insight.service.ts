import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class InsightService {

  private api = environment.apiUrl + '/insights';

  constructor(private http: HttpClient) {}

  // 🔥 Campaign insights
  getByCampaign(campaignId: number) {
    return this.http.get<any>(`${this.api}/campaign/${campaignId}`);
  }
}