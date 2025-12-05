package com.cohesity.scheduler.entity;


import com.cohesity.scheduler.entity.base.BaseTaskEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "lab_removal_notifier_task")
public class LabRemovalNotifierTask extends BaseTaskEntity {
}

