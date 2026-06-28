jest.mock('./scrapers/index', () => ({
  scrapeAllPlatforms: jest.fn(),
}));

const request = require('supertest');
const { scrapeAllPlatforms } = require('./scrapers/index');

let app;

beforeAll(() => {
  process.env.NODE_ENV = 'test';
});

beforeEach(() => {
  jest.clearAllMocks();
  jest.isolateModules(() => {
    app = require('./server');
  });
});

describe('GET /health', () => {
  it('returns status ok', async () => {
    const res = await request(app).get('/health');
    expect(res.status).toBe(200);
    expect(res.body.status).toBe('ok');
    expect(res.body.timestamp).toBeDefined();
  });
});

describe('POST /scrape', () => {
  it('returns 400 when companyName is missing', async () => {
    const res = await request(app)
      .post('/scrape')
      .send({ accounts: { instagram: 'https://ig.com/test' } });
    expect(res.status).toBe(400);
    expect(res.body.error).toContain('companyName');
  });

  it('returns 400 when accounts is missing', async () => {
    const res = await request(app)
      .post('/scrape')
      .send({ companyName: 'Test' });
    expect(res.status).toBe(400);
    expect(res.body.error).toContain('accounts');
  });

  it('returns 400 when body is empty', async () => {
    const res = await request(app).post('/scrape').send({});
    expect(res.status).toBe(400);
  });

  it('calls scrapeAllPlatforms and returns result', async () => {
    const mockResult = { companyName: 'Test', results: [], scrapedAt: new Date().toISOString() };
    scrapeAllPlatforms.mockResolvedValue(mockResult);

    const res = await request(app)
      .post('/scrape')
      .send({
        companyName: 'Test',
        accounts: { instagram: 'https://ig.com/test' },
      });

    expect(res.status).toBe(200);
    expect(res.body.companyName).toBe('Test');
    expect(scrapeAllPlatforms).toHaveBeenCalledWith(
      { instagram: 'https://ig.com/test' },
      'Test'
    );
  });

  it('returns 500 when scrapeAllPlatforms throws', async () => {
    scrapeAllPlatforms.mockRejectedValue(new Error('Scraping crashed'));

    const res = await request(app)
      .post('/scrape')
      .send({
        companyName: 'Test',
        accounts: { instagram: 'https://ig.com/test' },
      });

    expect(res.status).toBe(500);
    expect(res.body.error).toBe('Scraping failed');
  });
});
