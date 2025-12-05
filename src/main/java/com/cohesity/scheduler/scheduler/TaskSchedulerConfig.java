//package com.cohesity.scheduler.scheduler;
//
//
//import lombok.RequiredArgsConstructor;
//
//import org.springframework.batch.core.configuration.JobRegistry;
//import org.springframework.batch.core.job.Job;
//import org.springframework.batch.core.job.parameters.JobParametersBuilder;
//import org.springframework.batch.core.launch.JobLauncher;
//import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//@Component
//@EnableScheduling
//@RequiredArgsConstructor
//public class TaskSchedulerConfig {
//
//    private final JobLauncher jobLauncher;
//    private final JobRegistry jobRegistry;
//
//    @Scheduled(fixedDelay = 60000) // every 1 minute
//    public void runJobs() {
//        try {
//            for (String jobName : jobRegistry.getJobNames()) {
//                Job job = jobRegistry.getJob(jobName);
//                jobLauncher.run(job,
//                        new JobParametersBuilder()
//                                .addLong("timestamp", System.currentTimeMillis())
//                                .toJobParameters());
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
//
