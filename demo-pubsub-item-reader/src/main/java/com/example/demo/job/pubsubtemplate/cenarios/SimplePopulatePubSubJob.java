package com.example.demo.job.pubsubtemplate.cenarios;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.demo.commons.CommonsPubSubStep;

@Configuration
@EnableBatchProcessing		//para injetar JobBuilderFactory e StepBuilderFactory
public class SimplePopulatePubSubJob {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;
	
	@Autowired
	CommonsPubSubStep commonsStep;
	
	@Bean
	public Job simplePopulateJob() {
		return jobBuilderFactory.get("simple-populate")
				.start( commonsStep.stepPopulatePubSubWithTestData())
				.build();
	}
	
}
