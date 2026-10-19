package com.cleverpos.shop;
import com.cleverpos.common.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;
@Entity @Table(name="pos_shopprofile") @Getter @Setter
public class ShopProfile extends BaseEntity {
  @jakarta.validation.constraints.NotBlank private String name;
  @jakarta.validation.constraints.Pattern(regexp="^$|^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$", message="invalid GSTIN") private String gstin;
  private String phone;
  private String address;
  private String stateCode;
  private String upiId;
}
