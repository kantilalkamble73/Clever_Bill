package com.cleverpos.auth;
import com.cleverpos.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name="pos_user") @Getter @Setter
public class AppUser extends BaseEntity {
  @Column(unique=true, nullable=false) private String username;
  private String name;
  private String passwordHash;
  private String role;
}
