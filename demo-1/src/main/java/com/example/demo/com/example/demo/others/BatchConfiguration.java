//package com.example.demo.com.example.demo.others;
//
//import java.util.concurrent.Future;
//import java.util.concurrent.ThreadPoolExecutor;
//
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.JobParametersBuilder;
//import org.springframework.batch.core.JobParametersInvalidException;
//import org.springframework.batch.core.Step;
//import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
//import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
//import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
//import org.springframework.batch.core.launch.JobLauncher;
//import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
//import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
//import org.springframework.batch.core.repository.JobRestartException;
//import org.springframework.batch.integration.async.AsyncItemProcessor;
//import org.springframework.batch.integration.async.AsyncItemWriter;
//import org.springframework.batch.item.ItemProcessor;
//import org.springframework.batch.item.ItemReader;
//import org.springframework.batch.item.ItemWriter;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.task.TaskExecutor;
//import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//
//import com.example.demo.job.TestDTO;
//
//@Configuration
//@EnableScheduling
//@EnableBatchProcessing
//public class BatchConfiguration {
//
//    @Value("${async.thread.max.pool}")
//    private Integer maxPoolSize;
//
//    @Value("${async.thread.core.pool}")
//    private Integer corePoolSize;
//
//    @Value("${async.thread.queue}")
//    private Integer queueSize;
//
//    @Autowired
//    private JobBuilderFactory jobs;
//
//    @Autowired
//    private StepBuilderFactory steps;
//
//    @Autowired
//    private JobLauncher jobLauncher;
//
//    @Bean
//    public ItemReader<String> reader(){
//        return new TestItemReader();
//    }
//
//    @Bean
//    public ItemProcessor<String, TestDTO> processor(){
//        return new TestItemProcessor();
//    }
//
//    @Bean
//    public ItemWriter<TestDTO> writer(){
//        return new TestItemWriter();
//    }
//
//    @Bean
//    public ItemProcessor<String, Future<TestDTO>> asyncItemProcessor(){
//        AsyncItemProcessor<String, TestDTO> asyncItemProcessor = new AsyncItemProcessor<>();
//        asyncItemProcessor.setDelegate(processor());
//        asyncItemProcessor.setTaskExecutor(getAsyncExecutor());
//        return asyncItemProcessor;
//    }
//
//    @Bean
//    public ItemWriter<Future<TestDTO>> asyncItemWriter(){
//        AsyncItemWriter<TestDTO> asyncItemWriter = new AsyncItemWriter<>();
//        asyncItemWriter.setDelegate(writer());
//        return asyncItemWriter;
//    }
//
//    @Bean(name = "asyncExecutor")
//    public TaskExecutor getAsyncExecutor()
//    {
//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//        executor.setCorePoolSize(corePoolSize);
//        executor.setMaxPoolSize(corePoolSize);
//        executor.setQueueCapacity(queueSize);
//        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
//        executor.setThreadNamePrefix("AsyncExecutor-");
//        return executor;
//    }
//
//    @Bean
//    protected Step step1(){
//        return this.steps.get("step1")
//                .<String, Future<TestDTO>> chunk(corePoolSize)
//                .reader(reader())
//                .processor(asyncItemProcessor())
//                .writer(asyncItemWriter())
//                .build();
//    }
//
//    @Bean
//    protected Job job1(){
//        return this.jobs.get("job1").start(step1()).build();
//    }
//
//    @Scheduled(cron = "0 * * * * *", zone =  "America/Sao_Paulo")
//    public void schedule() throws JobParametersInvalidException, JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
//
//        jobLauncher.run(job1(), new JobParametersBuilder().addLong("time", System.currentTimeMillis()).toJobParameters());
//    }
//}