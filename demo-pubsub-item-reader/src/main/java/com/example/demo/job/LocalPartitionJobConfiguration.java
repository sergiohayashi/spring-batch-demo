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
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.demo.commons.CommonStepBuilder;
import com.example.demo.commons.CommonTaskExecutorBuilder;
import com.example.demo.reader.PubSubItemReaderBuilder;

@Configuration
@EnableBatchProcessing		//para injetar JobBuilderFactory e StepBuilderFactory
public class LocalPartitionJobConfiguration {
	
	
    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job localPartitionJob() {
    	return jobBuilderFactory.get( "pubsub-local-partition")
    			.start( step1())
    			.build();
    }

	private Step step1() {
		return stepBuilderFactory.get( "step1.manager")
			.partitioner( "step1", partitioner())
			.step( stepWorker())	// step particionado
			.gridSize( 2)
			.taskExecutor( CommonTaskExecutorBuilder.taskExecutorAsync())
			.build();
	}


	private Partitioner partitioner() {
		return new Partitioner() {

			@Override
			public Map<String, ExecutionContext> partition(int gridSize) {
				Map<String, ExecutionContext> result = new HashMap<>();
				
				for( int i= 1; i<= 2; i++) {
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
			.<String,String>chunk(1)
			.reader( pubsubItemReader(null))
			.writer( CommonStepBuilder.writer())
			.build();
	}

	@Bean
	@StepScope
	public ItemReader<String> pubsubItemReader(
		 @Value("#{stepExecutionContext['value']}") Integer value
			
		) {
		System.out.println( String.format( "partition value= %d", value));
		
		return new PubSubItemReaderBuilder()
				.projectId("playground--sergio")
				.subscriptionId("mysub")
				.build();
	}
	
}