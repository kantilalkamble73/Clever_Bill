# Clever POS
Backend: Spring Boot 3 + PostgreSQL (`backend`) | Frontend: React + Vite (`frontend`)

1. `docker compose up -d`  (PostgreSQL on 5432)
2. `cd backend && mvn spring-boot:run`  (API on :8080)
3. `cd frontend && npm install && npm run dev`  (UI on :5173)

Open the UI, choose "Register a new shop" (creates the OWNER and an isolated tenant).
OWNER adds ADMIN / CASHIER users in Staff Users. Change `app.jwt-secret` in application.yml before production.

- Auth: JWT (12h). Tenant isolation: tenantId comes from the token, every query is tenant-filtered.
- RBAC: OWNER all; ADMIN all except users; CASHIER billing, stock, khata, payments (no delete, no returns, no reports).
- Validation: Bean Validation (GSTIN format, required fields). Idempotency: `Idempotency-Key` header on POST /api/bills.
- Billing: line items, GST per line, auto stock OUT, auto payment, unpaid balance goes to Khata. Returns restore stock and reverse payment/khata.
- Integrations: UPI deep link + QR, WhatsApp share link, GSTIN validation, rule-based AI insights.

## AI Assistant
Default provider is Google Gemini (free tier). Get a key at aistudio.google.com, then put it in application.yml as `app.ai.gemini-key`
(or set `GEMINI_API_KEY`) and restart. To use Claude instead, set `provider: claude` and add `app.ai.key` (or `ANTHROPIC_API_KEY`).
The assistant sends aggregated shop figures (sales, GST, top products, low stock, khata balances with customer names, no phone numbers) to the provider.
Free-tier requests on Gemini may be used by Google to improve its products, so avoid it if that is a concern.

## Fonts
Fonts load from Google Fonts (Plus Jakarta Sans, Inter, Manrope, Sora, Space Grotesk, JetBrains Mono, Noto Sans Devanagari for Hindi/Marathi). Offline, the app falls back to system fonts. Change style and size in Appearance.

## Stock, purchasing and profit
- Stock Ledger and Purchasing pick products from a dropdown. A purchase with a product and quantity adds stock automatically and updates the product cost price.
- Shop & Tax Profile > "Block billing when out of stock" = ON makes billing refuse items without enough stock.
- Every create form sends an Idempotency-Key, so a double click does not save twice.
- Reports show gross profit for items that have a cost price.
- AI: `provider: ollama` runs a local free model (install Ollama, run `ollama pull llama3.2`, no key needed).
