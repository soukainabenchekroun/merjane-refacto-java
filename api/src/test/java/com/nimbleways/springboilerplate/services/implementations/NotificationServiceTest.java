package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@UnitTest
class NotificationServiceTest {

    private final NotificationService notificationService = new NotificationService();

    @Test
    void shouldSendDelayNotificationWithoutThrowingException() {
        assertDoesNotThrow(() ->
                notificationService.sendDelayNotification(10, "USB Dongle")
        );
    }

    @Test
    void shouldSendOutOfStockNotificationWithoutThrowingException() {
        assertDoesNotThrow(() ->
                notificationService.sendOutOfStockNotification("Watermelon")
        );
    }

    @Test
    void shouldSendExpirationNotificationWithoutThrowingException() {
        assertDoesNotThrow(() ->
                notificationService.sendExpirationNotification("Milk", LocalDate.of(2026, 5, 1))
        );
    }
}