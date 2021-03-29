package com.example.demo.job;

import java.util.concurrent.Future;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.demo.com.example.demo.steps.CommonStepBuilder;
import com.example.demo.com.example.demo.steps.CommonTaskExecutorBuilder;

@Configuration
@EnableBatchProcessing // para injetar JobBuilderFactory e StepBuilderFactory
public class AsyncProcess2JobConfiguration {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;


	@Bean
	public Job asyncProcessor2Job () {
		return jobBuilderFactory.get( "async-process-2")
				.start( asyncStep())
				.build();
		
	}

	private Step asyncStep() {
		return stepBuilderFactory.get( "async-step")
				.<Integer,Future<Integer>>chunk( 2)
				.reader( CommonStepBuilder.reader())
				.processor( asyncProcessor())
				.writer( (asyncWriter())
				)
				.build();
	}

	private ItemWriter<Future<Integer>> asyncWriter() {
		AsyncItemWriter<Integer> asyncItemWriter= new AsyncItemWriter<>();
		asyncItemWriter.setDelegate( CommonStepBuilder.writer());
		return asyncItemWriter;
	}

	private ItemProcessor<Integer, Future<Integer>> asyncProcessor() {
		
		// precisa de spring-batch-integration no pom.xml
		AsyncItemProcessor<Integer, Integer> asyncItemProcessor= new AsyncItemProcessor<Integer,Integer>();
		asyncItemProcessor.setDelegate( CommonStepBuilder.processor());
		asyncItemProcessor.setTaskExecutor( CommonTaskExecutorBuilder.taskExecutorAsync());
		return asyncItemProcessor;
	}
}
