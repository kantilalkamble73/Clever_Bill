package com.cleverpos.purchasing;
import com.cleverpos.catalog.ProductRepository;
import com.cleverpos.common.*;
import com.cleverpos.stock.*;
import java.math.*;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/purchases")
public class PurchaseController extends CrudController<Purchase> {
  private final PurchaseRepository repo; private final StockEntryRepository stock; private final ProductRepository products;
  public PurchaseController(PurchaseRepository repo, StockEntryRepository stock, ProductRepository products){ this.repo = repo; this.stock = stock; this.products = products; }
  protected TenantRepo<Purchase> repo(){ return repo; }
  @Override protected void afterCreate(String t, Purchase p){
    if (p.getProductId() == null || p.getQty() == null || p.getQty() <= 0) return;
    products.findById(p.getProductId()).filter(x -> t.equals(x.getTenantId())).ifPresent(x -> {
      StockEntry e = new StockEntry(); e.setTenantId(t); e.setProductId(x.getId()); e.setQty(p.getQty()); e.setType("IN");
      e.setNote("Purchase " + (p.getInvoiceNo() == null ? "" : p.getInvoiceNo())); stock.save(e);
      if (p.getAmount() != null) { x.setCostPrice(p.getAmount().divide(BigDecimal.valueOf(p.getQty()), 2, RoundingMode.HALF_UP)); products.save(x); }
    });
  }
}
