package com.nimbleways.springboilerplate.services;

import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * This service is responsible for processing orders. It retrieves the order from the repository,
 * processes each product in the order using the appropriate processor, and returns a response.
 */
@Service
public class OrderProcessingService {

    private final OrderRepository orderRepository;
    private final List<ProductProcessor> productProcessors;

    public OrderProcessingService(
            OrderRepository orderRepository,
            List<ProductProcessor> productProcessors
    ) {
        this.orderRepository = orderRepository;
        this.productProcessors = productProcessors;
    }

    @Transactional
    public ProcessOrderResponse processOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        for (Product product : order.getItems()) {
            findProcessor(product).process(product);
        }

        return new ProcessOrderResponse(order.getId());
    }

    private ProductProcessor findProcessor(Product product) {
        return productProcessors.stream()
                .filter(processor -> processor.supports(product))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported product type: " + product.getType()));
    }
}