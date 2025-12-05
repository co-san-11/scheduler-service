package com.cohesity.scheduler.entity.base;


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

    private LocalDateTime createdAt = LocalDateTime.now();
}
