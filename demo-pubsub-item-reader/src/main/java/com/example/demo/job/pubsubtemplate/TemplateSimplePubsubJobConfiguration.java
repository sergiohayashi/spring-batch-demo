package com.example.demo.job.pubsubtemplate;

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
import com.example.demo.reader.PubSubTemplateItemReader;

@Configuration
@EnableBatchProcessing // para injetar JobBuilderFactory e StepBuilderFactory
public class TemplateSimplePubsubJobConfiguration {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;
	
	@Autowired 
	public PubSubTemplateItemReader pubsubTemplateItemReader;

	@Bean
	public Job simpleTemplatePubsubSteps() {
		return jobBuilderFactory.get("simple-template-pubsub")
				.start(step1pubsub())
				.build();
	}

	public Step step1pubsub() {
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
		return pubsubTemplateItemReader
				.projectId("playground--sergio")
				.subscriptionId("mysub")
				.build();
	}

}
