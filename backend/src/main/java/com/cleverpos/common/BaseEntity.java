package com.cleverpos.common;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;
@MappedSuperclass @Getter @Setter
public abstract class BaseEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @Column(nullable = false) private String tenantId;
  @Column(updatable = false) private Instant createdAt;
  @PrePersist void onCreate(){ createdAt = Instant.now(); }
}
