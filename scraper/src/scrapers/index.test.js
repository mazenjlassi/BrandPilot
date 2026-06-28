jest.mock('../utils/browser', () => ({
  launchBrowser: jest.fn(),
  createPlatformContext: jest.fn(),
  applyStealthPatches: jest.fn(),
}));

jest.mock('./instagramScraper', () => ({
  scrapeInstagram: jest.fn(),
}));

jest.mock('./facebookScraper', () => ({
  scrapeFacebook: jest.fn(),
  filterAndLimitPosts: jest.fn(),
  PLATFORMS: 'facebook',
}));

jest.mock('./linkedinScraper', () => ({
  scrapeLinkedIn: jest.fn(),
}));

const { launchBrowser, createPlatformContext } = require('../utils/browser');
const { scrapeInstagram } = require('./instagramScraper');
const { scrapeFacebook } = require('./facebookScraper');
const { scrapeLinkedIn } = require('./linkedinScraper');
const { scrapeAllPlatforms } = require('./index');

describe('scrapeAllPlatforms', () => {
  const mockBrowser = { close: jest.fn().mockResolvedValue() };
  const mockContext = { newPage: jest.fn(), close: jest.fn().mockResolvedValue() };
  const mockPage = { close: jest.fn().mockResolvedValue() };

  beforeEach(() => {
    jest.clearAllMocks();
    launchBrowser.mockResolvedValue({ browser: mockBrowser });
    createPlatformContext.mockResolvedValue(mockContext);
    mockContext.newPage.mockResolvedValue(mockPage);
    scrapeInstagram.mockResolvedValue([{ postText: 'IG post' }]);
    scrapeFacebook.mockResolvedValue([{ postText: 'FB post' }]);
    scrapeLinkedIn.mockResolvedValue([{ postText: 'LI post' }]);
  });

  test('scrapes all platforms with valid accounts', async () => {
    const accounts = {
      instagram: 'https://instagram.com/test',
      facebook: 'https://facebook.com/test',
      linkedin: 'https://linkedin.com/company/test',
    };

    const result = await scrapeAllPlatforms(accounts, 'TestCompany');

    expect(launchBrowser).toHaveBeenCalled();
    expect(createPlatformContext).toHaveBeenCalledTimes(3);
    expect(result.companyName).toBe('TestCompany');
    expect(result.results).toHaveLength(3);
    expect(result.results[0].platform).toBe('instagram');
    expect(result.results[0].posts).toHaveLength(1);
  });

  test('skips platforms with no URL', async () => {
    const accounts = {
      instagram: null,
      facebook: undefined,
      linkedin: 'https://linkedin.com/company/test',
    };

    const result = await scrapeAllPlatforms(accounts, 'TestCompany');

    expect(scrapeInstagram).not.toHaveBeenCalled();
    expect(scrapeFacebook).not.toHaveBeenCalled();
    expect(scrapeLinkedIn).toHaveBeenCalled();
    expect(result.results).toHaveLength(1);
    expect(result.results[0].platform).toBe('linkedin');
  });

  test('closes browser after scrape', async () => {
    const accounts = { instagram: 'https://instagram.com/test', facebook: null, linkedin: null };

    await scrapeAllPlatforms(accounts, 'TestCompany');

    expect(mockBrowser.close).toHaveBeenCalled();
  });

  test('still closes browser when scraper throws', async () => {
    scrapeInstagram.mockRejectedValue(new Error('Scraper failed'));

    const accounts = { instagram: 'https://instagram.com/test', facebook: null, linkedin: null };

    const result = await scrapeAllPlatforms(accounts, 'TestCompany');

    expect(mockBrowser.close).toHaveBeenCalled();
    expect(result.results).toHaveLength(0);
  });

  test('passes correct arguments to Facebook scraper', async () => {
    const accounts = { instagram: null, facebook: 'https://facebook.com/test', linkedin: null };

    await scrapeAllPlatforms(accounts, 'TestCompany');

    expect(scrapeFacebook).toHaveBeenCalledWith(mockPage, 'TestCompany', mockBrowser, mockContext, 'https://facebook.com/test');
  });

  test('passes correct arguments to Instagram scraper', async () => {
    const accounts = { instagram: 'https://instagram.com/test', facebook: null, linkedin: null };

    await scrapeAllPlatforms(accounts, 'TestCompany');

    expect(scrapeInstagram).toHaveBeenCalledWith(mockPage, 'TestCompany', mockContext, 'https://instagram.com/test');
  });

  test('passes correct arguments to LinkedIn scraper', async () => {
    const accounts = { instagram: null, facebook: null, linkedin: 'https://linkedin.com/company/test' };

    await scrapeAllPlatforms(accounts, 'TestCompany');

    expect(scrapeLinkedIn).toHaveBeenCalledWith(mockPage, 'TestCompany', 'https://linkedin.com/company/test');
  });
});
