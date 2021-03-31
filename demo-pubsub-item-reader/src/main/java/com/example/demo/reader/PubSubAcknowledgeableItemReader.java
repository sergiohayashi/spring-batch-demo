package com.example.demo.reader;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.cloud.gcp.pubsub.support.converter.ConvertedAcknowledgeablePubsubMessage;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

@Configuration
public class PubSubAcknowledgeableItemReader {

	@Autowired 
	PubSubTemplate pubSubTemplate;
	
	private String subscriptionId;
	private String projectId;

	public PubSubAcknowledgeableItemReader subscriptionId( String subscriptionId) {
		this.subscriptionId= subscriptionId;
		return this;
	}
	
	public PubSubAcknowledgeableItemReader projectId( String projectId) {
		this.projectId= projectId;
		return this;
	}
	

	public ItemReader<ConvertedAcknowledgeablePubsubMessage<String>> build()  {
		Assert.notNull( projectId, "projectId não configurado");
		Assert.notNull( subscriptionId, "subscriptionId não configurado");
		
		Integer numOfMessages = 1;
	
		 try {
			return new ItemReader<ConvertedAcknowledgeablePubsubMessage<String>>() {
	
				@Override
				public ConvertedAcknowledgeablePubsubMessage<String> read()
						throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
					
					//TODO: Aqui funciona porque é String, mas se fosse uma classe teria que informar o 
					// 	converter para o PubSubTemplate
					var ret= pubSubTemplate.pullAndConvert(subscriptionId, numOfMessages, true, String.class);
					if( ret.size()<= 0) return null;
					
					//TODO: assumindo que pega de 1 em e por enquanto...
					System.out.println(String.format("[%s] READ: %s", Thread.currentThread().getName(), 
							ret.get(0).getPayload()));
					return ret.get(0);
				}
			};
		} catch (Exception e) {
			throw new IllegalStateException( "Unable to initialize pubsub reader", e);
		}
	}
	
}
