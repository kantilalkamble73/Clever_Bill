package com.cleverpos.stock;
import com.cleverpos.common.TenantRepo;
import org.springframework.data.jpa.repository.Query;
public interface StockEntryRepository extends TenantRepo<StockEntry> {
  @Query("select coalesce(sum(case when upper(e.type) = 'OUT' then -e.qty else e.qty end), 0L) from StockEntry e where e.tenantId = ?1 and e.productId = ?2")
  Long balance(String tenantId, Long productId);
}
