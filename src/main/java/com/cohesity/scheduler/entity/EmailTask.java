package com.cohesity.scheduler.entity;


import com.cohesity.scheduler.entity.base.BaseTaskEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Entity
@Table(name = "email_task")
@Data()
public class EmailTask extends BaseTaskEntity {
}

