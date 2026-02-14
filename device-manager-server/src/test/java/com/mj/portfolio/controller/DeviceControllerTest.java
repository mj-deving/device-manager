package com.mj.portfolio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mj.portfolio.dto.DeviceRequest;
import com.mj.portfolio.dto.DeviceResponse;
import com.mj.portfolio.entity.enums.DeviceStatus;
import com.mj.portfolio.entity.enums.DeviceType;
import com.mj.portfolio.exception.DeviceNotFoundException;
import com.mj.portfolio.service.DeviceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DeviceController.class)
class DeviceControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean DeviceService service;

    private DeviceResponse sampleResponse() {
        // Use reflection-free builder-style via setters would require a mutable object;
        // here we rely on the static factory (tested via service layer).
        // Instead, create a minimal DeviceResponse via the static from() only possible
        // with a Device entity. We use Mockito stubbing to return this from service.
        UUID id = UUID.randomUUID();
        // Build DeviceResponse manually via a Device (white-box, acceptable in tests)
        com.mj.portfolio.entity.Device d = new com.mj.portfolio.entity.Device();
        d.setName("Router-1");
        d.setType(DeviceType.ROUTER);
        d.setStatus(DeviceStatus.ACTIVE);
        d.setIpAddress("10.0.0.1");
        d.setLocation("Rack A");
        return DeviceResponse.from(d);
    }

    @Test
    void getList_returns200() throws Exception {
        when(service.findAll(any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleResponse())));

        mockMvc.perform(get("/api/v1/devices"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Router-1"));
    }

    @Test
    void getById_returns200_whenFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.findById(id)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/v1/devices/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Router-1"));
    }

    @Test
    void getById_returns404_whenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.findById(id)).thenThrow(new DeviceNotFoundException(id));

        mockMvc.perform(get("/api/v1/devices/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_returns201_withValidBody() throws Exception {
        DeviceRequest req = new DeviceRequest();
        req.setName("Switch-B");
        req.setType(DeviceType.SERVER);

        when(service.create(any(DeviceRequest.class))).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void create_returns400_withMissingName() throws Exception {
        DeviceRequest req = new DeviceRequest();
        req.setType(DeviceType.SERVER);
        // name is missing — should fail @NotBlank

        mockMvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_returns204() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/devices/{id}", id))
                .andExpect(status().isNoContent());
    }
}
