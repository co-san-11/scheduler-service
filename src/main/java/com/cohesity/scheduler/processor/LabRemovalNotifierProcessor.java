package com.cohesity.scheduler.processor;


import com.cohesity.scheduler.entity.LabRemovalNotifierTask;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class LabRemovalNotifierProcessor implements ItemProcessor<LabRemovalNotifierTask, LabRemovalNotifierTask> {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public LabRemovalNotifierTask process(LabRemovalNotifierTask item) throws Exception {
        System.out.println("Notifying lab removal: " + item.getPayload());
        // Example API call
        // restTemplate.postForObject("http://your-api/lab-reminder", item.getPayload(), String.class);
        return item;
    }
}

