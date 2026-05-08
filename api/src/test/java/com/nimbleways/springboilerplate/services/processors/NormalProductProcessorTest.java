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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@UnitTest
class NormalProductProcessorTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductService productService;

    private NormalProductProcessor normalProductProcessor;

    @BeforeEach
    void setUp() {
        normalProductProcessor = new NormalProductProcessor(productRepository, productService);
    }

    @Test
    void shouldDecreaseAvailableWhenNormalProductIsInStock() {
        Product product = new Product(null, 5, 3, "NORMAL", "USB Cable", null, null, null);

        normalProductProcessor.process(product);

        assertEquals(2, product.getAvailable());
        verify(productRepository).save(product);
        verifyNoInteractions(productService);
    }

    @Test
    void shouldNotifyDelayWhenNormalProductIsOutOfStockAndLeadTimeIsPositive() {
        Product product = new Product(null, 5, 0, "NORMAL", "USB Dongle", null, null, null);

        normalProductProcessor.process(product);

        assertEquals(0, product.getAvailable());
        verify(productService).notifyDelay(5, product);
        verify(productRepository, never()).save(product);
    }

    @Test
    void shouldDoNothingWhenNormalProductIsOutOfStockAndLeadTimeIsZero() {
        Product product = new Product(null, 0, 0, "NORMAL", "RJ45 Cable", null, null, null);

        normalProductProcessor.process(product);

        assertEquals(0, product.getAvailable());
        verifyNoInteractions(productService);
        verify(productRepository, never()).save(product);
    }

    @Test
    void shouldSupportNormalProduct() {
        Product product = new Product(null, 5, 3, "NORMAL", "USB Cable", null, null, null);

        assertTrue(normalProductProcessor.supports(product));
    }

    @Test
    void shouldNotSupportNonNormalProduct() {
        Product product = new Product(null, 5, 3, "EXPIRABLE", "Milk", null, null, null);

        assertFalse(normalProductProcessor.supports(product));
    }
}