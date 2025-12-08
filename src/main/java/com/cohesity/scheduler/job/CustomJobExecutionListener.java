package com.cohesity.scheduler.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;


public class CustomJobExecutionListener implements JobExecutionListener {

    private static final Logger logger = LoggerFactory.getLogger(CustomJobExecutionListener.class);

    @Override
    public void beforeJob(JobExecution jobExecution) {
        logger.info("Job '{}' is about to start.", jobExecution.getJobId());
        // Add any setup logic here, e.g., resource initialization, logging.
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        logger.info("Job '{}' has finished with status: {}",
                jobExecution.getJobId(), jobExecution.getStatus());
        // Add any cleanup or reporting logic here.
        if (jobExecution.getStatus().isUnsuccessful()) {
            logger.error("Job '{}' failed!", jobExecution.getJobId());
            // Perform error handling or notifications.
        }
    }
}
