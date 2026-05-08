package com.nimbleways.springboilerplate.services;

import com.nimbleways.springboilerplate.entities.Product;

public interface ProductProcessor {

    boolean supports(Product product);

    void process(Product product);
}