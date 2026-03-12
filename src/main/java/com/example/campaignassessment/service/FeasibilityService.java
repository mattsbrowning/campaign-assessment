package com.example.campaignassessment.service;

import com.example.campaignassessment.model.Assessment;
import com.example.campaignassessment.model.CampaignRequest;
import com.example.campaignassessment.model.ChannelType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class FeasibilityService {

    // Estimated unique impressions per £1,000 of budget, by channel.
    // These figures are illustrative; in production they would be drawn from
    // observed campaign data or a media planning data source.
    private static final Map<ChannelType, Long> BASE_REACH_PER_THOUSAND = Map.of(
        ChannelType.EMAIL,        80_000L,
        ChannelType.SOCIAL_MEDIA, 120_000L,
        ChannelType.DISPLAY,      200_000L,
        ChannelType.SEARCH,        40_000L,
        ChannelType.TV,           600_000L
    );

    // Relative addressable-audience size by country.
    // Unlisted countries receive the default factor.
    private static final Map<String, Double> COUNTRY_FACTOR = Map.of(
        "US", 1.00,
        "GB", 0.75,
        "DE", 0.60,
        "FR", 0.55,
        "AU", 0.40
    );

    private static final double DEFAULT_COUNTRY_FACTOR = 0.35;
    private static final int    DEFAULT_FLIGHT_DAYS    = 30;
    private static final double SAMPLE_RATE            = 0.02;
    private static final int    MIN_SAMPLE_SIZE        = 200;
    private static final int    MAX_SAMPLE_SIZE        = 5_000;

    public Assessment assess(CampaignRequest request) {
        long reach      = estimateReach(request);
        int  sampleSize = recommendSampleSize(reach);
        List<String> warnings = generateWarnings(request, reach);

        return new Assessment(
            UUID.randomUUID().toString(),
            request.channelType(),
            request.budget(),
            request.targetCountry(),
            request.flightDuration(),
            reach,
            sampleSize,
            warnings,
            Instant.now()
        );
    }

    private long estimateReach(CampaignRequest request) {
        long   baseRate      = BASE_REACH_PER_THOUSAND.get(request.channelType());
        double countryFactor = COUNTRY_FACTOR.getOrDefault(request.targetCountry(), DEFAULT_COUNTRY_FACTOR);
        int    flightDays    = request.flightDuration() != null ? request.flightDuration() : DEFAULT_FLIGHT_DAYS;

        // Reach scales with flight duration but with diminishing returns on unique audience.
        double flightFactor   = Math.sqrt((double) flightDays / DEFAULT_FLIGHT_DAYS);
        double budgetThousands = request.budget().doubleValue() / 1_000.0;

        return Math.round(budgetThousands * baseRate * countryFactor * flightFactor);
    }

    private int recommendSampleSize(long reach) {
        int sample = (int) Math.round(reach * SAMPLE_RATE);
        return Math.max(MIN_SAMPLE_SIZE, Math.min(MAX_SAMPLE_SIZE, sample));
    }

    private List<String> generateWarnings(CampaignRequest request, long reach) {
        List<String> warnings = new ArrayList<>();

        if (request.channelType() == ChannelType.TV
                && request.budget().compareTo(new BigDecimal("5000")) < 0) {
            warnings.add("Budget may be insufficient for TV; a minimum of £5,000 is recommended.");
        }

        if (request.budget().compareTo(new BigDecimal("1000")) < 0) {
            warnings.add("Low budget may significantly limit reach.");
        }

        if (reach < 10_000) {
            warnings.add("Estimated reach is low; consider increasing budget or broadening targeting.");
        }

        if (request.flightDuration() != null && request.flightDuration() < 7) {
            warnings.add("Flight duration under 7 days may not allow sufficient time for campaign optimisation.");
        }

        if (request.flightDuration() != null && request.flightDuration() > 90) {
            warnings.add("Extended flight duration beyond 90 days may reduce budget efficiency.");
        }

        return Collections.unmodifiableList(warnings);
    }
}
