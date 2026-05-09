package com.bpcl.audit_portal.common.model;

import com.bpcl.audit_portal.common.constants.AppPermission;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "permissions")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    private AppPermission name;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
