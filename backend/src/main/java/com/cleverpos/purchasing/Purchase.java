package com.cleverpos.purchasing;
import com.cleverpos.common.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;
@Entity @Table(name="pos_purchase") @Getter @Setter
public class Purchase extends BaseEntity {
  private String supplier;
  private String invoiceNo;
  private BigDecimal amount;
}
