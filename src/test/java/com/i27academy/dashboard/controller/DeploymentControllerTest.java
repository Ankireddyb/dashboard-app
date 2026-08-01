package com.i27academy.dashboard.controller;

import org.junit.jupiter.api.BeforeEach;
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
class DeploymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String SERVICES = "/api/v1/services";
    private static final String DEPLOYMENTS = "/api/v1/deployments";

    private String createService(String name) throws Exception {
        var result = mockMvc.perform(post(SERVICES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"%s\",\"ownerTeam\":\"team\"}".formatted(name)))
                .andReturn();
        return com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.id").toString();
    }

    private String deploymentJson(String serviceId, String env) {
        return """
                {"serviceId":%s,"version":"v1.0","environment":"%s","triggeredBy":"ci-bot"}
                """.formatted(serviceId, env);
    }

    @Test
    void createDeployment() throws Exception {
        var svcId = createService("deploy-svc");
        mockMvc.perform(post(DEPLOYMENTS).contentType(MediaType.APPLICATION_JSON)
                        .content(deploymentJson(svcId, "STAGING")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test
    void createDeploymentInvalidServiceReturns404() throws Exception {
        mockMvc.perform(post(DEPLOYMENTS).contentType(MediaType.APPLICATION_JSON)
                        .content(deploymentJson("99999", "STAGING")))
                .andExpect(status().isNotFound());
    }

    @Test
    void listDeployments() throws Exception {
        var svcId = createService("list-deploy-svc");
        mockMvc.perform(post(DEPLOYMENTS).contentType(MediaType.APPLICATION_JSON).content(deploymentJson(svcId, "STAGING")));
        mockMvc.perform(get(DEPLOYMENTS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void filterDeploymentsByEnvironment() throws Exception {
        var svcId = createService("env-filter-svc");
        mockMvc.perform(post(DEPLOYMENTS).contentType(MediaType.APPLICATION_JSON).content(deploymentJson(svcId, "STAGING")));
        mockMvc.perform(post(DEPLOYMENTS).contentType(MediaType.APPLICATION_JSON).content(deploymentJson(svcId, "PRODUCTION")));

        mockMvc.perform(get(DEPLOYMENTS + "?environment=STAGING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].environment", is("STAGING")));
    }

    @Test
    void updateDeploymentStatusToSuccess() throws Exception {
        var svcId = createService("status-svc");
        var result = mockMvc.perform(post(DEPLOYMENTS).contentType(MediaType.APPLICATION_JSON)
                        .content(deploymentJson(svcId, "PRODUCTION")))
                .andReturn();
        var id = com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.id").toString();

        mockMvc.perform(patch(DEPLOYMENTS + "/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"SUCCESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.completedAt", notNullValue()));
    }

    @Test
    void updateDeploymentStatusToFailed() throws Exception {
        var svcId = createService("fail-svc");
        var result = mockMvc.perform(post(DEPLOYMENTS).contentType(MediaType.APPLICATION_JSON)
                        .content(deploymentJson(svcId, "STAGING")))
                .andReturn();
        var id = com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.id").toString();

        mockMvc.perform(patch(DEPLOYMENTS + "/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"FAILED\",\"notes\":\"build error\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("FAILED")))
                .andExpect(jsonPath("$.notes", is("build error")));
    }
}
