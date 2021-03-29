package com.example.demo.job;

import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.example.demo.com.example.demo.steps.CommonStepBuilder;

@Configuration
@EnableBatchProcessing		//para injetar JobBuilderFactory e StepBuilderFactory
public class StepMultithread {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job jobStepMultithread() {
    	return jobBuilderFactory.get( "step-multithread")
    			.start( step1( taskExecutor()))
    			.build();
    }
    
    public TaskExecutor taskExecutorAsync() {
        return new SimpleAsyncTaskExecutor("spring_batch");
    }

	private TaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor executor= new ThreadPoolTaskExecutor();
		executor.setMaxPoolSize( 2);
		executor.setCorePoolSize( 2);
		executor.setQueueCapacity(2);
		executor.setRejectedExecutionHandler( new ThreadPoolExecutor.CallerRunsPolicy());
		executor.setThreadNamePrefix( "TaskExecutor-");

		// por alguma razão tem que ter este afterPropertiesSet(), senão não funciona..
		// fonte: https://stackoverflow.com/questions/48065089/thread-executor-doesnt-work-spring-batch
		executor.afterPropertiesSet();
		return executor;
	}

	private Step step1(TaskExecutor taskExecutor) {
		return this.stepBuilderFactory.get( "step multithread")
				.<Integer, Integer>chunk( 2)
				.reader( CommonStepBuilder.synchronizedReader())
				.writer( CommonStepBuilder.writer())
				.taskExecutor( taskExecutorAsync())	// OK, funciona, finaliza processo
//				.taskExecutor( taskExecutor())		// OK, funciona, mas não termina o processo...
				.build();
	}

	
}
