package com.cleverpos.shop;
import com.cleverpos.common.TenantRepo;
import jakarta.persistence.LockModeType;
import java.util.List;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
public interface ShopProfileRepository extends TenantRepo<ShopProfile> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select s from ShopProfile s where s.tenantId = ?1 order by s.id desc")
  List<ShopProfile> lockByTenant(String tenantId);
}
