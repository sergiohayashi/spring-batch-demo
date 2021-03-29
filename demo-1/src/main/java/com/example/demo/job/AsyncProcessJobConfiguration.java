package com.example.demo.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
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
public class AsyncProcessJobConfiguration {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;


	@Bean
	public Job asyncProcessorJob () {
		return jobBuilderFactory.get( "async-process")
				.start( asyncStep())
				.build();
		
	}

	@SuppressWarnings("unchecked")
	private Step asyncStep() {
		return ((SimpleStepBuilder<Integer,Integer>)stepBuilderFactory.get( "async-step")
				.<Integer,Integer>chunk( 2)
				.reader( CommonStepBuilder.reader())
				//TODO: tem um jeito de elimnitar este warning..?
				.processor( (ItemProcessor)asyncProcessor())
				.writer( (ItemWriter)asyncWriter())
				)
				.build();
	}

	private AsyncItemWriter<Integer> asyncWriter() {
		AsyncItemWriter<Integer> asyncItemWriter= new AsyncItemWriter<>();
		asyncItemWriter.setDelegate( CommonStepBuilder.writer());
		return asyncItemWriter;
	}

	private AsyncItemProcessor<Integer, Integer> asyncProcessor() {
		
		// precisa de spring-batch-integration no pom.xml
		AsyncItemProcessor<Integer, Integer> asyncItemProcessor= new AsyncItemProcessor<Integer,Integer>();
		asyncItemProcessor.setDelegate( CommonStepBuilder.processor());
		asyncItemProcessor.setTaskExecutor( CommonTaskExecutorBuilder.taskExecutorAsync());
		return asyncItemProcessor;
	}
}
