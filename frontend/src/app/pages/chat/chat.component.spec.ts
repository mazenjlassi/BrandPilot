import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ChatComponent } from './chat.component';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { ChatService } from '../../services/chat.service';
import { ConfirmDialogService } from '../../shared/confirm-dialog/confirm-dialog.service';
import { environment } from '../../../environments/environment';

describe('ChatComponent', () => {
  let component: ChatComponent;
  let fixture: ComponentFixture<ChatComponent>;
  let httpMock: HttpTestingController;
  let confirmSpy: jasmine.SpyObj<ConfirmDialogService>;

  const mockConversations = [
    { id: 1, title: 'Chat 1', createdAt: '2026-06-01T10:00:00Z' },
    { id: 2, title: 'Chat 2', createdAt: '2026-06-02T10:00:00Z' }
  ];

  const mockMessages = [
    { id: 1, role: 'USER', content: 'Hello', createdAt: '2026-06-01T10:00:00Z' },
    { id: 2, role: 'ASSISTANT', content: 'Hi there!', createdAt: '2026-06-01T10:00:05Z' }
  ];

  beforeEach(async () => {
    confirmSpy = jasmine.createSpyObj('ConfirmDialogService', ['confirm']);

    await TestBed.configureTestingModule({
      imports: [ChatComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        ChatService,
        { provide: ConfirmDialogService, useValue: confirmSpy }
      ]
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(ChatComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load conversations on init', () => {
    fixture.detectChanges();

    const req = httpMock.expectOne(`${environment.apiUrl}/chat/conversations`);
    expect(req.request.method).toBe('GET');
    req.flush(mockConversations);

    expect(component.conversations.length).toBe(2);
  });

  it('selectConversation should set id and load messages', () => {
    component.selectConversation(1);
    expect(component.selectedConversationId).toBe(1);

    const req = httpMock.expectOne(`${environment.apiUrl}/chat/conversations/1/messages`);
    expect(req.request.method).toBe('GET');
    req.flush(mockMessages);

    expect(component.messages.length).toBe(2);
  });

  it('createConversation should create and select new conversation', () => {
    component.createConversation();

    const req = httpMock.expectOne(`${environment.apiUrl}/chat/conversations`);
    expect(req.request.method).toBe('POST');
    req.flush({ id: 3, title: 'New Chat' });

    const msgReq = httpMock.expectOne(`${environment.apiUrl}/chat/conversations/3/messages`);
    msgReq.flush([]);

    expect(component.conversations.length).toBe(1);
    expect(component.selectedConversationId).toBe(3);
  });

  it('sendMessage should add temp message and send', () => {
    component.selectedConversationId = 1;
    component.newMessage = 'Test message';

    component.sendMessage();

    expect(component.messages.length).toBe(1);
    expect(component.messages[0].role).toBe('USER');
    expect(component.messages[0].content).toBe('Test message');
    expect(component.newMessage).toBe('');
    expect(component.loading).toBeTrue();

    const req = httpMock.expectOne(`${environment.apiUrl}/chat/conversations/1/messages`);
    expect(req.request.method).toBe('POST');
    req.flush({ id: 3, role: 'ASSISTANT', content: 'Response' });

    const convReq = httpMock.expectOne(`${environment.apiUrl}/chat/conversations`);
    convReq.flush([]);

    expect(component.messages.length).toBe(2);
    expect(component.loading).toBeFalse();
  });

  it('sendMessage should not send if message is empty', () => {
    component.newMessage = '';
    component.sendMessage();
    httpMock.expectNone(`${environment.apiUrl}/chat/conversations/1/messages`);
  });

  it('sendMessage should handle error', () => {
    component.selectedConversationId = 1;
    component.newMessage = 'Test';
    component.sendMessage();

    const req = httpMock.expectOne(`${environment.apiUrl}/chat/conversations/1/messages`);
    req.flush('Error', { status: 500, statusText: 'Server Error' });

    expect(component.loading).toBeFalse();
  });

  it('deleteConversation should confirm then delete', async () => {
    confirmSpy.confirm.and.resolveTo(true);
    component.conversations = [...mockConversations];
    component.selectedConversationId = 1;

    await component.deleteConversation(1);

    const req = httpMock.expectOne(`${environment.apiUrl}/chat/conversations/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);

    expect(component.conversations.length).toBe(1);
    expect(component.conversations[0].id).toBe(2);
  });

  it('deleteConversation should clear selected if deleted', async () => {
    confirmSpy.confirm.and.resolveTo(true);
    component.conversations = [...mockConversations];
    component.selectedConversationId = 1;
    component.messages = mockMessages;
    component.conclusion = 'some conclusion';

    await component.deleteConversation(1);

    httpMock.expectOne(`${environment.apiUrl}/chat/conversations/1`).flush(null);

    expect(component.selectedConversationId).toBeNull();
    expect(component.messages).toEqual([]);
    expect(component.conclusion).toBe('');
  });

  it('deleteConversation should skip if not confirmed', async () => {
    confirmSpy.confirm.and.resolveTo(false);
    await component.deleteConversation(1);
    httpMock.expectNone(`${environment.apiUrl}/chat/conversations/1`);
  });

  it('deleteConversation should handle error', async () => {
    confirmSpy.confirm.and.resolveTo(true);
    component.conversations = [...mockConversations];

    await component.deleteConversation(1);

    const req = httpMock.expectOne(`${environment.apiUrl}/chat/conversations/1`);
    req.flush('Error', { status: 500, statusText: 'Server Error' });

    expect(component.conversations.length).toBe(2);
  });

  it('getSelectedConversation should return matching conversation', () => {
    component.conversations = mockConversations;
    component.selectedConversationId = 1;
    expect(component.getSelectedConversation()?.id).toBe(1);
  });

  it('getSelectedConversation should return undefined if no match', () => {
    component.conversations = mockConversations;
    component.selectedConversationId = 99;
    expect(component.getSelectedConversation()).toBeUndefined();
  });

  it('generateConclusion should send POST request', () => {
    component.selectedConversationId = 1;
    component.generateConclusion();

    expect(component.generatingConclusion).toBeTrue();

    const req = httpMock.expectOne(`${environment.apiUrl}/chat/conversations/1/conclusion`);
    expect(req.request.method).toBe('POST');
    req.flush('Great conversation!', { headers: { 'Content-Type': 'text/plain' } });

    expect(component.conclusion).toBe('Great conversation!');
    expect(component.generatingConclusion).toBeFalse();
  });

  it('generateConclusion should handle error', () => {
    component.selectedConversationId = 1;
    component.generateConclusion();

    const req = httpMock.expectOne(`${environment.apiUrl}/chat/conversations/1/conclusion`);
    req.flush('Error', { status: 500, statusText: 'Server Error' });

    expect(component.generatingConclusion).toBeFalse();
  });
});
