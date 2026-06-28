const { filterAndLimitPosts } = require('./facebookScraper');

describe('filterAndLimitPosts', () => {
  const makePost = (overrides = {}) => ({
    postText: overrides.postText ?? 'Valid post content here for testing purposes',
    url: overrides.url ?? 'https://www.facebook.com/123/posts/456',
    postedAt: overrides.postedAt ?? '',
    ...overrides
  });

  test('filters posts without url', () => {
    const posts = [makePost({ url: '' }), makePost({ url: 'short' }), makePost()];
    expect(filterAndLimitPosts(posts, 10)).toHaveLength(1);
  });

  test('filters posts with comment_id in url', () => {
    const posts = [makePost({ url: 'https://www.facebook.com/123/posts/456?comment_id=789' }), makePost()];
    expect(filterAndLimitPosts(posts, 10)).toHaveLength(1);
  });

  test('filters posts with garbage text keywords', () => {
    const posts = [
      makePost({ postText: 'nasa - something irrelevant' }),
      makePost({ postText: 'suivi de compte activity' }),
      makePost({ postText: 'Page · Brand Name' }),
      makePost()
    ];
    expect(filterAndLimitPosts(posts, 10)).toHaveLength(1);
  });

  test('filters duplicate posts by first 30 chars of text', () => {
    const posts = [
      makePost({ postText: 'This is a unique post about marketing strategies' }),
      makePost({ postText: 'This is a unique post about marketing news today' }),
    ];
    expect(filterAndLimitPosts(posts, 10)).toHaveLength(1);
  });

  test('limits posts to given limit', () => {
    const posts = Array.from({ length: 25 }, (_, i) => makePost({
      postText: `Unique post number ${i} with enough content to pass filter`,
      url: `https://www.facebook.com/posts/${i}`
    }));
    expect(filterAndLimitPosts(posts, 5)).toHaveLength(5);
  });

  test('returns all valid posts when under limit', () => {
    const posts = [
      makePost({ postText: 'First unique post content here for testing' }),
      makePost({ postText: 'Second different post content for testing purposes' }),
    ];
    expect(filterAndLimitPosts(posts, 10)).toHaveLength(2);
  });

  test('returns empty array for empty input', () => {
    expect(filterAndLimitPosts([], 10)).toEqual([]);
  });

  test('handles missing postText gracefully', () => {
    const posts = [makePost({ postText: undefined })];
    expect(filterAndLimitPosts(posts, 10)).toHaveLength(1);
  });

  test('case-insensitive garbage text detection', () => {
    const posts = [
      makePost({ postText: 'nasa - breaking news alert' }),
      makePost({ postText: 'Followers count growing daily' }),
      makePost()
    ];
    expect(filterAndLimitPosts(posts, 10)).toHaveLength(1);
  });
});
