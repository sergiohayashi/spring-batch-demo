package com.example.demo.job.pubsubtemplate.cenarios;

import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gcp.pubsub.support.converter.ConvertedAcknowledgeablePubsubMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.demo.commons.CommonsPubSubStep;
import com.example.demo.reader.PubSubAcknowledgeableItemReader;
import com.example.demo.reader.PubSubTemplateItemReader;

@Configuration
@EnableBatchProcessing // para injetar JobBuilderFactory e StepBuilderFactory
public class ComAckNaEscritaJobConfiguration {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;
	
	@Autowired 
	public PubSubAcknowledgeableItemReader pubsubItemReader;		//<- faz ack na leitura..

	@Autowired
	CommonsPubSubStep commonsStep;
	
	@Bean
	public Job simpleAckOnWriterJob() {
		return jobBuilderFactory.get("simple-ack-on-writer-job")
				.start( commonsStep.stepPopulatePubSubWithTestData())
				.next( step1pubsub())
				.next( commonsStep.stepListAllWithAck())
				.build();
	}

	public Step step1pubsub() {
		return stepBuilderFactory.get("step-1")
				.<ConvertedAcknowledgeablePubsubMessage<String>, ConvertedAcknowledgeablePubsubMessage<String>>chunk(1)
				.reader(reader())
				.writer(writer())
				.build();
	}

	private int i= 0;
	private ItemWriter<ConvertedAcknowledgeablePubsubMessage<String>> writer() {
		return new ItemWriter<ConvertedAcknowledgeablePubsubMessage<String>>() {

			@Override
			public void write(List<? extends ConvertedAcknowledgeablePubsubMessage<String>> items) throws Exception {
				items.forEach( (d) -> {
						System.out.println( String.format("[%s] WRITE: %s", 
										Thread.currentThread().getName(), 
										d));
//						// faz ack
//						// caso não de um nack, depois de tempo flow_control.max_lease_duration deve voltar para a fila..
//						d.nack();	// se eu der um nack em todos, o job vai continuar indefinidamente...
						d.ack();
				});
			}
		};
	}

	private ItemReader<ConvertedAcknowledgeablePubsubMessage<String>> reader() {
		return pubsubItemReader
				.projectId("playground--sergio")
				.subscriptionId("mysub")
				.build();
	}
	
}
