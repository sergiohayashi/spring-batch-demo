package com.example.demo.taskexecutor;

import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

public class CommonTaskExecutorBuilder {

    public static TaskExecutor taskExecutorAsync() {
        return new SimpleAsyncTaskExecutor("spring_batch");
    }

}
