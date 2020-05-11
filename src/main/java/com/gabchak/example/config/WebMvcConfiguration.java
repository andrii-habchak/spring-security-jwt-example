package com.gabchak.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.reactive.config.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer  {

  public static final String API_PREFIX = "/api/v1";

  /**
   * Sets prefix '/api/v1' to all rest controllers.
   */
  public void configurePathMatch(PathMatchConfigurer configurer) {
    configurer.addPathPrefix(API_PREFIX,
        HandlerTypePredicate.forAnnotation(RestController.class));
  }
}