cp ..\..\..\..\target\demo-partitioned-pubsub-job-0.0.1-SNAPSHOT.jar .
docker build -t pubsub-partitioner . 
del /f demo-partitioned-pubsub-job-0.0.1-SNAPSHOT.jar