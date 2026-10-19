package com.cleverpos.integration;
import com.cleverpos.billing.*;
import com.cleverpos.shop.*;
import java.math.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/integrations")
public class IntegrationController {
  private final BillRepository bills; private final ShopProfileRepository shops;
  public IntegrationController(BillRepository b, ShopProfileRepository s){ bills = b; shops = s; }
  private Bill bill(String t, Long id){
    return bills.findById(id).filter(b -> t.equals(b.getTenantId())).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill not found")); }
  private static String enc(String s){ return URLEncoder.encode(s, StandardCharsets.UTF_8).replace("+", "%20"); }
  @GetMapping("/upi-link")
  public Map<String,String> upi(@RequestAttribute("tenantId") String t, @RequestParam Long billId){
    Bill b = bill(t, billId);
    ShopProfile s = shops.findByTenantIdOrderByIdDesc(t).stream().findFirst().orElse(null);
    if (s == null || s.getUpiId() == null || s.getUpiId().isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Add your UPI ID in Shop & Tax Profile first");
    BigDecimal due = b.getTotal().subtract(b.getPaid()); if (due.signum() <= 0) due = b.getTotal();
    String amt = due.setScale(2, RoundingMode.HALF_UP).toPlainString();
    return Map.of("amount", amt, "link", "upi://pay?pa=" + enc(s.getUpiId()) + "&pn=" + enc(String.valueOf(s.getName())) + "&am=" + amt + "&cu=INR&tn=" + enc(b.getBillNo())); }
  @GetMapping("/whatsapp-link")
  public Map<String,String> whatsapp(@RequestAttribute("tenantId") String t, @RequestParam Long billId){
    Bill b = bill(t, billId);
    String ph = b.getCustomerPhone() == null ? "" : b.getCustomerPhone().replaceAll("\\D", "");
    if (ph.length() == 10) ph = "91" + ph;
    if (ph.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This bill has no customer phone");
    String txt = "Invoice " + b.getBillNo() + ": total Rs " + b.getTotal() + ", paid Rs " + b.getPaid() + ". Thank you for shopping with us!";
    return Map.of("link", "https://wa.me/" + ph + "?text=" + enc(txt)); }
  @GetMapping("/gst/validate")
  public Map<String,Object> gst(@RequestParam String gstin){
    boolean ok = gstin.matches("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$");
    return Map.of("valid", ok, "stateCode", ok ? gstin.substring(0, 2) : ""); }
}
