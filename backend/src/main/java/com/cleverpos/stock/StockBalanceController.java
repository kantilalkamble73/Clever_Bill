package com.cleverpos.stock;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.*;
@RestController
public class StockBalanceController {
  private final StockEntryRepository repo;
  public StockBalanceController(StockEntryRepository r){ repo = r; }
  @GetMapping("/api/stock-balance")
  public Map<Long,Integer> balance(@RequestAttribute("tenantId") String t){
    return repo.findByTenantIdOrderByIdDesc(t).stream().filter(e -> e.getProductId() != null)
      .collect(Collectors.groupingBy(StockEntry::getProductId, Collectors.summingInt(e -> {
        int q = e.getQty() == null ? 0 : e.getQty(); return "OUT".equalsIgnoreCase(e.getType()) ? -q : q; }))); }
}
