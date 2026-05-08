package com.nimbleways.springboilerplate.services.processors;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.ProductProcessor;
import com.nimbleways.springboilerplate.services.implementations.ProductService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ExpirableProductProcessor implements ProductProcessor {

    private static final String EXPIRABLE = "EXPIRABLE";

    private final ProductRepository productRepository;
    private final ProductService productService;

    public ExpirableProductProcessor(ProductRepository productRepository, ProductService productService) {
        this.productRepository = productRepository;
        this.productService = productService;
    }

    @Override
    public boolean supports(Product product) {
        return EXPIRABLE.equals(product.getType());
    }

    @Override
    public void process(Product product) {
        if (product.getAvailable() > 0 && product.getExpiryDate().isAfter(LocalDate.now())) {
            product.setAvailable(product.getAvailable() - 1);
            productRepository.save(product);
            return;
        }

        productService.handleExpiredProduct(product);
    }
}