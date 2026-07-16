import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class NotificationService {

  private api = 'http://localhost:8081/notifications';

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
