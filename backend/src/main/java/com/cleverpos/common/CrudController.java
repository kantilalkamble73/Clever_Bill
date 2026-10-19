package com.cleverpos.common;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
public abstract class CrudController<T extends BaseEntity> {
  protected abstract TenantRepo<T> repo();
  private T owned(Long id, String t){
    return repo().findById(id).filter(x -> t.equals(x.getTenantId()))
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }
  @GetMapping public List<T> list(@RequestAttribute("tenantId") String t){
    return repo().findByTenantIdOrderByIdDesc(t); }
  @PostMapping public T create(@RequestAttribute("tenantId") String t, @jakarta.validation.Valid @RequestBody T e){
    e.setId(null); e.setTenantId(t); return repo().save(e); }
  @PutMapping("/{id}") public T update(@RequestAttribute("tenantId") String t, @PathVariable Long id, @jakarta.validation.Valid @RequestBody T e){
    owned(id,t); e.setId(id); e.setTenantId(t); return repo().save(e); }
  @DeleteMapping("/{id}") public void delete(@RequestAttribute("tenantId") String t, @PathVariable Long id){
    repo().delete(owned(id,t)); }
}
