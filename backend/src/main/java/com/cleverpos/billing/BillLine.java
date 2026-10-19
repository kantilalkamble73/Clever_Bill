package com.cleverpos.billing;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import lombok.*;
@Embeddable @Getter @Setter
public class BillLine {
  private Long productId; private String name; private Integer qty;
  private BigDecimal price, gstRate, gstAmount, lineTotal;
}
