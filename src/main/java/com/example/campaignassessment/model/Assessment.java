package com.example.campaignassessment.model;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Schema(description = "Feasibility assessment produced from a submitted campaign configuration")
public record Assessment(

    @Schema(description = "Unique identifier for this assessment", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    String id,

    @Schema(description = "The advertising channel from the submitted request")
    ChannelType channelType,

    @Schema(description = "The budget from the submitted request", example = "5000.00")
    BigDecimal budget,

    @Schema(description = "The target country from the submitted request", example = "GB")
    String targetCountry,

    @Schema(description = "The flight duration from the submitted request, if provided", example = "30")
    Integer flightDuration,

    @Schema(description = "Estimated number of unique individuals the campaign could reach", example = "450000")
    long estimatedReach,

    @Schema(description = "Recommended number of respondents for measurement purposes", example = "1200")
    int recommendedSampleSize,

    @Schema(description = "Advisory warnings about the campaign configuration, if any")
    List<String> warnings,

    @Schema(description = "Timestamp at which this assessment was created")
    Instant createdAt

) {}
