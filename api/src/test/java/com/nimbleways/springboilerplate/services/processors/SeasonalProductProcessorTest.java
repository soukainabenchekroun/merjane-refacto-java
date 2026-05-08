package com.nimbleways.springboilerplate.services.processors;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.ProductService;
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
class SeasonalProductProcessorTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductService productService;

    private SeasonalProductProcessor seasonalProductProcessor;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(
                LocalDate.of(2026, 5, 8)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant(),
                ZoneId.systemDefault()
        );

        seasonalProductProcessor = new SeasonalProductProcessor(
                productRepository,
                productService,
                fixedClock
        );
    }

    @Test
    void shouldDecreaseAvailableWhenSeasonalProductIsInSeasonAndInStock() {
        Product product = new Product(
                null,
                5,
                3,
                "SEASONAL",
                "Watermelon",
                null,
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 6, 1)
        );

        seasonalProductProcessor.process(product);

        assertEquals(2, product.getAvailable());
        verify(productRepository).save(product);
        verifyNoInteractions(productService);
    }

    @Test
    void shouldHandleSeasonalProductWhenProductIsOutOfStock() {
        Product product = new Product(
                null,
                5,
                0,
                "SEASONAL",
                "Watermelon",
                null,
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 6, 1)
        );

        seasonalProductProcessor.process(product);

        assertEquals(0, product.getAvailable());
        verify(productService).handleSeasonalProduct(product);
        verify(productRepository, never()).save(product);
    }

    @Test
    void shouldHandleSeasonalProductWhenSeasonHasNotStartedYet() {
        Product product = new Product(
                null,
                5,
                3,
                "SEASONAL",
                "Grapes",
                null,
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 7, 1)
        );

        seasonalProductProcessor.process(product);

        assertEquals(3, product.getAvailable());
        verify(productService).handleSeasonalProduct(product);
        verify(productRepository, never()).save(product);
    }

    @Test
    void shouldHandleSeasonalProductWhenSeasonHasEnded() {
        Product product = new Product(
                null,
                5,
                3,
                "SEASONAL",
                "Strawberry",
                null,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 5, 1)
        );

        seasonalProductProcessor.process(product);

        assertEquals(3, product.getAvailable());
        verify(productService).handleSeasonalProduct(product);
        verify(productRepository, never()).save(product);
    }

    @Test
    void shouldHandleSeasonalProductWhenTodayIsSeasonStartDate() {
        Product product = new Product(
                null,
                5,
                3,
                "SEASONAL",
                "Melon",
                null,
                LocalDate.of(2026, 5, 8),
                LocalDate.of(2026, 6, 1)
        );

        seasonalProductProcessor.process(product);

        assertEquals(3, product.getAvailable());
        verify(productService).handleSeasonalProduct(product);
        verify(productRepository, never()).save(product);
    }

    @Test
    void shouldHandleSeasonalProductWhenTodayIsSeasonEndDate() {
        Product product = new Product(
                null,
                5,
                3,
                "SEASONAL",
                "Cherry",
                null,
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 8)
        );

        seasonalProductProcessor.process(product);

        assertEquals(3, product.getAvailable());
        verify(productService).handleSeasonalProduct(product);
        verify(productRepository, never()).save(product);
    }

    @Test
    void shouldSupportSeasonalProduct() {
        Product product = new Product(
                null,
                5,
                3,
                "SEASONAL",
                "Watermelon",
                null,
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 6, 1)
        );

        assertTrue(seasonalProductProcessor.supports(product));
    }

    @Test
    void shouldNotSupportNonSeasonalProduct() {
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

        assertFalse(seasonalProductProcessor.supports(product));
    }
}