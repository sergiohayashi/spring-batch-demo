package com.example.pubsub;

import com.google.cloud.pubsub.v1.stub.GrpcSubscriberStub;
import com.google.cloud.pubsub.v1.stub.SubscriberStub;
import com.google.cloud.pubsub.v1.stub.SubscriberStubSettings;
import com.google.pubsub.v1.AcknowledgeRequest;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PullRequest;
import com.google.pubsub.v1.PullResponse;
import com.google.pubsub.v1.ReceivedMessage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SubscribeSyncExample {
	public static void main(String... args) throws Exception {
		// TODO(developer): Replace these variables before running the sample.
		String subscriptionId = "mysub"; // "your-subscription-id";
		String projectId = "playground--sergio"; // "your-project-id";

//    String projectId = "your-project-id";
//    String subscriptionId = "your-subscription-id";
		Integer numOfMessages = 2; // 10;

		subscribeSyncExample(projectId, subscriptionId, numOfMessages);
	}

	public static void subscribeSyncExample(String projectId, String subscriptionId, Integer numOfMessages)
			throws IOException {
		SubscriberStubSettings subscriberStubSettings = SubscriberStubSettings.newBuilder()
				.setTransportChannelProvider(SubscriberStubSettings.defaultGrpcTransportProviderBuilder()
						.setMaxInboundMessageSize(20 * 1024 * 1024) // 20MB (maximum message size).
						.build())
				.build();

		try (SubscriberStub subscriber = GrpcSubscriberStub.create(subscriberStubSettings)) {
			String subscriptionName = ProjectSubscriptionName.format(projectId, subscriptionId);
			PullRequest pullRequest = PullRequest.newBuilder().setMaxMessages(numOfMessages)
					.setSubscription(subscriptionName).build();

			// Use pullCallable().futureCall to asynchronously perform this operation.
			PullResponse pullResponse = subscriber.pullCallable().call(pullRequest);
			List<String> ackIds = new ArrayList<>();
			for (ReceivedMessage message : pullResponse.getReceivedMessagesList()) {

				System.out.println("AckId:" + message.getAckId());
				System.out.println("Data: " + message.getMessage().getData().toStringUtf8());
				System.out.println("Message Id: " + message.getMessage().getMessageId());

				// Handle received message
				// ...
				ackIds.add(message.getAckId());
			}
			// Acknowledge received messages.
			AcknowledgeRequest acknowledgeRequest = AcknowledgeRequest.newBuilder().setSubscription(subscriptionName)
					.addAllAckIds(ackIds).build();

			// Use acknowledgeCallable().futureCall to asynchronously perform this
			// operation.
			subscriber.acknowledgeCallable().call(acknowledgeRequest);
			System.out.println(pullResponse.getReceivedMessagesList());
		}
	}
}