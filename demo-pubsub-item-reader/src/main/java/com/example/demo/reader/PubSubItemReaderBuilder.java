package com.example.demo.reader;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.util.Assert;

import com.google.cloud.pubsub.v1.stub.GrpcSubscriberStub;
import com.google.cloud.pubsub.v1.stub.SubscriberStub;
import com.google.cloud.pubsub.v1.stub.SubscriberStubSettings;
import com.google.pubsub.v1.AcknowledgeRequest;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PullRequest;
import com.google.pubsub.v1.PullResponse;
import com.google.pubsub.v1.ReceivedMessage;

//TODO: Deixar genérico. PubSubItemReaderBuilder -> PubSubItemReaderBuilder<T>
//TODO: Ver se é  melhor retornar List<T> ao invés de T
public class PubSubItemReaderBuilder {

	private String subscriptionId;
	private String projectId;

	public PubSubItemReaderBuilder subscriptionId( String subscriptionId) {
		this.subscriptionId= subscriptionId;
		return this;
	}
	
	public PubSubItemReaderBuilder projectId( String projectId) {
		this.projectId= projectId;
		return this;
	}
	

	public ItemReader<String> build()  {
		
		Assert.notNull( projectId, "projectId não configurado");
		Assert.notNull( subscriptionId, "subscriptionId não configurado");
		
		Integer numOfMessages = 1;

		try {
			SubscriberStubSettings subscriberStubSettings = SubscriberStubSettings.newBuilder()
					.setTransportChannelProvider(SubscriberStubSettings.defaultGrpcTransportProviderBuilder()
							.setMaxInboundMessageSize(20 * 1024 * 1024) // 20MB (maximum message size).
							.build())
					.build();
			
			return new ItemReader<String>() {
				
				@Override
				public String read()
						throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
					
					
					try (SubscriberStub subscriber = GrpcSubscriberStub.create(subscriberStubSettings)) {
						String subscriptionName = ProjectSubscriptionName.format(projectId, subscriptionId);
						PullRequest pullRequest = PullRequest.newBuilder().setMaxMessages(numOfMessages)
								.setSubscription(subscriptionName).build();
	
						//TODO: Por enquanto...
						Assert.isTrue( numOfMessages== 1, "Por enquanto lê de 1 em 1..");
						
						// Use pullCallable().futureCall to asynchronously perform this operation.
						PullResponse pullResponse = subscriber.pullCallable().call(pullRequest);
						List<String> ackIds = new ArrayList<>();
						for (ReceivedMessage message : pullResponse.getReceivedMessagesList()) {
	
							// Handle received message
							System.out.println(String.format("[%s] READ: %s", Thread.currentThread().getName(), 
									message.getMessage().getData().toStringUtf8()));
							
							
							ackIds.add(message.getAckId());
						}
						// Acknowledge received messages.
						AcknowledgeRequest acknowledgeRequest = AcknowledgeRequest.newBuilder().setSubscription(subscriptionName)
								.addAllAckIds(ackIds).build();
	
						// Use acknowledgeCallable().futureCall to asynchronously perform this
						// operation.
						if( ackIds.size()> 0) {
							subscriber.acknowledgeCallable().call(acknowledgeRequest);

							//TODO: Mudar para lista..
							return pullResponse.getReceivedMessages(0).getMessage().getData().toStringUtf8();
						} else {
							return null;
						}
					}				
				}
			};
		} catch (Exception e) {
			throw new IllegalStateException( "Unable to initialize pubsub reader", e);
		}
	}
}
