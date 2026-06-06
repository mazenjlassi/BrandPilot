import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class ChatService {

  private api = 'http://localhost:8081/chat';

  constructor(private http: HttpClient) {}

  // ================= CONVERSATIONS =================

  getConversations() {
    return this.http.get<any[]>(`${this.api}/conversations`);
  }

  getConversation(id: number) {
    return this.http.get<any>(`${this.api}/conversations/${id}`);
  }

  createConversation(title: string) {
    return this.http.post<any>(`${this.api}/conversations`, {
      title: title
    });
  }

  deleteConversation(id: number) {
    return this.http.delete(`${this.api}/conversations/${id}`);
  }

  // ================= MESSAGES =================

  getMessages(conversationId: number) {
    return this.http.get<any[]>(
      `${this.api}/conversations/${conversationId}/messages`
    );
  }

  sendMessage(conversationId: number, content: string) {
    return this.http.post<any>(
      `${this.api}/conversations/${conversationId}/messages`,
      { content: content }
    );
  }

  // ================= CONCLUSION =================

  generateConclusion(conversationId: number) {
    return this.http.post(
      `${this.api}/conversations/${conversationId}/conclusion`,
      {},
      { responseType: 'text' }
    );
  }
}