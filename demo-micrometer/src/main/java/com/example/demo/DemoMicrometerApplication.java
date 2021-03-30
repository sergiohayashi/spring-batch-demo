package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@SpringBootApplication
public class DemoMicrometerApplication {

	public static void main(String[] args) {
        Metrics.addRegistry(new SimpleMeterRegistry());

		SpringApplication.run(DemoMicrometerApplication.class, args);
	}

}
