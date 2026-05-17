package com.bpcl.audit_portal.common.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "vapt_audit_phases",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"vapt_audit_id", "phase_number"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VaptAuditPhase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phase_number", nullable = false)
    private Integer phaseNumber;

    @Column(nullable = false)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vapt_audit_id", nullable = false)
    private VaptAudit vaptAudit;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}