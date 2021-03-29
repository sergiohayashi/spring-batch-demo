package com.example.demo.job;

import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.demo.reader.PubSubItemReaderBuilder;

@Configuration
@EnableBatchProcessing // para injetar JobBuilderFactory e StepBuilderFactory
public class SimplePubsubJobConfiguration {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Bean
	public Job simplePubsubSteps() {
		return jobBuilderFactory.get("simple-pubsub").start(step1ubsub()).build();
	}

	@Bean
	public Step step1ubsub() {
		return stepBuilderFactory.get("step-1")
				.<String, String>chunk(1)
				.reader(reader())
				.writer(writer())
				.build();
	}

	private ItemWriter<String> writer() {
		return new ItemWriter<String>() {

			@Override
			public void write(List<? extends String> items) throws Exception {
				items.forEach(
						d -> System.out.println(String.format("[%s] WRITE: %s", Thread.currentThread().getName(), d)));
			}
		};
	}

	private ItemReader<String> reader() {
		return new PubSubItemReaderBuilder()
				.projectId("playground--sergio")
				.subscriptionId("mysub")
				.build();
	}

}
