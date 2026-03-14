package com.example.campaignassessment.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Configuration submitted for feasibility assessment")
public record CampaignRequest(

    @Schema(description = "The advertising channel through which the campaign will run",
        example = "SOCIAL_MEDIA")
    @NotNull(message = "channelType is required")
    ChannelType channelType,

    @Schema(description = "Total campaign budget in GBP. Must be greater than zero.",
        example = "5000.00")
    @NotNull(message = "budget is required")
    @Positive(message = "budget must be greater than zero")
    BigDecimal budget,

    @Schema(description = "ISO 3166-1 alpha-2 country code identifying the target market",
        example = "GB")
    @NotBlank(message = "targetCountry is required")
    @Pattern(
        regexp = "[A-Z]{2}",
        message = "targetCountry must be a 2-letter ISO 3166-1 alpha-2 country code (e.g. GB, US)"
    )
    String targetCountry,

    @Schema(description = "Optional campaign run length in days. Influences the estimated reach calculation.",
        example = "30")
    @Positive(message = "flightDuration must be a positive number of days")
    Integer flightDuration

) {}
