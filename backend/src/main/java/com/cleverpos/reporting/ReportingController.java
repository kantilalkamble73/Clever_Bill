package com.cleverpos.reporting;
import com.cleverpos.billing.*;
import com.cleverpos.catalog.ProductRepository;
import com.cleverpos.khata.KhataEntryRepository;
import com.cleverpos.purchasing.PurchaseRepository;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/reports")
public class ReportingController {
  private final BillRepository bills; private final ProductRepository products;
  private final PurchaseRepository purchases; private final KhataEntryRepository khata;
  public ReportingController(BillRepository b, ProductRepository p, PurchaseRepository pu, KhataEntryRepository k){ bills = b; products = p; purchases = pu; khata = k; }
  private static BigDecimal nz(BigDecimal v){ return v == null ? BigDecimal.ZERO : v; }
  @GetMapping("/summary")
  public Map<String,Object> summary(@RequestAttribute("tenantId") String t){
    List<Bill> bl = bills.findByTenantIdOrderByIdDesc(t).stream().filter(x -> !"RETURNED".equals(x.getStatus())).toList();
    BigDecimal sales = bl.stream().map(x -> nz(x.getTotal())).reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal bought = purchases.findByTenantIdOrderByIdDesc(t).stream().map(x -> nz(x.getAmount())).reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal due = khata.findByTenantIdOrderByIdDesc(t).stream()
      .map(x -> "PAYMENT".equalsIgnoreCase(x.getType()) ? nz(x.getAmount()).negate() : nz(x.getAmount())).reduce(BigDecimal.ZERO, BigDecimal::add);
    Map<String,BigDecimal> days = new TreeMap<>();
    for (int i = 6; i >= 0; i--) days.put(LocalDate.now().minusDays(i).toString(), BigDecimal.ZERO);
    bl.forEach(x -> days.computeIfPresent(x.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate().toString(), (k, v) -> v.add(nz(x.getTotal()))));
    Map<String,BigDecimal> tp = new HashMap<>();
    bl.stream().flatMap(x -> x.getItems().stream()).forEach(l -> tp.merge(String.valueOf(l.getName()), nz(l.getLineTotal()), BigDecimal::add));
    List<Map<String,Object>> top = tp.entrySet().stream().sorted(Map.Entry.<String,BigDecimal>comparingByValue().reversed()).limit(5)
      .map(e -> Map.<String,Object>of("name", e.getKey(), "amount", e.getValue())).toList();
    Map<String,Object> m = new LinkedHashMap<>();
    m.put("bills", bl.size()); m.put("sales", sales); m.put("purchases", bought); m.put("khataDue", due);
    m.put("products", products.findByTenantIdOrderByIdDesc(t).size()); m.put("daily", days); m.put("topProducts", top);
    return m;
  }
}
