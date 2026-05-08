package com.nimbleways.springboilerplate.services.processors;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.ProductProcessor;
import com.nimbleways.springboilerplate.services.ProductService;
import org.springframework.stereotype.Component;

/**
 * Processor for normal products. If the product is available, it decreases the available quantity by 1. If the product
 * is out of stock but has a lead time, it delegates to the ProductService to handle the delay notification logic.
 */
@Component
public class NormalProductProcessor implements ProductProcessor {

    private static final String NORMAL = "NORMAL";

    private final ProductRepository productRepository;
    private final ProductService productService;

    public NormalProductProcessor(ProductRepository productRepository, ProductService productService) {
        this.productRepository = productRepository;
        this.productService = productService;
    }

    @Override
    public boolean supports(Product product) {
        return NORMAL.equals(product.getType());
    }

    @Override
    public void process(Product product) {
        if (product.getAvailable() > 0) {
            product.setAvailable(product.getAvailable() - 1);
            productRepository.save(product);
            return;
        }

        int leadTime = product.getLeadTime();

        if (leadTime > 0) {
            productService.notifyDelay(leadTime, product);
        }
    }
}