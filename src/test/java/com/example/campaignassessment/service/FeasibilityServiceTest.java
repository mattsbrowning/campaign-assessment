package com.example.campaignassessment.service;

import com.example.campaignassessment.model.Assessment;
import com.example.campaignassessment.model.CampaignRequest;
import com.example.campaignassessment.model.ChannelType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class FeasibilityServiceTest {

    private final FeasibilityService service = new FeasibilityService();

    @Test
    void assess_returnsAssessmentWithId() {
        CampaignRequest request = new CampaignRequest(ChannelType.SOCIAL_MEDIA, new BigDecimal("5000"), "GB", null);
        Assessment assessment = service.assess(request);
        assertThat(assessment.id()).isNotNull();
        assertThat(assessment.createdAt()).isNotNull();
    }

    @Test
    void assess_echoesInputsIntoAssessment() {
        CampaignRequest request = new CampaignRequest(ChannelType.SEARCH, new BigDecimal("3000"), "DE", 14);
        Assessment assessment = service.assess(request);
        assertThat(assessment.channelType()).isEqualTo(ChannelType.SEARCH);
        assertThat(assessment.budget()).isEqualByComparingTo(new BigDecimal("3000"));
        assertThat(assessment.targetCountry()).isEqualTo("DE");
        assertThat(assessment.flightDuration()).isEqualTo(14);
    }

    @Test
    void assess_estimatedReachIsPositive() {
        CampaignRequest request = new CampaignRequest(ChannelType.DISPLAY, new BigDecimal("5000"), "US", null);
        assertThat(service.assess(request).estimatedReach()).isPositive();
    }

    @Test
    void assess_longerFlightDurationIncreasesReach() {
        CampaignRequest shortFlight = new CampaignRequest(ChannelType.DISPLAY, new BigDecimal("5000"), "US", 10);
        CampaignRequest longFlight  = new CampaignRequest(ChannelType.DISPLAY, new BigDecimal("5000"), "US", 60);
        assertThat(service.assess(longFlight).estimatedReach())
            .isGreaterThan(service.assess(shortFlight).estimatedReach());
    }

    @Test
    void assess_sampleSizeIsWithinConfiguredBounds() {
        // Use a very large budget to hit the upper bound and a tiny one for the lower.
        CampaignRequest large = new CampaignRequest(ChannelType.TV, new BigDecimal("500000"), "US", null);
        CampaignRequest small = new CampaignRequest(ChannelType.EMAIL, new BigDecimal("10"), "US", null);
        assertThat(service.assess(large).recommendedSampleSize()).isLessThanOrEqualTo(5_000);
        assertThat(service.assess(small).recommendedSampleSize()).isGreaterThanOrEqualTo(200);
    }

    @Test
    void assess_lowBudget_producesLowBudgetWarning() {
        CampaignRequest request = new CampaignRequest(ChannelType.EMAIL, new BigDecimal("500"), "US", null);
        assertThat(service.assess(request).warnings())
            .anyMatch(w -> w.contains("Low budget"));
    }

    @Test
    void assess_tvWithBudgetUnder5000_producesTvWarning() {
        CampaignRequest request = new CampaignRequest(ChannelType.TV, new BigDecimal("1000"), "GB", null);
        assertThat(service.assess(request).warnings())
            .anyMatch(w -> w.contains("TV"));
    }

    @Test
    void assess_flightDurationUnder7Days_producesWarning() {
        CampaignRequest request = new CampaignRequest(ChannelType.EMAIL, new BigDecimal("5000"), "US", 3);
        assertThat(service.assess(request).warnings())
            .anyMatch(w -> w.contains("7 days"));
    }

    @Test
    void assess_flightDurationOver90Days_producesWarning() {
        CampaignRequest request = new CampaignRequest(ChannelType.EMAIL, new BigDecimal("5000"), "US", 120);
        assertThat(service.assess(request).warnings())
            .anyMatch(w -> w.contains("90 days"));
    }

    @Test
    void assess_noWarningsForHealthyRequest() {
        CampaignRequest request = new CampaignRequest(ChannelType.SOCIAL_MEDIA, new BigDecimal("10000"), "US", 30);
        assertThat(service.assess(request).warnings()).isEmpty();
    }
}
