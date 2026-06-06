import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CampaignListComponent } from './campaign-list.component';
import { provideRouter } from '@angular/router';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { CampaignService } from '../../services/campaign.service';

describe('CampaignListComponent', () => {
  let component: CampaignListComponent;
  let fixture: ComponentFixture<CampaignListComponent>;
  let httpMock: HttpTestingController;

  const mockCampaigns = [
    { id: 1, name: 'AI Campaign', topic: 'Artificial Intelligence', status: 'active', createdAt: '2026-06-10T10:00:00Z' },
    { id: 2, name: 'Social Push', topic: 'Marketing', status: 'active', createdAt: '2026-06-05T10:00:00Z' },
    { id: 3, name: 'Brand Awareness', topic: 'Branding', status: 'draft', createdAt: '2026-06-15T10:00:00Z' }
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CampaignListComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        CampaignService
      ]
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(CampaignListComponent);
    component = fixture.componentInstance;
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load campaigns on init', () => {
    fixture.detectChanges();

    const req = httpMock.expectOne('http://localhost:8081/campaigns');
    expect(req.request.method).toBe('GET');
    req.flush(mockCampaigns);

    expect(component.campaigns.length).toBe(3);
    expect(component.loading).toBeFalse();
  });

  it('should handle load error', () => {
    fixture.detectChanges();

    const req = httpMock.expectOne('http://localhost:8081/campaigns');
    req.flush('Error', { status: 500, statusText: 'Server Error' });

    expect(component.error).toBe('Failed to load campaigns');
    expect(component.loading).toBeFalse();
  });

  describe('filteredCampaigns', () => {
    beforeEach(() => {
      component.campaigns = [...mockCampaigns];
    });

    it('should return all campaigns by default', () => {
      const result = component.filteredCampaigns;
      expect(result.length).toBe(3);
    });

    it('should filter by search query', () => {
      component.searchQuery = 'AI';
      const result = component.filteredCampaigns;
      expect(result.length).toBe(1);
      expect(result[0].name).toBe('AI Campaign');
    });

    it('should filter by topic search', () => {
      component.searchQuery = 'Branding';
      const result = component.filteredCampaigns;
      expect(result.length).toBe(1);
      expect(result[0].name).toBe('Brand Awareness');
    });

    it('should filter by status', () => {
      component.statusFilter = 'draft';
      const result = component.filteredCampaigns;
      expect(result.length).toBe(1);
      expect(result[0].name).toBe('Brand Awareness');
    });

    it('should combine search and status filters', () => {
      component.searchQuery = 'a';
      component.statusFilter = 'active';
      const result = component.filteredCampaigns;
      expect(result.length).toBe(2);
    });

    it('should sort newest first by default', () => {
      const result = component.filteredCampaigns;
      expect(result[0].name).toBe('Brand Awareness');
      expect(result[2].name).toBe('Social Push');
    });

    it('should sort oldest first', () => {
      component.sortBy = 'oldest';
      const result = component.filteredCampaigns;
      expect(result[0].name).toBe('Social Push');
      expect(result[2].name).toBe('Brand Awareness');
    });

    it('should sort by name', () => {
      component.sortBy = 'name';
      const result = component.filteredCampaigns;
      expect(result[0].name).toBe('AI Campaign');
      expect(result[1].name).toBe('Brand Awareness');
      expect(result[2].name).toBe('Social Push');
    });

    it('should handle empty search results', () => {
      component.searchQuery = 'zzz';
      const result = component.filteredCampaigns;
      expect(result.length).toBe(0);
    });

    it('should handle missing optional fields gracefully', () => {
      component.campaigns = [
        { id: 1, name: '', topic: null, status: null, createdAt: null }
      ];
      const result = component.filteredCampaigns;
      expect(result.length).toBe(1);
    });
  });

  it('openCampaign should navigate', () => {
    const routerSpy = spyOn(component['router'], 'navigate');
    const event = new MouseEvent('click');
    component.openCampaign(event, 1);
    expect(routerSpy).toHaveBeenCalledWith(['/campaigns', 1]);
  });
});
