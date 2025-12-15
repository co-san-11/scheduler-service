package com.cohesity.scheduler.scheduler;

import lombok.extern.slf4j.Slf4j;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;

import java.util.Properties;

@Component
@Slf4j
public class EmailTaskScheduler {

    private final JobOperator jobOperator;
    private final Job emailTaskJob;
    private final JobLauncher jobLauncher;

    EmailTaskScheduler(JobOperator jobOperator, @Qualifier("emailTaskJob") Job emailTaskJob, JobLauncher jobLauncher){

        this.jobOperator = jobOperator;
        this.emailTaskJob = emailTaskJob;
        this.jobLauncher = jobLauncher;
    }

    /**
     * Scheduled to run every 60 seconds (configurable via application.properties)
     * Each execution uses unique non-identifying parameters to allow re-execution
     * 
     * JobLauncher is marked deprecated but is still the standard approach for Spring Batch 6.0+
     * when using non-identifying job parameters. This is the recommended pattern.
     */
    @Scheduled(fixedRateString = "${scheduler.email-task-rate:60000}", initialDelay = 60000)
    public void runEmailTaskJob() {
        try {
            log.info("Starting Email Task Job");
            
            // Create unique parameters with non-identifying flags (false parameter)
            // This allows multiple executions without JobInstanceAlreadyCompleteException
            var jobParameters = new JobParametersBuilder()
                    .addLong("run.id", System.currentTimeMillis())   // identifying = true (default)
                    .addLong("timestamp", System.currentTimeMillis(), false) // optional non-identifying
                    .toJobParameters();
            Properties properties = new Properties();
            properties.put("emailTaskJob.run.id",String.valueOf( System.nanoTime()));
            properties.put("timestamp", String.valueOf(System.currentTimeMillis()));

          //  Long execution = jobOperator.start(emailTaskJob.getName(), properties);
            JobExecution jobExecution = jobLauncher.run(emailTaskJob, jobParameters);
            log.info("Email Task Job completed with status: {}", jobExecution.getStatus());
        } catch (Exception e) {
            log.error("Error executing Email Task Job", e);
        }
    }
}