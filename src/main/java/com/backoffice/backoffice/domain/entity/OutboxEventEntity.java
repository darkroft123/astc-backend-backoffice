package com.backoffice.backoffice.domain.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_events")
public class OutboxEventEntity {

    @Id
    public UUID id;

    public String aggregateType;
    public UUID aggregateId;
    public String eventType;

    @Column(columnDefinition = "TEXT")
    public String payload;

    public LocalDateTime createdAt;
    public LocalDateTime processedAt;
}