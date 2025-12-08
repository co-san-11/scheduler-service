package com.cohesity.scheduler.scheduler;

import lombok.extern.slf4j.Slf4j;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;

@Component
@Slf4j
public class EmailTaskScheduler {

    private final JobLauncher jobLauncher;
    private final Job emailTaskJob;

    EmailTaskScheduler(JobLauncher jobLauncher, @Qualifier("emailTaskJob") Job emailTaskJob) {
        this.jobLauncher = jobLauncher;
        this.emailTaskJob = emailTaskJob;
    }

    /**
     * Scheduled to run every 60 seconds (configurable via application.properties)
     * Each execution uses unique non-identifying parameters to allow re-execution
     * 
     * JobLauncher is the standard approach for launching Spring Batch jobs
     * with non-identifying job parameters.
     */
    @Scheduled(fixedRateString = "${scheduler.email-task-rate:60000}", initialDelay = 60000)
    public void runEmailTaskJob() {
        try {
            log.info("Starting Email Task Job");
            
            // Create unique parameters with non-identifying flags (false parameter)
            // This allows multiple executions without JobInstanceAlreadyCompleteException
            var jobParameters = new JobParametersBuilder()
                    .addLong("emailTaskJob.run.id", System.nanoTime(), false)
                    .addLong("timestamp", System.currentTimeMillis(), false)
                    .toJobParameters();

            log.debug("Email Job Parameters: {}", jobParameters);

            JobExecution execution = jobLauncher.run(emailTaskJob, jobParameters);
            log.info("Email Task Job completed with status: {}", execution.getStatus());
        } catch (Exception e) {
            log.error("Error executing Email Task Job", e);
        }
    }
}