package com.example.campaignassessment.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Isolated context with a low rate limit so the test does not need to fire
// the full 10 requests configured for production.
@SpringBootTest(properties = {
    "api.rate-limit.requests-per-minute=2",
    "api.key=RATE-LIMIT-TEST-KEY"
})
@AutoConfigureMockMvc
class RateLimitIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    private static final String KEY = "RATE-LIMIT-TEST-KEY";

    private static final String VALID_REQUEST = """
        {"channelType": "EMAIL", "budget": 1000, "targetCountry": "US"}
        """;

    @Test
    void exceedingRateLimit_returns429() throws Exception {
        // First two requests should be allowed.
        for (int i = 0; i < 2; i++) {
            mockMvc.perform(post("/assessments")
                    .header("X-API-Key", KEY)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(VALID_REQUEST))
                .andExpect(status().isCreated());
        }

        // Third request exceeds the per-minute limit.
        mockMvc.perform(post("/assessments")
                .header("X-API-Key", KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_REQUEST))
            .andExpect(status().is(429));
    }
}
