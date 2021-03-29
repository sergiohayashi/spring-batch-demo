package com.example.demo.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.demo.com.example.demo.steps.CommonStepBuilder;
import com.example.demo.com.example.demo.steps.CommonTaskExecutorBuilder;

@Configuration
@EnableBatchProcessing // para injetar JobBuilderFactory e StepBuilderFactory
public class ParallelStepJobConfiguration {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Bean
	public Job parallelStepJob() {
		return jobBuilderFactory.get("parallel-step")
				.start(splitFlow())
				.next(simpleStep("step-3"))
				.build().build();
	}

	//TODO: Explorar outros cenáris de flow..
	private Flow splitFlow() {
		return new FlowBuilder<SimpleFlow>("split-flow")
				.split(CommonTaskExecutorBuilder.taskExecutorAsync())
				.add(simpleFlow("flow-1"), simpleFlow("flow-2"))
				.build();

	}

	private Flow simpleFlow(String name) {
		return new FlowBuilder<SimpleFlow>(name)
				.start(simpleStep(name))
				.build();
	}

	private Step simpleStep(String name) {
		return this.stepBuilderFactory.get(name)
				.tasklet(CommonStepBuilder.simplePrintTask(name))
				.build();
	}
}
