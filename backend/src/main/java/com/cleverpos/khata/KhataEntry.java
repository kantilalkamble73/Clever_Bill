package com.cleverpos.khata;
import com.cleverpos.common.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;
@Entity @Table(name="pos_khataentry") @Getter @Setter
public class KhataEntry extends BaseEntity {
  private String customerName;
  private String phone;
  private BigDecimal amount;
  private String type;
}
