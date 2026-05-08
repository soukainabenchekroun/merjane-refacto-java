package com.nimbleways.springboilerplate.services.processors;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.ProductService;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@UnitTest
class ExpirableProductProcessorTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductService productService;

    private ExpirableProductProcessor expirableProductProcessor;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(
                LocalDate.of(2026, 5, 8)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant(),
                ZoneId.systemDefault()
        );

        expirableProductProcessor = new ExpirableProductProcessor(
                productRepository,
                productService,
                fixedClock
        );
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

        expirableProductProcessor.process(product);

        assertEquals(2, product.getAvailable());
        verify(productRepository).save(product);
        verifyNoInteractions(productService);
    }

    @Test
    void shouldHandleExpiredProductWhenExpiryDateIsBeforeToday() {
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

        expirableProductProcessor.process(product);

        assertEquals(3, product.getAvailable());
        verify(productService).handleExpiredProduct(product);
        verify(productRepository, never()).save(product);
    }

    @Test
    void shouldHandleExpiredProductWhenProductIsOutOfStock() {
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

        expirableProductProcessor.process(product);

        assertEquals(0, product.getAvailable());
        verify(productService).handleExpiredProduct(product);
        verify(productRepository, never()).save(product);
    }

    @Test
    void shouldHandleExpiredProductWhenExpiryDateIsToday() {
        Product product = new Product(
                null,
                5,
                3,
                "EXPIRABLE",
                "Cheese",
                LocalDate.of(2026, 5, 8),
                null,
                null
        );

        expirableProductProcessor.process(product);

        assertEquals(3, product.getAvailable());
        verify(productService).handleExpiredProduct(product);
        verify(productRepository, never()).save(product);
    }

    @Test
    void shouldSupportExpirableProduct() {
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

        assertTrue(expirableProductProcessor.supports(product));
    }

    @Test
    void shouldNotSupportNonExpirableProduct() {
        Product product = new Product(
                null,
                5,
                3,
                "NORMAL",
                "USB Cable",
                null,
                null,
                null
        );

        assertFalse(expirableProductProcessor.supports(product));
    }
}