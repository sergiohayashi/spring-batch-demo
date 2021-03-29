package com.example.pubsub;

import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.pubsub.v1.ProjectName;
import com.google.pubsub.v1.Topic;
import java.io.IOException;

//https://cloud.google.com/pubsub/docs/admin#listing_topics
public class ListTopicsExample {
  public static void main(String... args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
//    String projectId = "spring-batch-1616782042728"; //"your-project-id";
    String projectId = "playground--sergio"; //"your-project-id";

    listTopicsExample(projectId);
  }

  public static void listTopicsExample(String projectId) throws IOException {
    try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
      ProjectName projectName = ProjectName.of(projectId);
      for (Topic topic : topicAdminClient.listTopics(projectName).iterateAll()) {
        System.out.println(topic.getName());
      }
      System.out.println("Listed all topics.");
    }
  }
}