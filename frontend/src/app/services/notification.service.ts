import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class NotificationService {

  private api = environment.apiUrl + '/notifications';

  constructor(private http: HttpClient) {}

  getAll() {
    return this.http.get<any[]>(this.api);
  }

  getUnread() {
    return this.http.get<any[]>(`${this.api}/unread`);
  }

  getUnreadCount() {
    return this.http.get<any>(`${this.api}/unread-count`);
  }

  markAsRead(id: number) {
    return this.http.post(`${this.api}/${id}/read`, {});
  }

  markAllAsRead() {
    return this.http.post(`${this.api}/read-all`, {});
  }
}
