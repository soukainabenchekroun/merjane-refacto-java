package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@UnitTest
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private NotificationService notificationService;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(
                LocalDate.of(2026, 5, 8)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant(),
                ZoneId.systemDefault()
        );

        productService = new ProductService(
                productRepository,
                notificationService,
                fixedClock
        );
    }

    @Test
    void shouldSaveProductAndSendDelayNotification() {
        Product product = new Product(
                null,
                15,
                0,
                "NORMAL",
                "USB Dongle",
                null,
                null,
                null
        );

        productService.notifyDelay(15, product);

        assertEquals(15, product.getLeadTime());
        verify(productRepository).save(product);
        verify(notificationService).sendDelayNotification(15, "USB Dongle");
    }

    @Test
    void shouldSetAvailableToZeroAndNotifyOutOfStockWhenRestockDateIsAfterSeasonEnd() {
        Product product = new Product(
                null,
                40,
                0,
                "SEASONAL",
                "Watermelon",
                null,
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 6, 1)
        );

        productService.handleSeasonalProduct(product);

        assertEquals(0, product.getAvailable());
        verify(notificationService).sendOutOfStockNotification("Watermelon");
        verify(productRepository).save(product);
        verify(notificationService, never()).sendDelayNotification(anyInt(), anyString());
    }

    @Test
    void shouldNotifyOutOfStockWhenSeasonHasNotStartedYet() {
        Product product = new Product(
                null,
                10,
                5,
                "SEASONAL",
                "Grapes",
                null,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 7, 1)
        );

        productService.handleSeasonalProduct(product);

        assertEquals(5, product.getAvailable());
        verify(notificationService).sendOutOfStockNotification("Grapes");
        verify(productRepository).save(product);
        verify(notificationService, never()).sendDelayNotification(anyInt(), anyString());
    }

    @Test
    void shouldNotifyDelayWhenSeasonalProductCanBeRestockedBeforeSeasonEnd() {
        Product product = new Product(
                null,
                5,
                0,
                "SEASONAL",
                "Melon",
                null,
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 6, 1)
        );

        productService.handleSeasonalProduct(product);

        assertEquals(0, product.getAvailable());
        verify(productRepository).save(product);
        verify(notificationService).sendDelayNotification(5, "Melon");
        verify(notificationService, never()).sendOutOfStockNotification(anyString());
    }

    @Test
    void shouldDecreaseAvailableWhenExpirableProductIsNotExpiredAndInStock() {
        Product product = new Product(
                null,
                5,
                3,
                "EXPIRABLE",
                "Butter",
                LocalDate.of(2026, 5, 20),
                null,
                null
        );

        productService.handleExpiredProduct(product);

        assertEquals(2, product.getAvailable());
        verify(productRepository).save(product);
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSetAvailableToZeroAndNotifyExpirationWhenProductIsExpired() {
        Product product = new Product(
                null,
                5,
                3,
                "EXPIRABLE",
                "Milk",
                LocalDate.of(2026, 5, 1),
                null,
                null
        );

        productService.handleExpiredProduct(product);

        assertEquals(0, product.getAvailable());
        verify(notificationService).sendExpirationNotification("Milk", LocalDate.of(2026, 5, 1));
        verify(productRepository).save(product);
    }

    @Test
    void shouldSetAvailableToZeroAndNotifyExpirationWhenExpirableProductIsOutOfStock() {
        Product product = new Product(
                null,
                5,
                0,
                "EXPIRABLE",
                "Yogurt",
                LocalDate.of(2026, 5, 20),
                null,
                null
        );

        productService.handleExpiredProduct(product);

        assertEquals(0, product.getAvailable());
        verify(notificationService).sendExpirationNotification("Yogurt", LocalDate.of(2026, 5, 20));
        verify(productRepository).save(product);
    }
}