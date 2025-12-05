package com.cohesity.scheduler.repository;


import com.cohesity.scheduler.entity.LabRemovalNotifierTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabRemovalNotifierRepository extends JpaRepository<LabRemovalNotifierTask, Long> {
}

