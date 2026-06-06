import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LucideAngularModule, Search, MoreHorizontal, Paperclip, Smile, Send, CheckCircle, FileText, Plus, MessageSquare, Trash2 } from 'lucide-angular';
import { ChatService } from '../../services/chat.service';
import { ConfirmDialogService } from '../../shared/confirm-dialog/confirm-dialog.service';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']
})
export class ChatComponent implements OnInit {

  conversations: any[] = [];
  selectedConversationId!: number;

  messages: any[] = [];
  newMessage = '';

  conclusion = '';
  generatingConclusion = false;
  loading = false;

  icons = {
    search: Search,
    moreHorizontal: MoreHorizontal,
    paperclip: Paperclip,
    smile: Smile,
    send: Send,
    checkCircle: CheckCircle,
    fileText: FileText,
    plus: Plus,
    messageSquare: MessageSquare,
    trash2: Trash2
  };

  constructor(
    private chatService: ChatService,
    private confirm: ConfirmDialogService
  ) {}

  ngOnInit() {
    this.loadConversations();
  }

  // ================= LOAD =================

  loadConversations() {
    this.chatService.getConversations().subscribe(res => {
      this.conversations = res;
    });
  }

  selectConversation(id: number) {
    this.selectedConversationId = id;
    this.loadMessages();
  }

  loadMessages() {
    this.chatService.getMessages(this.selectedConversationId).subscribe(res => {
      this.messages = res;
    });
  }

  // ================= ACTIONS =================

  createConversation() {
    this.chatService.createConversation('New Chat').subscribe(res => {
      this.conversations.unshift(res);
      this.selectConversation(res.id);
    });
  }

  sendMessage() {
    if (!this.newMessage.trim()) return;

    const temp = {
      role: 'USER',
      content: this.newMessage
    };

    this.messages.push(temp);

    this.loading = true;

    this.chatService
      .sendMessage(this.selectedConversationId, this.newMessage)
      .subscribe({
        next: (res) => {
          this.messages.push(res);
          this.loading = false;

          this.loadConversations();
        },
        error: () => {
          this.loading = false;
        }
      });

    this.newMessage = '';
  }

  async deleteConversation(id: number) {
    const ok = await this.confirm.confirm({ title: 'Delete Conversation', message: 'Delete this conversation and all its messages?' });
    if (!ok) return;
    this.chatService.deleteConversation(id).subscribe({
      next: () => {
        this.conversations = this.conversations.filter(c => c.id !== id);
        if (this.selectedConversationId === id) {
          this.selectedConversationId = null!;
          this.messages = [];
          this.conclusion = '';
        }
      },
      error: () => {}
    });
  }

  getSelectedConversation() {
    return this.conversations.find(c => c.id === this.selectedConversationId);
  }

  generateConclusion() {
    if (this.generatingConclusion) return;
    
    this.generatingConclusion = true;
    this.conclusion = '';

    this.chatService
      .generateConclusion(this.selectedConversationId)
      .subscribe({
        next: (res) => {
          this.conclusion = res;
          this.generatingConclusion = false;
        },
        error: () => {
          this.generatingConclusion = false;
        }
      });
  }
}