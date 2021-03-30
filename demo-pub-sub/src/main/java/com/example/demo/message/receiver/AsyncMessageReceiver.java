package com.example.demo.message.receiver;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.cloud.gcp.pubsub.integration.AckMode;
import org.springframework.cloud.gcp.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import org.springframework.cloud.gcp.pubsub.support.BasicAcknowledgeablePubsubMessage;
import org.springframework.cloud.gcp.pubsub.support.GcpPubSubHeaders;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.handler.annotation.Header;

//TODO: NÃO FUNCIONA!!!


@Configuration
@EnableIntegration
public class AsyncMessageReceiver {

	
	// Create a message channel for messages arriving from the subscription `sub-one`.
	@Bean
	public MessageChannel inputMessageChannel() {
	  return new PublishSubscribeChannel();
	}

	// Create an inbound channel adapter to listen to the subscription `sub-one` and send
	// messages to the input message channel.
	@Bean
	public PubSubInboundChannelAdapter inboundChannelAdapter(
	    @Qualifier("inputMessageChannel") MessageChannel messageChannel,
	    PubSubTemplate pubSubTemplate) {
	  PubSubInboundChannelAdapter adapter =
//	      new PubSubInboundChannelAdapter(pubSubTemplate, "projects/spring-batch-1616782042728/subscriptions/sub-one");
//	      new PubSubInboundChannelAdapter(pubSubTemplate, "projects/playground--sergio/subscriptions/mysub");
		  new PubSubInboundChannelAdapter(pubSubTemplate, "mysub");
	  adapter.setOutputChannel(messageChannel);
	  adapter.setAckMode(AckMode.MANUAL);
	  adapter.setPayloadType(String.class);
	  return adapter;
	}
	

	// Define what happens to the messages arriving in the message channel.
	@ServiceActivator(inputChannel = "inputMessageChannel")
	public void messageReceiver(
	    String payload,
	    @Header(GcpPubSubHeaders.ORIGINAL_MESSAGE) BasicAcknowledgeablePubsubMessage message) {
	  System.out.println("Message arrived via an inbound channel adapter from sub-one! Payload: " + payload);
	  message.ack();
	}	
	
}
