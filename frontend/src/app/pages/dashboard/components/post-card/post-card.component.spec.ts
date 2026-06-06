import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PostCardComponent } from './post-card.component';

describe('PostCardComponent', () => {
  let component: PostCardComponent;
  let fixture: ComponentFixture<PostCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PostCardComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(PostCardComponent);
    component = fixture.componentInstance;
    component.post = { title: '', content: '', platform: '', likes: 0, commentsCount: 0, publishedAt: '', image: null };
    fixture.detectChanges();
  });

  it('creates_component', () => {
    expect(component).toBeTruthy();
  });

  it('accepts_post_input', () => {
    const testPost = { id: 1, content: 'test post', platform: 'LINKEDIN', likes: 0, commentsCount: 0, publishedAt: new Date().toISOString(), image: null };
    component.post = testPost;
    fixture.detectChanges();

    expect(component.post).toEqual(testPost);
    expect(component.post.id).toBe(1);
  });
});
