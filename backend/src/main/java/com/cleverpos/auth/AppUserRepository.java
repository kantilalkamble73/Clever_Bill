package com.cleverpos.auth;
import com.cleverpos.common.TenantRepo;
import java.util.Optional;
public interface AppUserRepository extends TenantRepo<AppUser> { Optional<AppUser> findByUsername(String username); }
