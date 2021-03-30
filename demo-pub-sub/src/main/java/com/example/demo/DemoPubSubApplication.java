package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// ref: https://cloud.google.com/pubsub/docs/spring

@SpringBootApplication
public class DemoPubSubApplication {

	public static void main(String[] args)  {
		SpringApplication.run(DemoPubSubApplication.class, args);
		
		
		try {
			Thread.sleep( 20000);
		} catch (InterruptedException e) {}
	}

}
