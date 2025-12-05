package com.cohesity.scheduler.job;

import com.cohesity.scheduler.entity.EmailTask;
import com.cohesity.scheduler.processor.EmailTaskProcessor;
import lombok.RequiredArgsConstructor;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.JdbcPagingItemReader;
import org.springframework.batch.infrastructure.item.database.Order;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.infrastructure.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class EmailTaskJobConfig {

    private final JobRepository jobRepository;
    private final EmailTaskProcessor processor;

    // -------------------------------
    // JOB
    // -------------------------------
    @Bean
    public Job emailTaskJob(Step emailTaskStep) {
        return new JobBuilder("emailTaskJob", jobRepository)
                .start(emailTaskStep)
                .build();
    }

    // -------------------------------
    // READER
    // -------------------------------
    @Bean
    public JdbcPagingItemReader<EmailTask> emailTaskReader(DataSource dataSource) throws Exception {
        return new JdbcPagingItemReaderBuilder<EmailTask>()
                .name("emailTaskJdbcPagingReader")
                .dataSource(dataSource)
                .selectClause("SELECT id, name, status, updated_at")
                .fromClause("FROM email_task")
                .whereClause("WHERE status = :status")
                .parameterValues(Map.of("status", "PEND"))
                .sortKeys(Map.of("id", Order.ASCENDING))   // required for paging
                .rowMapper(new BeanPropertyRowMapper<>(EmailTask.class))
                .pageSize(10)
                .build();
    }

    // -------------------------------
    // WRITER
    // -------------------------------
    @Bean
    public JdbcBatchItemWriter<EmailTask> emailTaskWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<EmailTask>()
                .dataSource(dataSource)
                .beanMapped()
                .sql("""
                        UPDATE email_task 
                        SET status = :status, updated_at = :updatedAt 
                        WHERE id = :id
                        """)
                .build();
    }

    // -------------------------------
    // TASK EXECUTOR
    // -------------------------------
    @Bean
    @Qualifier("EmailTaskExecutor")
    public AsyncTaskExecutor emailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("emailTaskThread-");
        executor.setCorePoolSize(5);   // max 5 parallel threads
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(0);  // force parallel execution, no queueing
        executor.initialize();
        return executor;
    }

    // -------------------------------
    // STEP
    // -------------------------------
    @Bean
    public Step emailTaskStep(JdbcPagingItemReader<EmailTask> reader,
                              JdbcBatchItemWriter<EmailTask> writer,
                              EmailTaskProcessor processor,
                              @Qualifier("EmailTaskExecutor") AsyncTaskExecutor executor) {

        return new StepBuilder("emailTaskStep", jobRepository)
                .<EmailTask, EmailTask>chunk(5)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(10)
                .retry(Exception.class)
                .retryLimit(3)
                .taskExecutor(executor)   // enables parallel chunk processing
                .build();
    }
}
