package com.demo.ranktracker.controller;

import com.demo.ranktracker.dto.WebsiteCreateRequest;
import com.demo.ranktracker.dto.WebsiteDTO;
import com.demo.ranktracker.service.WebsiteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WebsiteController.class)
class WebsiteControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WebsiteService websiteService;

    @Test
    void getAllWebsites_shouldReturn200AndJsonArray() throws Exception {
        WebsiteDTO dto = new WebsiteDTO(1L, "embarkx.com", "EmbarkX", 5L, LocalDateTime.now(), LocalDateTime.now());
        when(websiteService.getAllWebsites()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/websites"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].domain").value("embarkx.com"))
                .andExpect(jsonPath("$[0].keywordCount").value(5));
    }

    @Test
    void createWebsite_shouldReturn201WhenValid() throws Exception {
        WebsiteCreateRequest req = new WebsiteCreateRequest("embarkx.com", "EmbarkX");
        WebsiteDTO created = new WebsiteDTO(1L, "embarkx.com", "EmbarkX", 0L, LocalDateTime.now(), LocalDateTime.now());

        when(websiteService.createWebsite(any(WebsiteCreateRequest.class))).thenReturn(created);

        mockMvc.perform(post("/api/websites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.domain").value("embarkx.com"));
    }

    @Test
    void createWebsite_shouldReturn400WhenInvalidDomain() throws Exception {
        WebsiteCreateRequest req = new WebsiteCreateRequest("invalid domain with spaces", "Invalid");

        mockMvc.perform(post("/api/websites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.domain").exists());
    }
}
