package com.cleverpos.payments;
import com.cleverpos.common.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;
@Entity @Table(name="pos_payment") @Getter @Setter
public class Payment extends BaseEntity {
  private String billNo;
  private String mode;
  private BigDecimal amount;
  private String reference;
}
