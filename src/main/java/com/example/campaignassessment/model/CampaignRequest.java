package com.example.campaignassessment.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CampaignRequest(

    @NotNull(message = "channelType is required")
    ChannelType channelType,

    @NotNull(message = "budget is required")
    @Positive(message = "budget must be greater than zero")
    BigDecimal budget,

    @NotBlank(message = "targetCountry is required")
    @Pattern(
        regexp = "[A-Z]{2}",
        message = "targetCountry must be a 2-letter ISO 3166-1 alpha-2 country code (e.g. GB, US)"
    )
    String targetCountry,

    // Optional: campaign run length in days; influences estimated reach when provided
    @Positive(message = "flightDuration must be a positive number of days")
    Integer flightDuration

) {}
