package com.examenc5.cti;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CtiBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(CtiBackendApplication.class, args);
	}

}
