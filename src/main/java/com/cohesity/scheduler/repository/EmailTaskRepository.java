package com.cohesity.scheduler.repository;


import com.cohesity.scheduler.entity.EmailTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailTaskRepository extends JpaRepository<EmailTask, Long> {
}

