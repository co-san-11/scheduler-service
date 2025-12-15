package com.cohesity.scheduler.processor;


import com.cohesity.scheduler.entity.EmailTask;
import com.cohesity.scheduler.entity.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Component
@Slf4j
@Qualifier("EmailTaskProcessor")
public class EmailTaskProcessor implements ItemProcessor<EmailTask, EmailTask> {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public EmailTask process(EmailTask item) throws Exception {
        // Call external notification API
        try {
            log.info("Sending email for payload: {}", item.getPayload());
            // Example API call
            // restTemplate.postForObject("http://your-api/email", item.getPayload(), String.class);
            item.setStatus(Status.COMP.name());
            item.setUpdatedAt(LocalDateTime.now());
            return item;
        } catch (Exception e) {
            log.error("Error processing email task", e);
            throw new RuntimeException(e);
        }
    }
}

