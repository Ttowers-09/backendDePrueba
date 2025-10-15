package com.arsw.ids_ia.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;

@Getters
@Setters
@Builder

@Entity
@Table(name = "calls")

public class Call {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "case_id")
    private Case caseEntity;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime timestamp;

    @Builder.Default
    @Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(nullable = false)
    private CallStatus status = CallStatus.ACTIVE;

    public enum CallStatus {
        ACTIVE,
        RESOLVED,
        CLOSED
    }
}
