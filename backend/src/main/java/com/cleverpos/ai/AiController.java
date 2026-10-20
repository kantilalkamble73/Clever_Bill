package com.cleverpos.ai;
import com.cleverpos.reporting.*;
import com.fasterxml.jackson.databind.JsonNode;
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
  private static final String SYSTEM = "You are a smart business assistant for an Indian retail shop that uses GST billing. Use only the shop data provided. Be concise and practical, reply in the same language the owner writes in (Hinglish is fine). Amounts are in INR. If the data is not enough, say so.";
  private final ReportingController rep; private final ReportDetailController det; private final ObjectMapper om;
  private final String provider, claudeKey, claudeModel, geminiKey, geminiModel, ollamaUrl, ollamaModel; private final HttpClient http = HttpClient.newHttpClient();
  public AiController(ReportingController r, ReportDetailController d, ObjectMapper om,
      @Value("${app.ai.provider:gemini}") String provider, @Value("${app.ai.key:}") String claudeKey, @Value("${app.ai.model:claude-sonnet-5-5}") String claudeModel,
      @Value("${app.ai.gemini-key:}") String geminiKey, @Value("${app.ai.gemini-model:gemini-3.8-flash}") String geminiModel,
      @Value("${app.ai.ollama-url:http://localhost:11434}") String ollamaUrl, @Value("${app.ai.ollama-model:llama3.2}") String ollamaModel){
    rep = r; det = d; this.om = om; this.provider = provider.trim().toLowerCase(); this.claudeKey = claudeKey.trim(); this.claudeModel = claudeModel.trim();
    this.geminiKey = geminiKey.trim(); this.geminiModel = geminiModel.trim(); this.ollamaUrl = ollamaUrl.trim(); this.ollamaModel = ollamaModel.trim(); }
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
    boolean gem = provider.equals("gemini"), oll = provider.equals("ollama");
    if (!oll && (gem ? geminiKey : claudeKey).isEmpty())
      return Map.of("provider", "not-configured", "answer", gem
        ? "AI assistant is not configured yet. Put your Gemini API key in application.yml (app.ai.gemini-key) or set GEMINI_API_KEY, then restart the backend."
        : "AI assistant is not configured yet. Put your Anthropic API key in application.yml (app.ai.key) or set ANTHROPIC_API_KEY, then restart the backend.");
    Map<String,Object> detail = new LinkedHashMap<>(det.detail(t, null, null)); detail.remove("rows");
    String prompt = "Shop data (JSON):\n" + om.writeValueAsString(Map.of("summary", rep.summary(t), "last30days", detail)) + "\n\nQuestion: " + a.question();
    HttpRequest.Builder rb = HttpRequest.newBuilder().timeout(Duration.ofSeconds(oll ? 180 : 60)).header("content-type", "application/json"); Object body;
    if (oll) {
      body = Map.of("model", ollamaModel, "stream", false, "messages", List.of(Map.of("role", "system", "content", SYSTEM), Map.of("role", "user", "content", prompt)));
      rb.uri(URI.create(ollamaUrl + "/api/chat"));
    } else if (gem) {
      body = Map.of("systemInstruction", Map.of("parts", List.of(Map.of("text", SYSTEM))),
        "contents", List.of(Map.of("role", "user", "parts", List.of(Map.of("text", prompt)))),
        "generationConfig", Map.of("maxOutputTokens", 1500, "thinkingConfig", Map.of("thinkingBudget", 0)));
      rb.uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/" + geminiModel + ":generateContent")).header("x-goog-api-key", geminiKey);
    } else {
      body = Map.of("model", claudeModel, "max_tokens", 800, "system", SYSTEM, "messages", List.of(Map.of("role", "user", "content", prompt)));
      rb.uri(URI.create("https://api.anthropic.com/v1/messages")).header("x-api-key", claudeKey).header("anthropic-version", "2023-06-01");
    }
    HttpResponse<String> r;
    try { r = http.send(rb.POST(HttpRequest.BodyPublishers.ofString(om.writeValueAsString(body))).build(), HttpResponse.BodyHandlers.ofString()); }
    catch (java.io.IOException ex) { throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, oll ? "Cannot reach Ollama at " + ollamaUrl + ". Is it running?" : "Cannot reach the AI provider. Check your internet connection."); }
    JsonNode j; try { j = om.readTree(r.body()); } catch (Exception e) { j = om.createObjectNode(); }
    if (r.statusCode() != 200) throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI provider error (" + r.statusCode() + "): " + j.path("error").path("message").asText(""));
    String text = oll ? j.path("message").path("content").asText("") : gem ? j.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText("") : j.path("content").path(0).path("text").asText("");
    return Map.of("provider", oll ? "ollama" : gem ? "gemini" : "claude", "answer", text.isBlank() ? "No answer came back. Try rephrasing your question." : text);
  }
}
