package com.cleverpos.billing;
import com.cleverpos.catalog.*;
import com.cleverpos.khata.*;
import com.cleverpos.payments.*;
import com.cleverpos.stock.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/bills")
public class BillingController {
  private final BillRepository bills; private final ProductRepository products; private final StockEntryRepository stockRepo;
  private final KhataEntryRepository khataRepo; private final PaymentRepository payRepo;
  public BillingController(BillRepository b, ProductRepository p, StockEntryRepository s, KhataEntryRepository k, PaymentRepository pay){
    bills = b; products = p; stockRepo = s; khataRepo = k; payRepo = pay; }
  public record Item(@NotNull Long productId, @NotNull @Positive Integer qty){}
  public record NewBill(String customerName, String customerPhone, @NotEmpty List<@Valid Item> items, BigDecimal paid, String mode){}
  private static BigDecimal nz(BigDecimal v){ return v == null ? BigDecimal.ZERO : v; }
  private void stock(String t, Long pid, int qty, String type, String note){
    StockEntry e = new StockEntry(); e.setTenantId(t); e.setProductId(pid); e.setQty(qty); e.setType(type); e.setNote(note); stockRepo.save(e); }
  private void khata(String t, String name, String phone, BigDecimal amt, String type){
    KhataEntry k = new KhataEntry(); k.setTenantId(t); k.setCustomerName(name == null || name.isBlank() ? "Walk-in" : name); k.setPhone(phone); k.setAmount(amt); k.setType(type); khataRepo.save(k); }
  private void pay(String t, String billNo, String mode, BigDecimal amt){
    Payment p = new Payment(); p.setTenantId(t); p.setBillNo(billNo); p.setMode(mode); p.setAmount(amt); payRepo.save(p); }
  @GetMapping public List<Bill> list(@RequestAttribute("tenantId") String t){ return bills.findByTenantIdOrderByIdDesc(t); }
  @PostMapping @Transactional
  public Bill create(@RequestAttribute("tenantId") String t, @RequestHeader(value="Idempotency-Key", required=false) String key, @Valid @RequestBody NewBill n){
    if (key != null) { var ex = bills.findByTenantIdAndIdemKey(t, key); if (ex.isPresent()) return ex.get(); }
    Bill b = new Bill(); b.setTenantId(t); b.setIdemKey(key); b.setCustomerName(n.customerName()); b.setCustomerPhone(n.customerPhone());
    String mode = n.mode() == null ? "CASH" : n.mode(); b.setMode(mode);
    BigDecimal sub = BigDecimal.ZERO, gst = BigDecimal.ZERO;
    for (Item i : n.items()) {
      Product p = products.findById(i.productId()).filter(x -> t.equals(x.getTenantId()))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown product"));
      BigDecimal base = nz(p.getPrice()).multiply(BigDecimal.valueOf(i.qty()));
      BigDecimal g = base.multiply(nz(p.getGstRate())).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
      BillLine l = new BillLine(); l.setProductId(p.getId()); l.setName(p.getName()); l.setQty(i.qty());
      l.setPrice(p.getPrice()); l.setGstRate(p.getGstRate()); l.setGstAmount(g); l.setLineTotal(base.add(g));
      b.getItems().add(l); sub = sub.add(base); gst = gst.add(g); stock(t, p.getId(), i.qty(), "OUT", "Sale");
    }
    BigDecimal total = sub.add(gst), paid = n.paid() == null ? total : n.paid().min(total).max(BigDecimal.ZERO);
    b.setSubtotal(sub); b.setGstTotal(gst); b.setTotal(total); b.setPaid(paid);
    b.setStatus(paid.compareTo(total) >= 0 ? "PAID" : "DUE");
    b.setBillNo("INV-" + String.format("%05d", bills.findByTenantIdOrderByIdDesc(t).size() + 1));
    b = bills.save(b);
    if (paid.signum() > 0) pay(t, b.getBillNo(), mode, paid);
    BigDecimal due = total.subtract(paid);
    if (due.signum() > 0) khata(t, n.customerName(), n.customerPhone(), due, "CREDIT");
    return b;
  }
  @PostMapping("/{id}/return") @Transactional
  public Bill giveReturn(@RequestAttribute("tenantId") String t, @PathVariable Long id){
    Bill b = bills.findById(id).filter(x -> t.equals(x.getTenantId())).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill not found"));
    if ("RETURNED".equals(b.getStatus())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Bill already returned");
    for (BillLine l : b.getItems()) stock(t, l.getProductId(), l.getQty(), "IN", "Return " + b.getBillNo());
    if (b.getPaid().signum() > 0) pay(t, b.getBillNo(), "REFUND", b.getPaid().negate());
    BigDecimal due = b.getTotal().subtract(b.getPaid());
    if (due.signum() > 0) khata(t, b.getCustomerName(), b.getCustomerPhone(), due, "PAYMENT");
    b.setStatus("RETURNED"); return bills.save(b);
  }
}
