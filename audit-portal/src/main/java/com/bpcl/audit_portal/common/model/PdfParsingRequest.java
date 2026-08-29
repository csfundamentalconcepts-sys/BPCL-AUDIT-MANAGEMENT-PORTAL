package com.bpcl.audit_portal.common.model;

import com.bpcl.audit_portal.common.constants.ParsingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "pdf_parsing_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PdfParsingRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phase_id", nullable = false)
    private Long phaseId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "password")
    private String password;

    @Column(name = "blob_name", nullable = false)
    private String blobName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParsingStatus status;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
