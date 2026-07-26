import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class StrategyService {

  private api = environment.apiUrl + '/marketing-strategies';

  constructor(private http: HttpClient) {}

  getAll() {
    return this.http.get<any[]>(this.api);
  }

  getActive() {
    return this.http.get<any>(`${this.api}/active`);
  }

  getById(id: number) {
    return this.http.get<any>(`${this.api}/${id}`);
  }

  generate(data: { topic: string; durationWeeks?: number; autoGenerate?: boolean }) {
    return this.http.post<any>(`${this.api}/generate`, data);
  }

  generateAuto() {
    return this.http.post<any>(`${this.api}/generate-auto`, {});
  }

  update(id: number, data: any) {
    return this.http.put<any>(`${this.api}/${id}`, data);
  }

  approve(id: number) {
    return this.http.post<any>(`${this.api}/${id}/approve`, {});
  }

  deactivate(id: number) {
    return this.http.post<any>(`${this.api}/${id}/deactivate`, {});
  }

  getStrategyCampaigns(id: number) {
    return this.http.get<any[]>(`${this.api}/${id}/campaigns`);
  }

  generateWeek(id: number) {
    return this.http.post<any>(`${this.api}/${id}/generate-week`, {});
  }

  generateCurrentWeek() {
    return this.http.post<any>(`${this.api}/generate-current-week`, {});
  }

  setAutoGenerate(id: number, autoGenerate: boolean) {
    return this.http.put<any>(`${this.api}/${id}/auto-generate`, { autoGenerate });
  }

  delete(id: number) {
    return this.http.delete<any>(`${this.api}/${id}`);
  }

  deleteInactive() {
    return this.http.delete<any>(`${this.api}/inactive`);
  }
}
