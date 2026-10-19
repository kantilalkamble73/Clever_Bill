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
Set `ANTHROPIC_API_KEY` in the backend environment (optional model override: `app.ai.model`). The assistant sends aggregated shop figures
(sales, GST, top products, low stock, khata balances with customer names, no phone numbers) to the Anthropic API to answer questions.
Without the key, the page shows a setup message and the dashboard keeps its rule-based insights.

## Fonts
Fonts load from Google Fonts (Plus Jakarta Sans, Inter, Manrope, Sora, Space Grotesk, JetBrains Mono, Noto Sans Devanagari for Hindi/Marathi). Offline, the app falls back to system fonts. Change style and size in Appearance.
