package com.gudrhs8304.ticketory.core.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SpaForwardingConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/kakao").setViewName("forward:/index.html");
        registry.addViewController("/success").setViewName("forward:/index.html");
        registry.addViewController("/fail").setViewName("forward:/index.html");
    }
}