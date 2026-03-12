package com.example.campaignassessment.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record Assessment(
    String id,
    ChannelType channelType,
    BigDecimal budget,
    String targetCountry,
    Integer flightDuration,
    long estimatedReach,
    int recommendedSampleSize,
    List<String> warnings,
    Instant createdAt
) {}
