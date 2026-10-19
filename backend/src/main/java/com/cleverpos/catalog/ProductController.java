package com.cleverpos.catalog;
import com.cleverpos.common.*;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/products")
public class ProductController extends CrudController<Product> {
  private final ProductRepository repo;
  public ProductController(ProductRepository repo){ this.repo = repo; }
  protected TenantRepo<Product> repo(){ return repo; }
}
