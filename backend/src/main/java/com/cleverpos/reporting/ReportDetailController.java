package com.cleverpos.reporting;
import com.cleverpos.billing.*;
import com.cleverpos.catalog.ProductRepository;
import com.cleverpos.khata.KhataEntryRepository;
import com.cleverpos.stock.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/reports")
public class ReportDetailController {
  private final BillRepository bills; private final ProductRepository products; private final KhataEntryRepository khata; private final StockEntryRepository stock;
  public ReportDetailController(BillRepository b, ProductRepository p, KhataEntryRepository k, StockEntryRepository s){ bills = b; products = p; khata = k; stock = s; }
  private static BigDecimal nz(BigDecimal v){ return v == null ? BigDecimal.ZERO : v; }
  private static BigDecimal sum(List<Bill> l, Function<Bill,BigDecimal> f){ return l.stream().map(x -> nz(f.apply(x))).reduce(BigDecimal.ZERO, BigDecimal::add); }
  @GetMapping("/detail")
  public Map<String,Object> detail(@RequestAttribute("tenantId") String t,
      @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to){
    LocalDate end = to == null ? LocalDate.now() : to; LocalDate start = from == null ? end.minusDays(29) : from;
    ZoneId z = ZoneId.systemDefault();
    List<Bill> range = bills.findByTenantIdOrderByIdDesc(t).stream().filter(b -> { LocalDate dt = b.getCreatedAt().atZone(z).toLocalDate(); return !dt.isBefore(start) && !dt.isAfter(end); }).toList();
    List<Bill> sold = range.stream().filter(b -> !"RETURNED".equals(b.getStatus())).toList();
    List<Bill> ret = range.stream().filter(b -> "RETURNED".equals(b.getStatus())).toList();
    BigDecimal sales = sum(sold, Bill::getTotal);
    Map<String,BigDecimal> daily = new TreeMap<>();
    for (LocalDate d = start; !d.isAfter(end) && daily.size() < 366; d = d.plusDays(1)) daily.put(d.toString(), BigDecimal.ZERO);
    sold.forEach(b -> daily.computeIfPresent(b.getCreatedAt().atZone(z).toLocalDate().toString(), (k, v) -> v.add(nz(b.getTotal()))));
    Map<String,BigDecimal> byMode = new LinkedHashMap<>();
    sold.forEach(b -> byMode.merge(b.getMode() == null ? "CASH" : b.getMode(), nz(b.getPaid()), BigDecimal::add));
    Map<String,BigDecimal[]> tp = new HashMap<>();
    sold.stream().flatMap(b -> b.getItems().stream()).forEach(l -> {
      BigDecimal[] a = tp.computeIfAbsent(String.valueOf(l.getName()), k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
      a[0] = a[0].add(BigDecimal.valueOf(l.getQty() == null ? 0 : l.getQty())); a[1] = a[1].add(nz(l.getLineTotal())); });
    List<Map<String,Object>> top = tp.entrySet().stream().sorted((x, y) -> y.getValue()[1].compareTo(x.getValue()[1])).limit(10)
      .map(e -> Map.<String,Object>of("name", e.getKey(), "qty", e.getValue()[0], "amount", e.getValue()[1])).toList();
    Map<String,BigDecimal> kh = new HashMap<>();
    khata.findByTenantIdOrderByIdDesc(t).forEach(k -> kh.merge(k.getCustomerName() == null ? "Walk-in" : k.getCustomerName(),
      "PAYMENT".equalsIgnoreCase(k.getType()) ? nz(k.getAmount()).negate() : nz(k.getAmount()), BigDecimal::add));
    List<Map<String,Object>> owing = kh.entrySet().stream().filter(e -> e.getValue().signum() > 0).sorted((x, y) -> y.getValue().compareTo(x.getValue())).limit(10)
      .map(e -> Map.<String,Object>of("name", e.getKey(), "amount", e.getValue())).toList();
    Map<Long,Integer> bal = stock.findByTenantIdOrderByIdDesc(t).stream().filter(e -> e.getProductId() != null)
      .collect(Collectors.groupingBy(StockEntry::getProductId, Collectors.summingInt(e -> { int q = e.getQty() == null ? 0 : e.getQty(); return "OUT".equalsIgnoreCase(e.getType()) ? -q : q; })));
    List<Map<String,Object>> low = products.findByTenantIdOrderByIdDesc(t).stream().filter(p -> bal.getOrDefault(p.getId(), 0) <= 5)
      .map(p -> Map.<String,Object>of("name", String.valueOf(p.getName()), "balance", bal.getOrDefault(p.getId(), 0))).limit(15).toList();
    List<Map<String,Object>> rows = range.stream().map(b -> { Map<String,Object> r = new LinkedHashMap<>();
      r.put("billNo", b.getBillNo()); r.put("date", b.getCreatedAt().atZone(z).toLocalDate().toString()); r.put("customer", b.getCustomerName());
      r.put("total", b.getTotal()); r.put("gst", b.getGstTotal()); r.put("paid", b.getPaid()); r.put("mode", b.getMode()); r.put("status", b.getStatus()); return r; }).toList();
    Map<String,Object> m = new LinkedHashMap<>();
    m.put("from", start.toString()); m.put("to", end.toString()); m.put("sales", sales); m.put("gst", sum(sold, Bill::getGstTotal)); m.put("bills", sold.size());
    m.put("avg", sold.isEmpty() ? BigDecimal.ZERO : sales.divide(BigDecimal.valueOf(sold.size()), 2, RoundingMode.HALF_UP));
    m.put("returns", sum(ret, Bill::getTotal)); m.put("daily", daily); m.put("byMode", byMode); m.put("topProducts", top);
    m.put("khataOwing", owing); m.put("lowStock", low); m.put("rows", rows);
    return m;
  }
}
