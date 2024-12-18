package dev.stiebo.app.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DefaultConfiguration {
    @Bean
    public Long defaultMaxAllowed() {
        return 750L;
    }

    @Bean
    public Long defaultMaxManual() {
        return 1500L;
    }
}
