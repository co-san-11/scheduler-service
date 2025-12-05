package com.cohesity.scheduler.job;

import com.cohesity.scheduler.entity.EmailTask;
import com.cohesity.scheduler.processor.EmailTaskProcessor;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;

import org.springframework.batch.infrastructure.item.database.JpaPagingItemReader;
import org.springframework.batch.infrastructure.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.infrastructure.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class EmailTaskJobConfig {

    private final JobRepository jobRepository;
    private final EntityManagerFactory entityManagerFactory;
    private final EmailTaskProcessor processor;

    @Bean
    public Job emailTaskJob() {
        return new JobBuilder("emailTaskJob", jobRepository)
                .start(emailTaskStep())
                .build();
    }

    @Bean
    public Step emailTaskStep() {

        // Reader: JPA Paging Reader (modern replacement for RepositoryItemReader)
        JpaPagingItemReader<EmailTask> reader = new JpaPagingItemReaderBuilder<EmailTask>()
                .name("emailTaskReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT e FROM EmailTask e")
                .pageSize(10)
                .build();

        // Writer: JPA Item Writer (modern replacement for RepositoryItemWriter)
        var writer = new JpaItemWriterBuilder<EmailTask>()
                .entityManagerFactory(entityManagerFactory)
                .build();

        // TaskExecutor for parallel processing (optional)
        AsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor("emailTaskThread-");

        return new StepBuilder("emailTaskStep", jobRepository)
                .<EmailTask, EmailTask>chunk(5)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                // Fault tolerance: skip and retry
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(10)
                .retry(Exception.class)
                .retryLimit(3)
                // Parallel processing
                .taskExecutor(taskExecutor)
                .build();
    }
}
