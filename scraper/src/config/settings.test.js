describe('settings', () => {
  const OLD_ENV = process.env;

  beforeEach(() => {
    jest.resetModules();
    process.env = { ...OLD_ENV };
  });

  afterAll(() => {
    process.env = OLD_ENV;
  });

  test('uses default values when env vars are not set', () => {
    delete process.env.TARGET_URL;
    delete process.env.HEADLESS;
    delete process.env.SLOWMO;
    delete process.env.PORT;
    delete process.env.BRIGHTDATA_PROXY;

    const settings = require('./settings');
    expect(settings.targetUrl).toBe('https://www.instagram.com/ibm');
    expect(settings.postLimit).toBe(5);
    expect(settings.headless).toBe(false);
    expect(settings.slowMo).toBe(100);
    expect(settings.serverPort).toBe(3000);
    expect(settings.brightDataProxy).toBeNull();
  });

  test('reads TARGET_URL from env', () => {
    process.env.TARGET_URL = 'https://www.instagram.com/test';
    const settings = require('./settings');
    expect(settings.targetUrl).toBe('https://www.instagram.com/test');
  });

  test('reads HEADLESS as boolean', () => {
    process.env.HEADLESS = 'true';
    const settings = require('./settings');
    expect(settings.headless).toBe(true);
  });

  test('reads PORT from env', () => {
    process.env.PORT = '4000';
    const settings = require('./settings');
    expect(settings.serverPort).toBe(4000);
  });

  test('reads BRIGHTDATA_PROXY from env', () => {
    process.env.BRIGHTDATA_PROXY = 'http://proxy:1234';
    const settings = require('./settings');
    expect(settings.brightDataProxy).toBe('http://proxy:1234');
  });

  test('has viewport with default dimensions', () => {
    const settings = require('./settings');
    expect(settings.viewport).toEqual({ width: 1280, height: 720 });
  });

  test('has userAgent string', () => {
    const settings = require('./settings');
    expect(settings.userAgent).toContain('Mozilla');
  });
});
