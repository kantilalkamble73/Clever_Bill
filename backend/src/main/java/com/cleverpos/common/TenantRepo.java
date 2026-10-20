package com.cleverpos.common;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
@NoRepositoryBean
public interface TenantRepo<T extends BaseEntity> extends JpaRepository<T, Long> {
  List<T> findByTenantIdOrderByIdDesc(String tenantId);
  Optional<T> findByTenantIdAndIdemKey(String tenantId, String idemKey);
}
