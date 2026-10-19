package com.cleverpos.catalog;
import com.cleverpos.common.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;
@Entity @Table(name="pos_product") @Getter @Setter
public class Product extends BaseEntity {
  @jakarta.validation.constraints.NotBlank private String name;
  private String sku;
  private String hsn;
  @jakarta.validation.constraints.PositiveOrZero private BigDecimal price;
  private BigDecimal gstRate;
  private String unit;
}
