package com.cleverpos.common;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
public abstract class CrudController<T extends BaseEntity> {
  protected abstract TenantRepo<T> repo();
  protected void afterCreate(String tenant, T saved) {}
  private T owned(Long id, String t){
    return repo().findById(id).filter(x -> t.equals(x.getTenantId())).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }
  @GetMapping public List<T> list(@RequestAttribute("tenantId") String t){ return repo().findByTenantIdOrderByIdDesc(t); }
  @PostMapping @Transactional
  public T create(@RequestAttribute("tenantId") String t, @RequestHeader(value="Idempotency-Key", required=false) String key, @jakarta.validation.Valid @RequestBody T e){
    if (key != null) { var ex = repo().findByTenantIdAndIdemKey(t, key); if (ex.isPresent()) return ex.get(); }
    e.setId(null); e.setTenantId(t); e.setIdemKey(key);
    T saved = repo().save(e); afterCreate(t, saved); return saved; }
  @PutMapping("/{id}") public T update(@RequestAttribute("tenantId") String t, @PathVariable Long id, @jakarta.validation.Valid @RequestBody T e){
    owned(id,t); e.setId(id); e.setTenantId(t); return repo().save(e); }
  @DeleteMapping("/{id}") public void delete(@RequestAttribute("tenantId") String t, @PathVariable Long id){ repo().delete(owned(id,t)); }
}
