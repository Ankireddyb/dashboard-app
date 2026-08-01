package com.i27academy.dashboard.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PipelineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String SERVICES = "/api/v1/services";
    private static final String PIPELINES = "/api/v1/pipelines";

    private String createService(String name) throws Exception {
        var result = mockMvc.perform(post(SERVICES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"%s\",\"ownerTeam\":\"team\"}".formatted(name)))
                .andReturn();
        return com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.id").toString();
    }

    private String pipelineJson(String serviceId, String branch) {
        return """
                {"serviceId":%s,"pipelineName":"ci","branch":"%s","triggeredBy":"push"}
                """.formatted(serviceId, branch);
    }

    @Test
    void createPipelineRun() throws Exception {
        var svcId = createService("pipe-svc");
        mockMvc.perform(post(PIPELINES).contentType(MediaType.APPLICATION_JSON)
                        .content(pipelineJson(svcId, "main")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status", is("QUEUED")));
    }

    @Test
    void listPipelineRunsWithBranchFilter() throws Exception {
        var svcId = createService("branch-filter-svc");
        mockMvc.perform(post(PIPELINES).contentType(MediaType.APPLICATION_JSON).content(pipelineJson(svcId, "main")));
        mockMvc.perform(post(PIPELINES).contentType(MediaType.APPLICATION_JSON).content(pipelineJson(svcId, "develop")));

        mockMvc.perform(get(PIPELINES + "?branch=main"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].branch", is("main")));
    }

    @Test
    void updatePipelineRunToSuccess() throws Exception {
        var svcId = createService("pipe-success-svc");
        var result = mockMvc.perform(post(PIPELINES).contentType(MediaType.APPLICATION_JSON)
                        .content(pipelineJson(svcId, "main")))
                .andReturn();
        var id = com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.id").toString();

        mockMvc.perform(patch(PIPELINES + "/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"SUCCESS\",\"durationSeconds\":120}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.durationSeconds", is(120)))
                .andExpect(jsonPath("$.completedAt", notNullValue()));
    }

    @Test
    void getPipelineRunNotFound() throws Exception {
        mockMvc.perform(get(PIPELINES + "/99999"))
                .andExpect(status().isNotFound());
    }
}
