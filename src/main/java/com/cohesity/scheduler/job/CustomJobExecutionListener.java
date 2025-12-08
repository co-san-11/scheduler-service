package com.cohesity.scheduler.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.context.annotation.Bean;


public class CustomJobExecutionListener implements JobExecutionListener {

    private static final Logger logger = LoggerFactory.getLogger(CustomJobExecutionListener.class);

    @Override
    public void beforeJob(JobExecution jobExecution) {
        logger.info("Job '{}' is about to start.", jobExecution.getJobInstanceId());
        // Add any setup logic here, e.g., resource initialization, logging.
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        logger.info("Job '{}' has finished with status: {}",
                jobExecution.getJobInstanceId(), jobExecution.getStatus());
        // Add any cleanup or reporting logic here.
        if (jobExecution.getStatus().isUnsuccessful()) {
            logger.error("Job '{}' failed!", jobExecution.getJobInstanceId());
            // Perform error handling or notifications.
        }
    }
}
