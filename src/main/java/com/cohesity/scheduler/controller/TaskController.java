package com.cohesity.scheduler.controller;


import com.cohesity.scheduler.entity.EmailTask;
import com.cohesity.scheduler.entity.Status;
import com.cohesity.scheduler.repository.EmailTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final EmailTaskRepository emailTaskRepository;

    @PostMapping("/email")
    public EmailTask addEmailTask(@RequestBody String payload) {
        EmailTask task = new EmailTask();
        task.setPayload(payload);
        task.setStatus(Status.PEND.name());
        return emailTaskRepository.save(task);
    }
}

