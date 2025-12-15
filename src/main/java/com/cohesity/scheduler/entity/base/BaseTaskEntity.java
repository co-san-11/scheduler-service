package com.cohesity.scheduler.entity.base;


import com.cohesity.scheduler.entity.Status;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@MappedSuperclass
@Data
public abstract class BaseTaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Lob
    private String payload;

    private String status;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

}
