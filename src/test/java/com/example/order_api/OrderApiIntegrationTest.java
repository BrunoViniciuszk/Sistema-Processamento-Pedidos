package com.example.order_api;

import com.example.order_api.dto.CreateOrderRequest;
import com.example.order_api.dto.OrderEventResponse;
import com.example.order_api.dto.OrderResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "app.job.interval=100"
})
class OrderApiIntegrationTest extends BaseIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    @DisplayName("Deve processar fluxo completo: CREATED -> TRANSPORT -> DELIVERED")
    void shouldProcessFullOrderFlow() throws Exception {

        CreateOrderRequest request = new CreateOrderRequest("customer-test");


        MvcResult result = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID orderId = objectMapper.readValue(
                result.getResponse().getContentAsString(), OrderResponse.class).id();


        await().atMost(10, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {

                    MvcResult check = mockMvc.perform(get("/api/orders/" + orderId))
                            .andExpect(status().isOk())
                            .andReturn();

                    String status = objectMapper.readValue(
                            check.getResponse().getContentAsString(), OrderResponse.class).status();

                    assertThat(status).isEqualTo("DELIVERED");
                });


        MvcResult eventsResult = mockMvc.perform(get("/api/orders/" + orderId + "/events"))
                .andExpect(status().isOk())
                .andReturn();

        List<OrderEventResponse> events = objectMapper.readValue(
                eventsResult.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, OrderEventResponse.class)
        );


        assertThat(events).hasSize(3);
        assertThat(events).extracting("eventType")
                .containsExactly("ORDER_CREATED", "IN_TRANSPORT", "DELIVERED");
    }
}