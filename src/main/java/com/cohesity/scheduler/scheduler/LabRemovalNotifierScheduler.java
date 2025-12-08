package com.cohesity.scheduler.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Properties;

@Component
@Slf4j
public class LabRemovalNotifierScheduler {

    private final JobOperator jobOperator;
    private final Job labRemovalNotifierJob;

    @Autowired
    public LabRemovalNotifierScheduler(
            JobOperator jobOperator,
            @Qualifier("labRemovalNotifierJob") Job labRemovalNotifierJob) {
        this.jobOperator = jobOperator;
        this.labRemovalNotifierJob = labRemovalNotifierJob;
    }

    /**
     * Scheduled to run every 120 seconds (configurable via application.properties)
     * Each execution uses unique non-identifying parameters to allow re-execution
     * 
     * JobLauncher is marked deprecated but is still the standard approach for Spring Batch 6.0+
     * when using non-identifying job parameters. This is the recommended pattern.
     */
    @Scheduled(fixedRateString = "${scheduler.lab-removal-notifier-rate:120000}", initialDelay = 60000)
    public void runLabRemovalNotifierJob() {
        try {
            log.info("Starting Lab Removal Notifier Job");
            
            // Create unique parameters with non-identifying flags (false parameter)
            // This allows multiple executions without JobInstanceAlreadyCompleteException
            var jobParameters = new JobParametersBuilder()
                    .addLong("labRemovalNotifierJob.run.id", System.nanoTime(), false)
                    .addLong("timestamp", System.currentTimeMillis(), false)
                    .toJobParameters();
            Properties properties = new Properties();
            properties.put("labRemovalNotifierJob.run.id",String.valueOf( System.nanoTime()));
            properties.put("timestamp", String.valueOf(System.currentTimeMillis()));
            log.debug("Lab Removal Job Parameters: {}", jobParameters);

            Long execution = jobOperator.start(labRemovalNotifierJob.getName(), properties);
            log.info("Lab Removal Notifier Job completed with status: {}", execution);
        } catch (Exception e) {
            log.error("Error executing Lab Removal Notifier Job", e);
        }
    }
}

