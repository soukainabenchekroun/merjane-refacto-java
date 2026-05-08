package com.nimbleways.springboilerplate.controllers;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    void shouldProcessOrderAndApplyBusinessRules() throws Exception {
        // GIVEN
        Product normalInStock = new Product(
                null,
                15,
                30,
                "NORMAL",
                "USB Cable",
                null,
                null,
                null
        );

        Product normalOutOfStock = new Product(
                null,
                10,
                0,
                "NORMAL",
                "USB Dongle",
                null,
                null,
                null
        );

        Product expirableNotExpired = new Product(
                null,
                15,
                30,
                "EXPIRABLE",
                "Butter",
                LocalDate.now().plusDays(26),
                null,
                null
        );

        Product expirableExpired = new Product(
                null,
                90,
                6,
                "EXPIRABLE",
                "Milk",
                LocalDate.now().minusDays(2),
                null,
                null
        );

        Product seasonalInStock = new Product(
                null,
                15,
                30,
                "SEASONAL",
                "Watermelon",
                null,
                LocalDate.now().minusDays(2),
                LocalDate.now().plusDays(58)
        );

        Product seasonalNotStarted = new Product(
                null,
                15,
                30,
                "SEASONAL",
                "Grapes",
                null,
                LocalDate.now().plusDays(180),
                LocalDate.now().plusDays(240)
        );

        productRepository.saveAll(List.of(
                normalInStock,
                normalOutOfStock,
                expirableNotExpired,
                expirableExpired,
                seasonalInStock,
                seasonalNotStarted
        ));

        Order order = new Order();
        order.setItems(new HashSet<>(List.of(
                normalInStock,
                normalOutOfStock,
                expirableNotExpired,
                expirableExpired,
                seasonalInStock,
                seasonalNotStarted
        )));

        order = orderRepository.save(order);

        // WHEN
        mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId())
                        .contentType("application/json"))
                .andExpect(status().isOk());

        // THEN
        Product updatedNormalInStock = productRepository.findFirstByName("USB Cable").get();
        Product updatedNormalOutOfStock = productRepository.findFirstByName("USB Dongle").get();
        Product updatedExpirableNotExpired = productRepository.findFirstByName("Butter").get();
        Product updatedExpirableExpired = productRepository.findFirstByName("Milk").get();
        Product updatedSeasonalInStock = productRepository.findFirstByName("Watermelon").get();
        Product updatedSeasonalNotStarted = productRepository.findFirstByName("Grapes").get();

        assertEquals(29, updatedNormalInStock.getAvailable());
        assertEquals(0, updatedNormalOutOfStock.getAvailable());

        assertEquals(29, updatedExpirableNotExpired.getAvailable());
        assertEquals(0, updatedExpirableExpired.getAvailable());

        assertEquals(29, updatedSeasonalInStock.getAvailable());
        assertEquals(30, updatedSeasonalNotStarted.getAvailable());

        verify(notificationService).sendDelayNotification(10, "USB Dongle");
        verify(notificationService).sendExpirationNotification("Milk", expirableExpired.getExpiryDate());
        verify(notificationService).sendOutOfStockNotification("Grapes");
    }
}