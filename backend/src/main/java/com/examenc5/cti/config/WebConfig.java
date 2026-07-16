package com.examenc5.cti.config;

import java.util.Arrays;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	private final CtiProperties properties;

	public WebConfig(CtiProperties properties) {
		this.properties = properties;
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		String[] origins = Arrays.stream(properties.allowedOrigins().split(","))
				.map(String::trim)
				.filter(origin -> !origin.isBlank())
				.toArray(String[]::new);

		registry.addMapping("/**")
				.allowedOrigins(origins)
				.allowedMethods("GET", "OPTIONS")
				.allowedHeaders("*");
	}
}
