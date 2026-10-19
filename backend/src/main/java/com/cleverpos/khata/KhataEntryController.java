package com.cleverpos.khata;
import com.cleverpos.common.*;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/khata")
public class KhataEntryController extends CrudController<KhataEntry> {
  private final KhataEntryRepository repo;
  public KhataEntryController(KhataEntryRepository repo){ this.repo = repo; }
  protected TenantRepo<KhataEntry> repo(){ return repo; }
}
