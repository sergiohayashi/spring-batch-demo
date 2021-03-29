package com.example.demo.commons;

import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

public class CommonTaskExecutorBuilder {

    public static TaskExecutor taskExecutorAsync() {
        return new SimpleAsyncTaskExecutor("spring_batch");
    }

}
