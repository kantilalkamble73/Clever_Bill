package com.cleverpos.payments;
import com.cleverpos.common.*;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/payments")
public class PaymentController extends CrudController<Payment> {
  private final PaymentRepository repo;
  public PaymentController(PaymentRepository repo){ this.repo = repo; }
  protected TenantRepo<Payment> repo(){ return repo; }
}
