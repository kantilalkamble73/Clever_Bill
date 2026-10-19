package com.cleverpos.stock;
import com.cleverpos.common.*;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/stock")
public class StockEntryController extends CrudController<StockEntry> {
  private final StockEntryRepository repo;
  public StockEntryController(StockEntryRepository repo){ this.repo = repo; }
  protected TenantRepo<StockEntry> repo(){ return repo; }
}
