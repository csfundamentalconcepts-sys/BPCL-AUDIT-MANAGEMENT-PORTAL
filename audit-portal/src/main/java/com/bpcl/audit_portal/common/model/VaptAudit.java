package com.bpcl.audit_portal.common.model;

import com.bpcl.audit_portal.common.constants.VaptAuditStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "vapt_audits",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"vapt_card_id", "audit_year"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VaptAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "audit_year", nullable = false)
    private Integer auditYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vapt_card_id", nullable = false)
    private VaptCard vaptCard;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VaptAuditStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}