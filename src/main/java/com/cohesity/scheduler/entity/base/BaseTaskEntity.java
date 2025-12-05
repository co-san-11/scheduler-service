package com.cohesity.scheduler.entity.base;


import com.cohesity.scheduler.entity.Status;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@MappedSuperclass
@Data
public abstract class BaseTaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    private String payload;

    private Status status;

    private LocalDateTime createdAt = LocalDateTime.now();
}
