package com.example.demo.job;

import java.util.Arrays;
import java.util.List;

import org.springframework.batch.core.ItemProcessListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Metrics;

//fonte: https://stackoverflow.com/questions/64869472/spring-batch-and-boot-micrometer
@Configuration
@EnableBatchProcessing
public class SimpleJobConfiguration {

	
	@Bean
    public ItemReader<Integer> itemReader() {
        return new ListItemReader<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
    }

    @Bean
    public ItemProcessor<Integer, Integer> itemProcessor() {
        return item -> {
            System.out.println("processing item " + item);
            Thread.sleep(2000);
            return item + 1;
        };
    }

    @Bean
    public ItemWriter<Integer> itemWriter() {
        return items -> {
            for (Integer item : items) {
                System.out.println("writing item = " + item);
            }
        };
    }

    @Bean
    public Job job(JobBuilderFactory jobs, StepBuilderFactory steps) {
        return jobs.get("job")
                .start(steps.get("step")
                        .<Integer, Integer>chunk(5)
                        .reader(itemReader())
                        .processor(itemProcessor())
                        .writer(itemWriter())
                        .listener(new MonitoringItemProcessListener())
                        .build())
                .build();
    }
    
    static class MonitoringItemProcessListener implements ItemProcessListener<Integer, Integer> {

        @Override
        public void beforeProcess(Integer item) {
            
        }

        @Override
        public void afterProcess(Integer item, Integer result) {
            List<Meter> meters = Metrics.globalRegistry.getMeters();
            for (Meter meter : meters) {
                if (meter.getId().getName().equals("spring.batch.item.process")) {
                    System.out.println("meter description = " + meter.getId().getDescription());
                    Iterable<Measurement> measurements = meter.measure();
                    for (Measurement measurement : measurements) {
                        System.out.println("measurement: statistic = " + measurement.getStatistic() + " | value = " + measurement.getValue());
                    }
                }
            }
        }

        @Override
        public void onProcessError(Integer item, Exception e) {

        }
    }

//    public static void main(String[] args) throws Exception {
//        Metrics.addRegistry(new SimpleMeterRegistry());
//        ApplicationContext context = new AnnotationConfigApplicationContext(SimpleJobConfiguration.class);
//        JobLauncher jobLauncher = context.getBean(JobLauncher.class);
//        Job job = context.getBean(Job.class);
//        jobLauncher.run(job, new JobParameters());
//    }
}
