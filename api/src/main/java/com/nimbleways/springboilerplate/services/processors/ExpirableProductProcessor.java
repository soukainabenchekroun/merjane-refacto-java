package com.nimbleways.springboilerplate.services.processors;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.ProductProcessor;
import com.nimbleways.springboilerplate.services.ProductService;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

@Component
public class ExpirableProductProcessor implements ProductProcessor {

    private static final String EXPIRABLE = "EXPIRABLE";

    private final ProductRepository productRepository;
    private final ProductService productService;
    private final Clock clock;

    public ExpirableProductProcessor(
            ProductRepository productRepository,
            ProductService productService,
            Clock clock
    ) {
        this.productRepository = productRepository;
        this.productService = productService;
        this.clock = clock;
    }

    @Override
    public boolean supports(Product product) {
        return EXPIRABLE.equals(product.getType());
    }

    @Override
    public void process(Product product) {
        LocalDate today = LocalDate.now(clock);

        if (product.getAvailable() > 0 && product.getExpiryDate().isAfter(today)) {
            product.setAvailable(product.getAvailable() - 1);
            productRepository.save(product);
            return;
        }

        productService.handleExpiredProduct(product);
    }
}