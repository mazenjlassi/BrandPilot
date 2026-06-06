import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { ChatService } from './chat.service';

describe('ChatService', () => {
  let service: ChatService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        ChatService
      ]
    });
    service = TestBed.inject(ChatService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('getConversations_callsGet', () => {
    service.getConversations().subscribe();
    const req = httpMock.expectOne('http://localhost:8081/chat/conversations');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('createConversation_callsPost', () => {
    service.createConversation('New Chat').subscribe();
    const req = httpMock.expectOne('http://localhost:8081/chat/conversations');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ title: 'New Chat' });
    req.flush({});
  });

  it('getMessages_callsWithConversationId', () => {
    service.getMessages(1).subscribe();
    const req = httpMock.expectOne('http://localhost:8081/chat/conversations/1/messages');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('sendMessage_callsPostWithContent', () => {
    service.sendMessage(1, 'Hello').subscribe();
    const req = httpMock.expectOne('http://localhost:8081/chat/conversations/1/messages');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ content: 'Hello' });
    req.flush({});
  });

  it('generateConclusion_callsPost', () => {
    service.generateConclusion(1).subscribe();
    const req = httpMock.expectOne('http://localhost:8081/chat/conversations/1/conclusion');
    expect(req.request.method).toBe('POST');
    req.flush('');
  });
});
