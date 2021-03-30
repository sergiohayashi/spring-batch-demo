package com.example.demo.commons;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonsPubSubStep {

	@Value("${pubsub.topic}")
	private String topic;

	@Autowired
	PubSubTemplate pubSubTemplate;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	public Step stepPopulatePubSubWithTestData() {

		return this.stepBuilderFactory.get("stepPopulatePubSubWithTestData")
				.tasklet(new Tasklet() {

					@Override
					public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
						for (int i = 0; i < 10; i++) {
							System.out.println( pubSubTemplate.publish(topic, String.format("message=%d ", i)).get());
						}
						return RepeatStatus.FINISHED;
					}
				}).build();
	}
}
