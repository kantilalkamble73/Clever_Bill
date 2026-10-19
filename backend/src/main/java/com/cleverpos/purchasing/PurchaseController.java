package com.cleverpos.purchasing;
import com.cleverpos.common.*;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/purchases")
public class PurchaseController extends CrudController<Purchase> {
  private final PurchaseRepository repo;
  public PurchaseController(PurchaseRepository repo){ this.repo = repo; }
  protected TenantRepo<Purchase> repo(){ return repo; }
}
