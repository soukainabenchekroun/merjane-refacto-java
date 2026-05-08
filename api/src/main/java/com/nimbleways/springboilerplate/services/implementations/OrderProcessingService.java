package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Set;

@Service
public class OrderProcessingService {

    private final ProductService productService;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public OrderProcessingService(
            ProductService productService,
            ProductRepository productRepository,
            OrderRepository orderRepository
    ) {
        this.productService = productService;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    public ProcessOrderResponse processOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).get();

        Set<Product> products = order.getItems();

        for (Product product : products) {
            processProduct(product);
        }

        return new ProcessOrderResponse(order.getId());
    }

    private void processProduct(Product product) {
        if (product.getType().equals("NORMAL")) {
            processNormalProduct(product);
        } else if (product.getType().equals("SEASONAL")) {
            processSeasonalProduct(product);
        } else if (product.getType().equals("EXPIRABLE")) {
            processExpirableProduct(product);
        }
    }

    private void processNormalProduct(Product product) {
        if (product.getAvailable() > 0) {
            decreaseAvailableQuantity(product);
        } else {
            int leadTime = product.getLeadTime();
            if (leadTime > 0) {
                productService.notifyDelay(leadTime, product);
            }
        }
    }

    private void processSeasonalProduct(Product product) {
        if (LocalDate.now().isAfter(product.getSeasonStartDate())
                && LocalDate.now().isBefore(product.getSeasonEndDate())
                && product.getAvailable() > 0) {
            decreaseAvailableQuantity(product);
        } else {
            productService.handleSeasonalProduct(product);
        }
    }

    private void processExpirableProduct(Product product) {
        if (product.getAvailable() > 0 && product.getExpiryDate().isAfter(LocalDate.now())) {
            decreaseAvailableQuantity(product);
        } else {
            productService.handleExpiredProduct(product);
        }
    }

    private void decreaseAvailableQuantity(Product product) {
        product.setAvailable(product.getAvailable() - 1);
        productRepository.save(product);
    }
}