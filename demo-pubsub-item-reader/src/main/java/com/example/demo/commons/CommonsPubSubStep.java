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
	
	@Value("${pubsub.subscriptionId}")
	private String subscriptionId;

	@Autowired
	PubSubTemplate pubSubTemplate;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	public Step stepPopulatePubSubWithTestData() {

		return this.stepBuilderFactory.get("stepPopulatePubSubWithTestData")
				.tasklet(new Tasklet() {

					@Override
					public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
						System.out.println( "Empilha mesagens de testes..");
						for (int i = 0; i < 10; i++) {
							System.out.println( pubSubTemplate.publish(topic, String.format("message=%d ", i)).get());
						}
						System.out.println( "Aguada 5 segundos para garantir....");
						Thread.sleep( 5000);
						return RepeatStatus.FINISHED;
					}
				}).build();
	}

	public Step stepListAllWithAck() {

		return this.stepBuilderFactory.get("stepListAllWithAck")
				.tasklet(new Tasklet() {

					@Override
					public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
						
						System.out.println( "Aguada 5 segundos para garantir....");
						Thread.sleep( 5000);
						System.out.println( "Listando com ack o que restou na fila:");
						while( true) {
							var m= pubSubTemplate.pullAndAck( subscriptionId,  1,  true);
							if( m== null || m.size()<= 0) break;
							System.out.println( String.format( "msg= %s", m.get( 0).getData().toString()));
						}
						System.out.println( "DONE");
						return RepeatStatus.FINISHED;
					}
				}).build();
	}

}
