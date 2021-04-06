package com.example.demo;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.task.configuration.EnableTask;

@EnableTask	//<- cloud task
@SpringBootApplication
@EnableBatchProcessing
public class DemoSimplePartitionedJobApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoSimplePartitionedJobApplication.class, args);
	}

}
