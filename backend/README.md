# Clever POS - Backend
Spring Boot 3 (Java 17) + PostgreSQL. API runs on http://localhost:8080.

## Run
1. `docker compose up -d`   (PostgreSQL on 5432)
2. `mvn spring-boot:run`    (API on :8080)

## Configuration (environment variables)
| Variable | Default | Purpose |
|---|---|---|
| `JWT_SECRET` | dev placeholder | **Set this in production** (32+ chars). A warning is logged if the default is used. |
| `DB_URL` | `jdbc:postgresql://localhost:5432/cleverpos` | Database URL |
| `DB_USER` / `DB_PASSWORD` | `postgres` / `postgres` | Database login (also used by docker-compose) |
| `APP_TIMEZONE` | `Asia/Kolkata` | "Today" in reports follows this timezone |
| `GEMINI_API_KEY` / `ANTHROPIC_API_KEY` | empty | AI assistant (see below) |

## Security and roles
- Auth: JWT (12h). `tenantId` comes from the token and every query is tenant-filtered.
- OWNER: everything. ADMIN: everything except Staff Users. CASHIER: can only POST to bills, payments, khata and stock (no PUT, no DELETE, no returns, no reports, no AI).
- Validation: Bean Validation (GSTIN format, required fields). Idempotency: `Idempotency-Key` header on every create.

## Billing
- Line items, GST per line, auto stock OUT, auto payment, unpaid balance goes to Khata. Returns restore stock and reverse payment/khata.
- Billing and returns take a per-shop database lock: unique invoice numbers (plus a unique constraint on tenant + bill no), no overselling when two cashiers bill together, no double return.
- If an existing database already has duplicate bill numbers, clean them before starting (the new unique constraint needs a clean table).
- Shop & Tax Profile > "Block billing when out of stock" = ON refuses items without enough stock.
- A purchase with a product and quantity adds stock and updates the product cost price.

## AI Assistant
Default provider is Google Gemini (`app.ai.provider: gemini`). Set `GEMINI_API_KEY`, or use `provider: claude` with `ANTHROPIC_API_KEY`, or `provider: ollama` for a local model (`ollama pull llama3.2`, no key).
The assistant sends aggregated shop figures (sales, GST, top products, low stock, khata balances with customer names, no phone numbers) to the provider. Free-tier Gemini requests may be used by Google to improve its products.

## Known gaps
Decimal quantities (kg/litre), CGST/SGST/IGST split, khata-to-bill linking and SQL-level report aggregation are not done yet.
