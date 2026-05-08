package com.nimbleways.springboilerplate.services;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final NotificationService notificationService;
    private final Clock clock;

    public ProductService(
            ProductRepository productRepository,
            NotificationService notificationService,
            Clock clock
    ) {
        this.productRepository = productRepository;
        this.notificationService = notificationService;
        this.clock = clock;
    }

    public void notifyDelay(int leadTime, Product product) {
        product.setLeadTime(leadTime);
        productRepository.save(product);
        notificationService.sendDelayNotification(leadTime, product.getName());
    }

    public void handleSeasonalProduct(Product product) {
        LocalDate today = LocalDate.now(clock);

        if (today.plusDays(product.getLeadTime()).isAfter(product.getSeasonEndDate())) {
            notificationService.sendOutOfStockNotification(product.getName());
            product.setAvailable(0);
            productRepository.save(product);
        } else if (product.getSeasonStartDate().isAfter(today)) {
            notificationService.sendOutOfStockNotification(product.getName());
            productRepository.save(product);
        } else {
            notifyDelay(product.getLeadTime(), product);
        }
    }

    public void handleExpiredProduct(Product product) {
        LocalDate today = LocalDate.now(clock);

        if (product.getAvailable() > 0 && product.getExpiryDate().isAfter(today)) {
            product.setAvailable(product.getAvailable() - 1);
            productRepository.save(product);
        } else {
            notificationService.sendExpirationNotification(product.getName(), product.getExpiryDate());
            product.setAvailable(0);
            productRepository.save(product);
        }
    }
}