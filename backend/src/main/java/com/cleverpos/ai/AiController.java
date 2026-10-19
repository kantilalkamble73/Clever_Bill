package com.cleverpos.ai;
import com.cleverpos.reporting.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/ai")
public class AiController {
  private final ReportingController rep; private final ReportDetailController det; private final ObjectMapper om;
  private final String key, model; private final HttpClient http = HttpClient.newHttpClient();
  public AiController(ReportingController r, ReportDetailController d, ObjectMapper om, @Value("${app.ai.key:}") String key, @Value("${app.ai.model:claude-sonnet-5-5}") String model){
    rep = r; det = d; this.om = om; this.key = key; this.model = model; }
  public record Ask(@NotBlank String question){}
  @GetMapping("/insights") @SuppressWarnings("unchecked")
  public Map<String,Object> insights(@RequestAttribute("tenantId") String t){
    Map<String,Object> s = rep.summary(t), d = det.detail(t, null, null); List<String> out = new ArrayList<>();
    List<Map<String,Object>> top = (List<Map<String,Object>>) s.get("topProducts");
    if (top.isEmpty()) out.add("No sales yet. Create your first bill to unlock insights.");
    else out.add("Best seller: " + top.get(0).get("name") + " (Rs " + top.get(0).get("amount") + ").");
    if (((BigDecimal) s.get("khataDue")).signum() > 0) out.add("Rs " + s.get("khataDue") + " is pending in Khata. Send WhatsApp reminders.");
    if (((BigDecimal) s.get("purchases")).compareTo((BigDecimal) s.get("sales")) > 0) out.add("Purchases are higher than sales. Review your stock buying.");
    int low = ((List<?>) d.get("lowStock")).size();
    if (low > 0) out.add(low + " product(s) have 5 or fewer units in stock. Plan a purchase.");
    return Map.of("provider", "rule-based", "insights", out);
  }
  @PostMapping("/ask")
  public Map<String,Object> ask(@RequestAttribute("tenantId") String t, @Valid @RequestBody Ask a) throws Exception {
    if (key == null || key.isBlank())
      return Map.of("provider", "not-configured", "answer", "AI assistant is not configured yet. Set the ANTHROPIC_API_KEY environment variable for the backend and restart it.");
    Map<String,Object> detail = new LinkedHashMap<>(det.detail(t, null, null)); detail.remove("rows");
    String data = om.writeValueAsString(Map.of("summary", rep.summary(t), "last30days", detail));
    Map<String,Object> body = Map.of("model", model, "max_tokens", 800,
      "system", "You are a smart business assistant for an Indian retail shop that uses GST billing. Use only the shop data provided. Be concise and practical, reply in the same language the owner writes in (Hinglish is fine). Amounts are in INR. If the data is not enough, say so.",
      "messages", List.of(Map.of("role", "user", "content", "Shop data (JSON):\n" + data + "\n\nQuestion: " + a.question())));
    HttpRequest req = HttpRequest.newBuilder(URI.create("https://api.anthropic.com/v1/messages")).header("x-api-key", key)
      .header("anthropic-version", "2023-06-01").header("content-type", "application/json").timeout(Duration.ofSeconds(45))
      .POST(HttpRequest.BodyPublishers.ofString(om.writeValueAsString(body))).build();
    HttpResponse<String> r = http.send(req, HttpResponse.BodyHandlers.ofString());
    if (r.statusCode() != 200) throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI provider error (" + r.statusCode() + ")");
    return Map.of("provider", "claude", "answer", om.readTree(r.body()).path("content").path(0).path("text").asText(""));
  }
}
