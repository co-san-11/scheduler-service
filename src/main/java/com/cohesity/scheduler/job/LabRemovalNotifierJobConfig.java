package com.cohesity.scheduler.job;

import com.cohesity.scheduler.entity.LabRemovalNotifierTask;
import com.cohesity.scheduler.processor.LabRemovalNotifierProcessor;
import lombok.RequiredArgsConstructor;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
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
public class LabRemovalNotifierJobConfig {

    private final LabRemovalNotifierProcessor processor;

    // -------------------------------
    // JOB
    // -------------------------------
    @Bean(name = "labRemovalNotifierJob")
    public Job labRemovalNotifierJob(Step labRemovalNotifierStep, JobRepository jobRepository) {
        return new JobBuilder("labRemovalNotifierJob", jobRepository)
                .listener(new CustomJobExecutionListener())
                .start(labRemovalNotifierStep)
                .build();
    }

    // -------------------------------
    // READER
    // -------------------------------
    @Bean
    public JdbcPagingItemReader<LabRemovalNotifierTask> labRemovalNotifierReader(DataSource dataSource) throws Exception {
        return new JdbcPagingItemReaderBuilder<LabRemovalNotifierTask>()
                .name("labRemovalNotifierJdbcPagingReader")
                .dataSource(dataSource)
                .selectClause("SELECT id, name, status, updated_at")
                .fromClause("FROM lab_removal_notifier_task")
                .whereClause("WHERE status = :status")
                .parameterValues(Map.of("status", "PEND"))
                .sortKeys(Map.of("id", Order.ASCENDING))   // required for paging
                .rowMapper(new BeanPropertyRowMapper<>(LabRemovalNotifierTask.class))
                .pageSize(10)
                .build();
    }

    // -------------------------------
    // WRITER
    // -------------------------------
    @Bean
    public JdbcBatchItemWriter<LabRemovalNotifierTask> labRemovalNotifierWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<LabRemovalNotifierTask>()
                .dataSource(dataSource)
                .beanMapped()
                .sql("""
                        UPDATE lab_removal_notifier_task 
                        SET status = :status, updated_at = :updatedAt 
                        WHERE id = :id
                        """)
                .build();
    }

    // -------------------------------
    // TASK EXECUTOR
    // -------------------------------
    @Bean
    @Qualifier("LabRemovalNotifierExecutor")
    public AsyncTaskExecutor labRemovalNotifierExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("labRemovalNotifierThread-");
        executor.setCorePoolSize(3);   // max 3 parallel threads
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(0);  // force parallel execution, no queueing
        executor.initialize();
        return executor;
    }

    // -------------------------------
    // STEP
    // -------------------------------
    @Bean
    public Step labRemovalNotifierStep(JdbcPagingItemReader<LabRemovalNotifierTask> reader,
                                       JdbcBatchItemWriter<LabRemovalNotifierTask> writer,
                                       @Qualifier("LabRemovalNotifierProcessor")
                                           LabRemovalNotifierProcessor processor,
                                       @Qualifier("LabRemovalNotifierExecutor") AsyncTaskExecutor executor,
                                       JobRepository jobRepository,
                                       PlatformTransactionManager transactionManager) {

        return new StepBuilder("labRemovalNotifierStep", jobRepository)
                .<LabRemovalNotifierTask, LabRemovalNotifierTask>chunk(5)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(10)
                .retry(Exception.class)
                .retryLimit(3)
                .taskExecutor(executor)
                .transactionManager(transactionManager)
                .build();
    }
}
