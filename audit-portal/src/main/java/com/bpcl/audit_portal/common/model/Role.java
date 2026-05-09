package com.bpcl.audit_portal.common.model;

import com.bpcl.audit_portal.common.constants.AppRole;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;


@Data
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    private AppRole roleName;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
