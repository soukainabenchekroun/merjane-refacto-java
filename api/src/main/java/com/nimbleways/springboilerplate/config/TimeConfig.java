package com.nimbleways.springboilerplate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * This configuration class provides a Clock bean that can be used throughout the application.
 * By using a Clock bean, we can easily mock the current time in our tests, allowing us to
 * simulate different scenarios such as order processing delays or expiration notifications.
 */
@Configuration
public class TimeConfig {

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}