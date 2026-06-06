import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CompanyProfile {
  id?: number;
  companyName: string;
  instagramUrl: string;
  facebookUrl: string;
  linkedinUrl: string;
}

@Injectable({ providedIn: 'root' })
export class PatternService {

  private api = 'http://localhost:8081/api/scraped-posts';
  private scraperApi = 'http://localhost:8081/api/scraper';
  private companyApi = 'http://localhost:8081/api/company-profiles';
  private patternApi = 'http://localhost:8081/api/patterns';

  constructor(private http: HttpClient) {}

  // Scraped Posts CRUD
  getScrapedPosts(companyName?: string): Observable<any[]> {
    let url = this.api;
    if (companyName) url += `?companyName=${encodeURIComponent(companyName)}`;
    return this.http.get<any[]>(url);
  }

  createScrapedPost(data: any): Observable<any> {
    return this.http.post<any>(this.api, data);
  }

  deleteScrapedPost(id: number): Observable<any> {
    return this.http.delete(`${this.api}/${id}`);
  }

  getUnanalyzedCount(): Observable<any> {
    return this.http.get(`${this.api}/count?usedForPattern=false`);
  }

  getScrapedCount(companyName: string): Observable<number> {
    return this.http.get<number>(`${this.api}/count?companyName=${encodeURIComponent(companyName)}`);
  }

  getCompanies(): Observable<string[]> {
    return this.http.get<string[]>(`${this.api}/companies`);
  }

  // Company Profiles
  getCompanyProfiles(): Observable<CompanyProfile[]> {
    return this.http.get<CompanyProfile[]>(this.companyApi);
  }

  getCompanyProfileByName(companyName: string): Observable<CompanyProfile> {
    return this.http.get<CompanyProfile>(`${this.companyApi}/by-name/${encodeURIComponent(companyName)}`);
  }

  createCompanyProfile(data: CompanyProfile): Observable<CompanyProfile> {
    return this.http.post<CompanyProfile>(this.companyApi, data);
  }

  updateCompanyProfile(id: number, data: CompanyProfile): Observable<CompanyProfile> {
    return this.http.put<CompanyProfile>(`${this.companyApi}/${id}`, data);
  }

  deleteCompanyProfile(id: number): Observable<void> {
    return this.http.delete<void>(`${this.companyApi}/${id}`);
  }

  // Scraper Trigger
  triggerScrape(companyName: string): Observable<any> {
    const params = new HttpParams().set('companyName', companyName);
    return this.http.post(`${this.scraperApi}/trigger`, null, { params });
  }

  // Patterns
  getAllPatterns(companyName?: string): Observable<any[]> {
    let url = this.patternApi;
    if (companyName) url += `?companyName=${encodeURIComponent(companyName)}`;
    return this.http.get<any[]>(url);
  }

  deletePattern(id: number): Observable<any> {
    return this.http.delete(`${this.patternApi}/crud/${id}`);
  }

  runBatchAnalysis(companyName: string): Observable<any> {
    return this.http.post(`${this.patternApi}/analyze-batch?companyName=${encodeURIComponent(companyName)}`, {});
  }
}