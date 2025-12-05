//package com.cohesity.scheduler.config;
//
//import lombok.RequiredArgsConstructor;
//
//
//import org.springframework.batch.core.step.Step;
//import org.springframework.batch.infrastructure.item.ItemProcessor;
//import org.springframework.batch.infrastructure.item.ItemReader;
//import org.springframework.batch.infrastructure.item.ItemWriter;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//public class StepBuilderConfig {
//
//    private final StepBuilderFactory stepBuilderFactory;
//
//    public <T> Step buildStep(
//            String stepName,
//            ItemReader<T> reader,
//            ItemProcessor<T, T> processor,
//            ItemWriter<T> writer) {
//
//        return stepBuilderFactory.get(stepName)
//                .<T, T>chunk(20)
//                .reader(reader)
//                .processor(processor)
//                .writer(writer)
//                .faultTolerant()
//                .retryLimit(3)
//                .build();
//    }
//}
