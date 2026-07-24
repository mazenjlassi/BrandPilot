const { scrapeAllPlatforms } = require('./src/scrapers/index');
const axios = require('axios');

const BACKEND_URL = process.env.BACKEND_URL;
const SCRAPER_TOKEN = process.env.SCRAPER_TOKEN;
const COMPANY_NAME = process.env.COMPANY_NAME || '';

if (!BACKEND_URL || !SCRAPER_TOKEN) {
  console.error('Missing BACKEND_URL or SCRAPER_TOKEN');
  process.exit(1);
}

const headers = { Authorization: `Bearer ${SCRAPER_TOKEN}` };

async function getCompanies() {
  if (COMPANY_NAME) {
    const { data } = await axios.get(`${BACKEND_URL}/api/scraper/companies`, { headers });
    return data.filter(c => c.companyName === COMPANY_NAME);
  }
  const { data } = await axios.get(`${BACKEND_URL}/api/scraper/companies`, { headers });
  return data;
}

async function main() {
  const companies = await getCompanies();
  if (companies.length === 0) {
    console.log('No companies to scrape');
    return;
  }

  console.log(`Found ${companies.length} companies to scrape`);

  for (const company of companies) {
    console.log(`\n=== Scraping ${company.companyName} ===`);

    const accounts = {
      linkedin: company.linkedinUrl || '',
      instagram: company.instagramUrl || '',
      facebook: company.facebookUrl || ''
    };

    const result = await scrapeAllPlatforms(accounts, company.companyName);

    if (!result || !result.results) {
      console.log(`  No results for ${company.companyName}`);
      continue;
    }

    console.log(`  Sending ${result.results.length} platform results to backend...`);

    await axios.post(`${BACKEND_URL}/api/scraper/ingest`, {
      companyName: company.companyName,
      results: result.results
    }, { headers });

    const total = result.results.reduce((s, r) => s + (r.posts ? r.posts.length : 0), 0);
    console.log(`  ✓ Saved ${total} posts for ${company.companyName}`);
  }

  console.log('\n=== Scrape complete ===');
}

main().catch(err => {
  console.error('Scrape failed:', err.message);
  process.exit(1);
});
