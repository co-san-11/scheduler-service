package com.cohesity.scheduler.job;

import com.cohesity.scheduler.entity.EmailTask;
import com.cohesity.scheduler.entity.Status;
import com.cohesity.scheduler.processor.EmailTaskProcessor;
import jakarta.transaction.TransactionManager;
import lombok.RequiredArgsConstructor;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class EmailTaskJobConfig {

    private final EmailTaskProcessor processor;




    // -------------------------------
    // JOB
    // -------------------------------
    @Bean(name = "emailTaskJob")
    public Job emailTaskJob(Step emailTaskStep, JobRepository jobRepository) {
        return new JobBuilder("emailTaskJob", jobRepository)
                .listener(new CustomJobExecutionListener())
                .start(emailTaskStep)
                .build();
    }

    // -------------------------------
    // READER
    // -------------------------------
    @Bean
    @StepScope
    public JdbcPagingItemReader<EmailTask> emailTaskReader(DataSource dataSource) throws Exception {
        return new JdbcPagingItemReaderBuilder<EmailTask>()
                .name("emailTaskJdbcPagingReader")
                .dataSource(dataSource)
                .selectClause("SELECT * ")
                .fromClause("FROM email_task")
                .whereClause("WHERE status = :status")
                .parameterValues(Map.of("status", Status.PEND.name()))
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
                SET status = :status,
                    updated_at = :updatedAt
                WHERE id = :id
                  AND status = 'PEND'
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
                              @Qualifier("EmailTaskProcessor")
                                  EmailTaskProcessor processor,
                              @Qualifier("EmailTaskExecutor") AsyncTaskExecutor executor, JobRepository jobRepository, PlatformTransactionManager transactionManager) {

        return new StepBuilder("emailTaskStep", jobRepository)
                .<EmailTask, EmailTask>chunk(5,transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(10)
                .retry(Exception.class)
                .retryLimit(3)
         //       .taskExecutor(executor)
                .transactionManager(transactionManager)
                .build();
    }
}
