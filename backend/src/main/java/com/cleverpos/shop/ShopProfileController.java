package com.cleverpos.shop;
import com.cleverpos.common.*;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/shop-profile")
public class ShopProfileController extends CrudController<ShopProfile> {
  private final ShopProfileRepository repo;
  public ShopProfileController(ShopProfileRepository repo){ this.repo = repo; }
  protected TenantRepo<ShopProfile> repo(){ return repo; }
}
