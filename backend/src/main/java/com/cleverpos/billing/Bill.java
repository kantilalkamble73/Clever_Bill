package com.cleverpos.billing;
import com.cleverpos.common.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.*;
import lombok.*;
@Entity @Table(name="pos_bill", uniqueConstraints={@UniqueConstraint(columnNames={"tenant_id","idem_key"}), @UniqueConstraint(columnNames={"tenant_id","bill_no"})}) @Getter @Setter
public class Bill extends BaseEntity {
  private String billNo, customerName, customerPhone, status, mode;
  private BigDecimal subtotal, gstTotal, total, paid;
  @ElementCollection(fetch=FetchType.EAGER) @CollectionTable(name="pos_bill_line")
  private List<BillLine> items = new ArrayList<>();
}
