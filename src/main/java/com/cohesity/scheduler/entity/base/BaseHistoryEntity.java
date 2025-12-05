package com.cohesity.scheduler.entity.base;


import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@MappedSuperclass
@Data
public abstract class BaseHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    private String payload;

    private LocalDateTime processedAt = LocalDateTime.now();
}

