package com.example.metatry.Services.strategy;

import com.example.metatry.DTOs.GenerateStrategyRequest;
import com.example.metatry.DTOs.MarketingStrategyDTO;
import com.example.metatry.DTOs.MarketingStrategyRequest;
import com.example.metatry.Enums.MarketingStrategyStatus;
import com.example.metatry.Models.MarketingStrategy;
import com.example.metatry.Repositories.CampaignRepository;
import com.example.metatry.Repositories.MarketingStrategyRepository;
import com.example.metatry.Services.GeminiService;
import com.example.metatry.Services.MemoryContextService;
import com.example.metatry.Services.prompts.StrategyPromptBuilder;
import com.example.metatry.Services.strategy.NotificationService;
import com.example.metatry.Exceptions.StrategyNotFoundException;
import com.example.metatry.Exceptions.StrategyConflictException;
import com.example.metatry.Exceptions.StrategyGenerationException;
import com.example.metatry.Exceptions.StrategyNotEditableException;
import com.example.metatry.Services.scheduler.WeeklyImageDecisionService;
import com.example.metatry.Services.scheduler.WeeklyPostPlanner;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarketingStrategyServiceTest {

    @Mock private MarketingStrategyRepository strategyRepository;
    @Mock private CampaignRepository campaignRepository;
    @Mock private GeminiService geminiService;
    @Mock private StrategyPromptBuilder strategyPromptBuilder;
    @Mock private MemoryContextService memoryContextService;
    @Mock private MarketingStrategyMapper marketingStrategyMapper;
    @Mock private WeeklyPostPlanner weeklyPostPlanner;
    @Mock private WeeklyImageDecisionService weeklyImageDecisionService;
    @Mock private NotificationService notificationService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MarketingStrategyService strategyService;

    @BeforeEach
    void setUp() {
        strategyService = new MarketingStrategyService(
                strategyRepository, campaignRepository, geminiService,
                strategyPromptBuilder, memoryContextService,
                marketingStrategyMapper, weeklyPostPlanner,
                weeklyImageDecisionService, notificationService,
                objectMapper
        );
    }

    @Test
    void generateStrategy_createsAndReturnsDTO() {
        GenerateStrategyRequest request = GenerateStrategyRequest.builder()
                .topic("AI Marketing").durationWeeks(8).autoGenerate(false).build();

        when(strategyRepository.existsByStatus(MarketingStrategyStatus.PENDING)).thenReturn(false);
        when(strategyPromptBuilder.build("AI Marketing", 8)).thenReturn("prompt");
        when(geminiService.generate("prompt")).thenReturn("""
                {"title":"AI Strategy","summary":"AI summary","description":"desc","durationWeeks":8,"campaigns":[]}
                """);

        MarketingStrategy saved = MarketingStrategy.builder()
                .id(1L).title("AI Strategy").status(MarketingStrategyStatus.PENDING).build();
        when(strategyRepository.save(any())).thenReturn(saved);

        MarketingStrategyDTO dto = MarketingStrategyDTO.builder()
                .id(1L).title("AI Strategy").status("PENDING").build();
        when(marketingStrategyMapper.toDTO(saved)).thenReturn(dto);

        MarketingStrategyDTO result = strategyService.generateStrategy(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("AI Strategy");
        verify(strategyRepository).save(any());
    }

    @Test
    void generateStrategy_whenPendingExists_throws() {
        when(strategyRepository.existsByStatus(MarketingStrategyStatus.PENDING)).thenReturn(true);

        assertThatThrownBy(() -> strategyService.generateStrategy(
                GenerateStrategyRequest.builder().topic("T").build()))
                .isInstanceOf(StrategyConflictException.class)
                .hasMessageContaining("PENDING strategy already exists");
    }

    @Test
    void generateStrategy_whenAiResponseInvalid_throws() {
        when(strategyRepository.existsByStatus(MarketingStrategyStatus.PENDING)).thenReturn(false);
        when(strategyPromptBuilder.build(any(), any())).thenReturn("prompt");
        when(geminiService.generate(any())).thenReturn("not json");

        assertThatThrownBy(() -> strategyService.generateStrategy(
                GenerateStrategyRequest.builder().topic("T").build()))
                .isInstanceOf(StrategyGenerationException.class)
                .hasMessageContaining("Failed to parse");
    }

    @Test
    void generateAutoStrategy_usesContextAndGenerates() {
        when(memoryContextService.getRecentContext()).thenReturn("Brand context");
        when(geminiService.generate(contains("Based on this context")))
                .thenReturn("AI Marketing Trends");
        when(strategyRepository.existsByStatus(MarketingStrategyStatus.PENDING)).thenReturn(false);
        when(strategyPromptBuilder.build(any(), any())).thenReturn("prompt");
        when(geminiService.generate("prompt")).thenReturn("""
                {"title":"Auto Strategy","summary":"Auto","durationWeeks":8,"campaigns":[]}
                """);

        MarketingStrategy saved = MarketingStrategy.builder()
                .id(2L).title("Auto Strategy").status(MarketingStrategyStatus.PENDING).build();
        when(strategyRepository.save(any())).thenReturn(saved);

        MarketingStrategyDTO dto = MarketingStrategyDTO.builder()
                .id(2L).title("Auto Strategy").status("PENDING").build();
        when(marketingStrategyMapper.toDTO(saved)).thenReturn(dto);

        MarketingStrategyDTO result = strategyService.generateAutoStrategy();

        assertThat(result.getTitle()).isEqualTo("Auto Strategy");
        verify(memoryContextService).getRecentContext();
    }

    @Test
    void generateAutoStrategy_truncatesLongTopic() {
        when(memoryContextService.getRecentContext()).thenReturn("ctx");
        when(geminiService.generate(contains("Based on this context")))
                .thenReturn("a".repeat(100));
        when(strategyRepository.existsByStatus(MarketingStrategyStatus.PENDING)).thenReturn(false);
        when(strategyPromptBuilder.build(any(), any())).thenReturn("prompt");
        when(geminiService.generate("prompt")).thenReturn("""
                {"title":"T","summary":"","durationWeeks":8,"campaigns":[]}
                """);
        when(strategyRepository.save(any())).thenReturn(MarketingStrategy.builder().build());
        when(marketingStrategyMapper.toDTO(any())).thenReturn(MarketingStrategyDTO.builder().build());

        strategyService.generateAutoStrategy();
        verify(strategyRepository).save(any());
    }

    @Test
    void getActiveStrategy_returnsActive() {
        MarketingStrategy active = MarketingStrategy.builder().id(1L).build();
        when(strategyRepository.findFirstByStatusOrderByCreatedAtDesc(MarketingStrategyStatus.ACTIVE))
                .thenReturn(Optional.of(active));
        when(marketingStrategyMapper.toDTO(active)).thenReturn(MarketingStrategyDTO.builder().id(1L).build());

        assertThat(strategyService.getActiveStrategy()).isNotNull();
    }

    @Test
    void getActiveStrategy_returnsNullWhenNone() {
        when(strategyRepository.findFirstByStatusOrderByCreatedAtDesc(MarketingStrategyStatus.ACTIVE))
                .thenReturn(Optional.empty());
        assertThat(strategyService.getActiveStrategy()).isNull();
    }

    @Test
    void getAllStrategies_returnsAll() {
        when(strategyRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(
                MarketingStrategy.builder().id(1L).build()));
        when(marketingStrategyMapper.toDTO(any())).thenReturn(MarketingStrategyDTO.builder().id(1L).build());

        assertThat(strategyService.getAllStrategies()).hasSize(1);
    }

    @Test
    void getStrategy_found_returnsDTO() {
        MarketingStrategy ms = MarketingStrategy.builder().id(1L).build();
        when(strategyRepository.findById(1L)).thenReturn(Optional.of(ms));
        when(marketingStrategyMapper.toDTO(ms)).thenReturn(MarketingStrategyDTO.builder().id(1L).build());

        assertThat(strategyService.getStrategy(1L).getId()).isEqualTo(1L);
    }

    @Test
    void getStrategy_notFound_throws() {
        when(strategyRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> strategyService.getStrategy(99L))
                .isInstanceOf(StrategyNotFoundException.class)
                .hasMessageContaining("Strategy not found");
    }

    @Test
    void updateStrategy_updatesFields() {
        MarketingStrategy ms = MarketingStrategy.builder()
                .id(1L).status(MarketingStrategyStatus.PENDING).build();
        when(strategyRepository.findById(1L)).thenReturn(Optional.of(ms));
        when(strategyRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(marketingStrategyMapper.toDTO(any())).thenReturn(MarketingStrategyDTO.builder().id(1L).title("Updated").build());

        MarketingStrategyRequest req = new MarketingStrategyRequest();
        req.setTitle("Updated");
        req.setDurationWeeks(4);

        MarketingStrategyDTO result = strategyService.updateStrategy(1L, req);

        assertThat(result.getTitle()).isEqualTo("Updated");
        verify(strategyRepository).save(any());
    }

    @Test
    void updateStrategy_whenCompleted_throws() {
        MarketingStrategy ms = MarketingStrategy.builder().id(1L).status(MarketingStrategyStatus.COMPLETED).build();
        when(strategyRepository.findById(1L)).thenReturn(Optional.of(ms));

        assertThatThrownBy(() -> strategyService.updateStrategy(1L, new MarketingStrategyRequest()))
                .isInstanceOf(StrategyNotEditableException.class)
                .hasMessageContaining("Cannot edit");
    }

    @Test
    void updateStrategy_whenInactive_throws() {
        MarketingStrategy ms = MarketingStrategy.builder().id(1L).status(MarketingStrategyStatus.INACTIVE).build();
        when(strategyRepository.findById(1L)).thenReturn(Optional.of(ms));

        assertThatThrownBy(() -> strategyService.updateStrategy(1L, new MarketingStrategyRequest()))
                .isInstanceOf(StrategyNotEditableException.class)
                .hasMessageContaining("Cannot edit");
    }

    @Test
    void deactivateStrategy_setsInactive() {
        MarketingStrategy ms = MarketingStrategy.builder().id(1L).status(MarketingStrategyStatus.ACTIVE).build();
        when(strategyRepository.findById(1L)).thenReturn(Optional.of(ms));
        when(strategyRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(marketingStrategyMapper.toDTO(any())).thenReturn(MarketingStrategyDTO.builder().id(1L).status("INACTIVE").build());

        MarketingStrategyDTO result = strategyService.deactivateStrategy(1L);

        assertThat(result.getStatus()).isEqualTo("INACTIVE");
    }

    @Test
    void setAutoGenerate_togglesAndNotifies() {
        MarketingStrategy ms = MarketingStrategy.builder().id(1L).title("Test").autoGenerate(false).build();
        when(strategyRepository.findById(1L)).thenReturn(Optional.of(ms));
        when(strategyRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(marketingStrategyMapper.toDTO(any())).thenReturn(MarketingStrategyDTO.builder().id(1L).autoGenerate(true).build());

        MarketingStrategyDTO result = strategyService.setAutoGenerate(1L, true);

        assertThat(result.getAutoGenerate()).isTrue();
        verify(notificationService).createNotification(contains("enabled"), any(), any());
    }

    @Test
    void setAutoGenerate_disabled_notifies() {
        MarketingStrategy ms = MarketingStrategy.builder().id(1L).title("Test").autoGenerate(true).build();
        when(strategyRepository.findById(1L)).thenReturn(Optional.of(ms));
        when(strategyRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(marketingStrategyMapper.toDTO(any())).thenReturn(MarketingStrategyDTO.builder().id(1L).autoGenerate(false).build());

        strategyService.setAutoGenerate(1L, false);

        verify(notificationService).createNotification(contains("disabled"), any(), any());
    }

    @Test
    void autoCompleteExpiredStrategies_completesExpired() {
        MarketingStrategy expired = MarketingStrategy.builder()
                .id(1L).expectedEndDate(LocalDate.now().minusDays(1))
                .status(MarketingStrategyStatus.ACTIVE).build();
        MarketingStrategy notExpired = MarketingStrategy.builder()
                .id(2L).expectedEndDate(LocalDate.now().plusDays(5))
                .status(MarketingStrategyStatus.ACTIVE).build();
        when(strategyRepository.findAll()).thenReturn(List.of(expired, notExpired));
        when(strategyRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        strategyService.autoCompleteExpiredStrategies();

        assertThat(expired.getStatus()).isEqualTo(MarketingStrategyStatus.COMPLETED);
        assertThat(notExpired.getStatus()).isEqualTo(MarketingStrategyStatus.ACTIVE);
        verify(strategyRepository, times(1)).save(expired);
    }

    @Test
    void getStrategy_notFoundById_throws() {
        when(strategyRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> strategyService.getStrategy(99L))
                .isInstanceOf(StrategyNotFoundException.class)
                .hasMessageContaining("Strategy not found");
    }
}
