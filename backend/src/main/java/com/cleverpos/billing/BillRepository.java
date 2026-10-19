package com.cleverpos.billing;
import com.cleverpos.common.TenantRepo;
import java.util.Optional;
public interface BillRepository extends TenantRepo<Bill> { Optional<Bill> findByTenantIdAndIdemKey(String tenantId, String idemKey); }
