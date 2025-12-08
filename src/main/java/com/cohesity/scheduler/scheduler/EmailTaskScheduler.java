package com.cohesity.scheduler.scheduler;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.boot.batch.autoconfigure.JobLauncherApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailTaskScheduler {

    private final JobOperator jobOperator;
    private final Job emailTaskJob;

    @Scheduled(fixedRateString = "${scheduler.email-task-rate:60000}",initialDelay = 60000  )
    public void runEmailTaskJob() {
        System.out.println("starting the scheduler");
        log.info("Starting Scheduler");
        var jobParameters = new JobParametersBuilder()
                .addLocalDateTime("run.time", LocalDateTime.now())
                .addLong("run.id", System.currentTimeMillis()) // ensures uniqueness
                .toJobParameters();
        try {
            jobOperator.startNextInstance(
                    emailTaskJob
            );

        } catch (Exception e) {
            // use a logger instead of e.printStackTrace() in production
            e.printStackTrace();
            System.out.println("error in scheduler");
            log.info("log error in scheduler");
        }
    }
}