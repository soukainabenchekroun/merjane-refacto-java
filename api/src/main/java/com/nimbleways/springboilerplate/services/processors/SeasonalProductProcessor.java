package com.nimbleways.springboilerplate.services.processors;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.ProductProcessor;
import com.nimbleways.springboilerplate.services.ProductService;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

import static com.nimbleways.springboilerplate.domain.ProductTypes.SEASONAL;

/**
 * Processor for seasonal products. If the product is currently in season and available, it decreases the available quantity
 * by 1. If the product is out of season or out of stock, it delegates to the ProductService to handle the seasonal
 * product logic.
 */
@Component
public class SeasonalProductProcessor implements ProductProcessor {

    private final ProductRepository productRepository;
    private final ProductService productService;
    private final Clock clock;

    public SeasonalProductProcessor(
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
        return SEASONAL.equals(product.getType());
    }

    @Override
    public void process(Product product) {
        LocalDate today = LocalDate.now(clock);

        if (today.isAfter(product.getSeasonStartDate())
                && today.isBefore(product.getSeasonEndDate())
                && product.getAvailable() > 0) {
            product.setAvailable(product.getAvailable() - 1);
            productRepository.save(product);
            return;
        }

        productService.handleSeasonalProduct(product);
    }
}