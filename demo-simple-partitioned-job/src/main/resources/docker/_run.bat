cp ..\..\..\..\target\demo-simple-partitioned-job-0.0.1-SNAPSHOT.jar .
docker build -t simple-remote-partitioned-job .
del /f demo-simple-partitioned-job-0.0.1-SNAPSHOT.jar

kubectl delete jobs --all

kubectl apply -f job.yaml

timeout /t 10 /nobreak > NUL

kubectl get job
kubectl get pod
