package com.cohesity.scheduler.entity;


import com.cohesity.scheduler.entity.base.BaseTaskEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "email_task")
public class EmailTask extends BaseTaskEntity {
}

