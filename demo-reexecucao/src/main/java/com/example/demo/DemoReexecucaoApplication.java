package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoReexecucaoApplication {

	//TODO: Testar reexecução 
	// fonte:  https://stackoverflow.com/questions/61271924/a-job-instance-already-exists-and-is-complete-for-parameters-spring-cloud-task
	
	
	public static void main(String[] args) {
		SpringApplication.run(DemoReexecucaoApplication.class, args);
	}

}
