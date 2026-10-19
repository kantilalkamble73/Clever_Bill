package com.cleverpos.stock;
import com.cleverpos.common.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;
@Entity @Table(name="pos_stockentry") @Getter @Setter
public class StockEntry extends BaseEntity {
  private Long productId;
  private Integer qty;
  private String type;
  private String note;
}
