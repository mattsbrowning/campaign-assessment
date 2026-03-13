package com.example.campaignassessment.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "api.rate-limit.requests-per-minute=50")
@AutoConfigureMockMvc
class AssessmentControllerTest {

    @Autowired
    MockMvc mockMvc;

    private static final String VALID_KEY = "EXAMPLE-KEY";

    private static final String VALID_REQUEST = """
        {
          "channelType": "SOCIAL_MEDIA",
          "budget": 5000,
          "targetCountry": "GB"
        }
        """;

    // --- Authentication ---

    @Test
    void post_missingApiKey_returns401() throws Exception {
        mockMvc.perform(post("/assessments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_REQUEST))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void post_wrongApiKey_returns401() throws Exception {
        mockMvc.perform(post("/assessments")
                .header("X-API-Key", "WRONG-KEY")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_REQUEST))
            .andExpect(status().isUnauthorized());
    }

    // --- Input validation ---

    @Test
    void post_missingChannelType_returns400() throws Exception {
        mockMvc.perform(post("/assessments")
                .header("X-API-Key", VALID_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"budget": 5000, "targetCountry": "GB"}
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void post_negativeBudget_returns400() throws Exception {
        mockMvc.perform(post("/assessments")
                .header("X-API-Key", VALID_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"channelType": "EMAIL", "budget": -100, "targetCountry": "US"}
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void post_invalidCountryCode_returns400() throws Exception {
        mockMvc.perform(post("/assessments")
                .header("X-API-Key", VALID_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"channelType": "EMAIL", "budget": 1000, "targetCountry": "Britain"}
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void post_zeroBudget_returns400() throws Exception {
        mockMvc.perform(post("/assessments")
                .header("X-API-Key", VALID_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"channelType": "EMAIL", "budget": 0, "targetCountry": "US"}
                    """))
            .andExpect(status().isBadRequest());
    }

    // --- Happy path ---

    @Test
    void post_validRequest_returns201WithHateoasLinks() throws Exception {
        mockMvc.perform(post("/assessments")
                .header("X-API-Key", VALID_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_REQUEST))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.estimatedReach").isNumber())
            .andExpect(jsonPath("$.recommendedSampleSize").isNumber())
            .andExpect(jsonPath("$._links.self.href").exists())
            .andExpect(jsonPath("$._links.assessments.href").exists());
    }

    @Test
    void post_withOptionalFlightDuration_returns201() throws Exception {
        mockMvc.perform(post("/assessments")
                .header("X-API-Key", VALID_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"channelType": "DISPLAY", "budget": 10000, "targetCountry": "US", "flightDuration": 14}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.flightDuration").value(14));
    }

    @Test
    void getById_existingAssessment_returns200() throws Exception {
        String location = mockMvc.perform(post("/assessments")
                .header("X-API-Key", VALID_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_REQUEST))
            .andReturn().getResponse().getHeader("Location");

        String id = location.substring(location.lastIndexOf('/') + 1);

        mockMvc.perform(get("/assessments/" + id)
                .header("X-API-Key", VALID_KEY))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void getById_unknownId_returns404() throws Exception {
        mockMvc.perform(get("/assessments/does-not-exist")
                .header("X-API-Key", VALID_KEY))
            .andExpect(status().isNotFound());
    }

    @Test
    void getAll_returns200WithSelfLink() throws Exception {
        mockMvc.perform(get("/assessments")
                .header("X-API-Key", VALID_KEY))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._links.self.href").exists());
    }
}
