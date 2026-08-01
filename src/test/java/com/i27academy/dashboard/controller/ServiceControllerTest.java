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

import com.i27academy.dashboard.repository.ServiceRepository;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ServiceRepository serviceRepository;

    private static final String BASE = "/api/v1/services";

    private String serviceJson(String name) {
        return """
                {"name":"%s","description":"desc","repositoryUrl":"https://github.com/test","ownerTeam":"team-a"}
                """.formatted(name);
    }

    @Test
    void createService() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(serviceJson("svc-1")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name", is("svc-1")));
    }

    @Test
    void createDuplicateServiceReturns409() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(serviceJson("dup-svc")))
                .andExpect(status().isCreated());
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(serviceJson("dup-svc")))
                .andExpect(status().isConflict());
    }

    @Test
    void listServicesEmpty() throws Exception {
        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void listServices() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(serviceJson("svc-a")));
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(serviceJson("svc-b")));
        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void getServiceById() throws Exception {
        var result = mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(serviceJson("svc-get")))
                .andReturn();
        var body = result.getResponse().getContentAsString();
        var id = com.jayway.jsonpath.JsonPath.read(body, "$.id").toString();

        mockMvc.perform(get(BASE + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("svc-get")));
    }

    @Test
    void getNonexistentServiceReturns404() throws Exception {
        mockMvc.perform(get(BASE + "/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateService() throws Exception {
        var result = mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(serviceJson("svc-update")))
                .andReturn();
        var id = com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.id").toString();

        mockMvc.perform(patch(BASE + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ownerTeam\":\"team-b\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerTeam", is("team-b")));
    }

    @Test
    void deleteService() throws Exception {
        var result = mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(serviceJson("svc-del")))
                .andReturn();
        var id = com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.id").toString();

        mockMvc.perform(delete(BASE + "/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(BASE + "/" + id))
                .andExpect(status().isNotFound());
    }
}
