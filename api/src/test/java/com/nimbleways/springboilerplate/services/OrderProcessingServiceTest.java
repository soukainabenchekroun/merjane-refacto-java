package com.nimbleways.springboilerplate.services;

import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@UnitTest
class OrderProcessingServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductProcessor normalProductProcessor;

    @Mock
    private ProductProcessor seasonalProductProcessor;

    @Mock
    private ProductProcessor expirableProductProcessor;

    private OrderProcessingService orderProcessingService;

    @BeforeEach
    void setUp() {
        orderProcessingService = new OrderProcessingService(
                orderRepository,
                List.of(
                        normalProductProcessor,
                        seasonalProductProcessor,
                        expirableProductProcessor
                )
        );
    }

    @Test
    void shouldProcessEachProductWithMatchingProcessor() {
        // GIVEN
        Product normalProduct = new Product(
                null,
                5,
                3,
                "NORMAL",
                "USB Cable",
                null,
                null,
                null
        );

        Product seasonalProduct = new Product(
                null,
                5,
                3,
                "SEASONAL",
                "Watermelon",
                null,
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 6, 1)
        );

        Product expirableProduct = new Product(
                null,
                5,
                3,
                "EXPIRABLE",
                "Butter",
                LocalDate.of(2026, 5, 20),
                null,
                null
        );

        Order order = new Order();
        order.setId(1L);
        order.setItems(Set.of(normalProduct, seasonalProduct, expirableProduct));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        when(normalProductProcessor.supports(any(Product.class)))
                .thenAnswer(invocation -> {
                    Product product = invocation.getArgument(0);
                    return "NORMAL".equals(product.getType());
                });

        when(seasonalProductProcessor.supports(any(Product.class)))
                .thenAnswer(invocation -> {
                    Product product = invocation.getArgument(0);
                    return "SEASONAL".equals(product.getType());
                });

        when(expirableProductProcessor.supports(any(Product.class)))
                .thenAnswer(invocation -> {
                    Product product = invocation.getArgument(0);
                    return "EXPIRABLE".equals(product.getType());
                });

        // WHEN
        ProcessOrderResponse response = orderProcessingService.processOrder(1L);

        // THEN
        assertEquals(1L, response.id());

        verify(orderRepository).findById(1L);
        verify(normalProductProcessor).process(normalProduct);
        verify(seasonalProductProcessor).process(seasonalProduct);
        verify(expirableProductProcessor).process(expirableProduct);
    }

    @Test
    void shouldThrowExceptionWhenOrderDoesNotExist() {
        // GIVEN
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN / THEN
        assertThrows(
                IllegalArgumentException.class,
                () -> orderProcessingService.processOrder(99L)
        );

        verify(orderRepository).findById(99L);
        verifyNoInteractions(normalProductProcessor);
        verifyNoInteractions(seasonalProductProcessor);
        verifyNoInteractions(expirableProductProcessor);
    }

    @Test
    void shouldThrowExceptionWhenNoProcessorSupportsProductType() {
        // GIVEN
        Product unsupportedProduct = new Product(
                null,
                5,
                3,
                "DIGITAL",
                "E-book",
                null,
                null,
                null
        );

        Order order = new Order();
        order.setId(1L);
        order.setItems(Set.of(unsupportedProduct));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        when(normalProductProcessor.supports(unsupportedProduct)).thenReturn(false);
        when(seasonalProductProcessor.supports(unsupportedProduct)).thenReturn(false);
        when(expirableProductProcessor.supports(unsupportedProduct)).thenReturn(false);

        // WHEN / THEN
        assertThrows(
                IllegalArgumentException.class,
                () -> orderProcessingService.processOrder(1L)
        );

        verify(orderRepository).findById(1L);
        verify(normalProductProcessor, never()).process(unsupportedProduct);
        verify(seasonalProductProcessor, never()).process(unsupportedProduct);
        verify(expirableProductProcessor, never()).process(unsupportedProduct);
    }
}