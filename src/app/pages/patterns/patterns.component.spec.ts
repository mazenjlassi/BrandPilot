import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PatternsComponent } from './patterns.component';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController } from '@angular/common/http/testing';
import { PatternService } from '../../services/pattern.service';
import { ConfirmDialogService } from '../../shared/confirm-dialog/confirm-dialog.service';

describe('PatternsComponent', () => {
  let component: PatternsComponent;
  let fixture: ComponentFixture<PatternsComponent>;
  let httpMock: HttpTestingController;
  let confirmService: ConfirmDialogService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PatternsComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        PatternService,
        ConfirmDialogService
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(PatternsComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    confirmService = TestBed.inject(ConfirmDialogService);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('creates_component', () => {
    expect(component).toBeTruthy();
  });

  it('loads_companies_on_init', () => {
    fixture.detectChanges();
    const req = httpMock.expectOne('http://localhost:8081/api/scraped-posts/companies');
    req.flush(['NVIDIA', 'Google']);

    const patternsReq = httpMock.expectOne('http://localhost:8081/api/patterns?companyName=NVIDIA');
    patternsReq.flush([]);
    const countReq = httpMock.expectOne('http://localhost:8081/api/scraped-posts/count?companyName=NVIDIA');
    countReq.flush({ count: 0 });

    expect(component.companies.length).toBe(2);
  });

  it('selects_first_company_after_load', () => {
    fixture.detectChanges();
    const req = httpMock.expectOne('http://localhost:8081/api/scraped-posts/companies');
    req.flush(['NVIDIA', 'Google']);

    const patternsReq = httpMock.expectOne('http://localhost:8081/api/patterns?companyName=NVIDIA');
    patternsReq.flush([]);

    const countReq = httpMock.expectOne('http://localhost:8081/api/scraped-posts/count?companyName=NVIDIA');
    countReq.flush({ count: 0 });

    expect(component.selectedCompany).toBe('NVIDIA');
  });

  it('selectCompany_loads_patterns_and_count', () => {
    component.companies = ['NVIDIA'];
    component.selectCompany('NVIDIA');

    const patternsReq = httpMock.expectOne('http://localhost:8081/api/patterns?companyName=NVIDIA');
    patternsReq.flush([{ id: 1, topic: 'AI' }]);

    const countReq = httpMock.expectOne('http://localhost:8081/api/scraped-posts/count?companyName=NVIDIA');
    countReq.flush({ count: 5 });

    expect(component.patterns.length).toBe(1);
    expect(component.patterns[0].topic).toBe('AI');
  });

  it('toggleExpand_expands_and_collapses', () => {
    component.expandedId = 1;
    component.toggleExpand(1);
    expect(component.expandedId).toBeNull();

    component.toggleExpand(2);
    expect(component.expandedId).toBe(2);
  });

  it('runBatchAnalysis_calls_service', () => {
    component.selectedCompany = 'NVIDIA';
    component.runBatchAnalysis();

    const req = httpMock.expectOne('http://localhost:8081/api/patterns/analyze-batch?companyName=NVIDIA');
    req.flush('Analysis complete');

    const patternsReq = httpMock.expectOne('http://localhost:8081/api/patterns?companyName=NVIDIA');
    patternsReq.flush([]);

    expect(component.batchResult).toBe('Analysis complete');
  });

  it('deletePattern_confirms_and_removes', (done) => {
    component.patterns = [{ id: 1, topic: 'AI' }, { id: 2, topic: 'ML' }];
    spyOn(confirmService, 'confirm').and.returnValue(Promise.resolve(true));

    component.deletePattern(1).then(() => {
      const req = httpMock.expectOne('http://localhost:8081/api/patterns/crud/1');
      expect(req.request.method).toBe('DELETE');
      req.flush({});

      expect(component.patterns.length).toBe(1);
      expect(component.patterns[0].id).toBe(2);
      done();
    });
  });

  it('parsePlatformBreakdown_handles_string', () => {
    const result = component.parsePlatformBreakdown('{"linkedin": 5, "instagram": 3}');
    expect(result.length).toBe(2);
    expect(result[0].key).toBe('linkedin');
  });

  it('parsePlatformBreakdown_handles_object', () => {
    const result = component.parsePlatformBreakdown({ linkedin: 5, instagram: 3 });
    expect(result.length).toBe(2);
  });

  it('parsePlatformBreakdown_returns_empty_for_null', () => {
    expect(component.parsePlatformBreakdown(null)).toEqual([]);
  });

  it('parsePlatformBreakdown_returns_empty_for_invalid', () => {
    expect(component.parsePlatformBreakdown('not-json')).toEqual([]);
  });

  it('parseUsedPostIds_handles_string', () => {
    const result = component.parseUsedPostIds('[1, 2, 3]');
    expect(result).toEqual([1, 2, 3]);
  });

  it('parseUsedPostIds_handles_array', () => {
    const result = component.parseUsedPostIds([1, 2, 3]);
    expect(result).toEqual([1, 2, 3]);
  });

  it('parseUsedPostIds_returns_empty_for_null', () => {
    expect(component.parseUsedPostIds(null)).toEqual([]);
  });

  it('getScoreColor_high', () => {
    expect(component.getScoreColor(0.7)).toBe('#16A34A');
    expect(component.getScoreColor(1.0)).toBe('#16A34A');
  });

  it('getScoreColor_medium', () => {
    expect(component.getScoreColor(0.4)).toBe('#F59E0B');
    expect(component.getScoreColor(0.69)).toBe('#F59E0B');
  });

  it('getScoreColor_low', () => {
    expect(component.getScoreColor(0.39)).toBe('#EF4444');
    expect(component.getScoreColor(0)).toBe('#EF4444');
  });

  it('getScoreLabel_high', () => {
    expect(component.getScoreLabel(0.7)).toBe('High');
  });

  it('getScoreLabel_medium', () => {
    expect(component.getScoreLabel(0.4)).toBe('Medium');
  });

  it('getScoreLabel_low', () => {
    expect(component.getScoreLabel(0)).toBe('Low');
  });
});
