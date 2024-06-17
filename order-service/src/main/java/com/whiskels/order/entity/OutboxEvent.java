package com.whiskels.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "outbox")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private UUID id;
    private String topic;
    private String event;
    @Column(insertable = false)
    private LocalDateTime created;
    private LocalDateTime sent;

    public static OutboxEvent of(final String topic, final String event) {
        return new OutboxEvent(null, topic, event, null, null);
    }
}
