import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AdminService {

  private api = environment.apiUrl + '/admin';

  constructor(private http: HttpClient) {}

  getUserStats() {
    return this.http.get<any>(`${this.api}/stats`);
  }

  getCampaignsProgress(limit: number = 3) {
    return this.http.get<any[]>(`${this.api}/campaigns/progress?limit=${limit}`);
  }
}