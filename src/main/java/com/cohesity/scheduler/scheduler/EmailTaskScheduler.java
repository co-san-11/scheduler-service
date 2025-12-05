package com.cohesity.scheduler.scheduler;

import lombok.RequiredArgsConstructor;

import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;

@Component
@RequiredArgsConstructor
public class EmailTaskScheduler {

    private final JobOperator jobOperator;
    private final Job emailTaskJob;

    @Scheduled(fixedRateString = "${scheduler.email-task-rate:60000}")
    public void runEmailTaskJob() {

        try {
            jobOperator.start(
                    emailTaskJob,
                    new JobParameters()
            );
        } catch (Exception e) {
            // use a logger instead of e.printStackTrace() in production
            e.printStackTrace();
        }
    }
}
