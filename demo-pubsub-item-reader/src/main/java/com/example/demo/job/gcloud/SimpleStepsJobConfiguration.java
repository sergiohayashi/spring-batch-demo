package com.example.demo.job.gcloud;

import java.util.Arrays;
import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing // para injetar JobBuilderFactory e StepBuilderFactory
public class SimpleStepsJobConfiguration {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Bean
	public Job simpleSteps() {
		return jobBuilderFactory.get("simple-steps").start(step1()).build();
	}

	@Bean
	public Step step1() {
		return stepBuilderFactory.get("step-1").<String, String>chunk(10).reader(reader()).writer(writer()).build();
	}

	public ItemWriter<String> writer() {
		return new ItemWriter<String>() {

			@Override
			public void write(List<? extends String> items) throws Exception {
				items.forEach(
						d -> System.out.println(String.format("%s write: %s", Thread.currentThread().getName(), d)));
			}
		};
	}

	public ItemReader<String> reader() {
		List<Integer> values = Arrays.asList(1, 2, 3, 4, 5);
		int j[] = new int[] { 0 };

		return new ItemReader<String>() {

			@Override
			public String read()
					throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
				if (j[0] >= values.size())
					return null;

				String ret = values.get(j[0]++).toString();
				System.out.println(String.format("read: %s", ret));
				return ret;
			}
		};
	}

}
