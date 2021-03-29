package com.example.demo.job;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.demo.com.example.demo.steps.CommonStepBuilder;
import com.example.demo.com.example.demo.steps.CommonTaskExecutorBuilder;

@Configuration
@EnableBatchProcessing		//para injetar JobBuilderFactory e StepBuilderFactory
public class PartitionamentoLocalJobConfiguration {
	
	
    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job localPartitionJob() {
    	return jobBuilderFactory.get( "local-partition")
    			.start( step1())
    			.build();
    }

	private Step step1() {
		return stepBuilderFactory.get( "step1.manager")
			.<Integer,Integer>partitioner( "step1", partitioner())
			.step( stepWorker())
			.gridSize( 2)
			.taskExecutor( CommonTaskExecutorBuilder.taskExecutorAsync())
			.build();
	}


	private Partitioner partitioner() {
		return new Partitioner() {

			@Override
			public Map<String, ExecutionContext> partition(int gridSize) {
				Map<String, ExecutionContext> result = new HashMap<>();
				
				for( int i= 1; i<= 10; i++) {
					ExecutionContext value = new ExecutionContext();
					result.put( "partition"+i, value);
					value.putInt( "value", i);
				}
				return result;
			}
		};
	}

	private Step stepWorker() {
		return this.stepBuilderFactory.get( "step-worker")
				//TODO: obter via DI para pegar parametro do executionContext!
			.tasklet( workerTask(null))
			.build();
	}

	@Bean
	@StepScope
	public Tasklet workerTask(
			 @Value("#{stepExecutionContext['value']}") Integer value
			
			) {
		
		return CommonStepBuilder.simplePrintTask( value==null?"null":value.toString());
	}
	
}