# Clever POS - Frontend
React 18 + Vite. UI runs on http://localhost:5173 and talks to the backend API.

## Run
- `npm install && npm run dev`
- or with Docker: `docker compose up`

The API address comes from `VITE_API` (default `http://localhost:8080/api`). Start the backend first.
Build for production: `npm run build` (output in `dist/`).

## Using the app
Open the UI and choose "Register a new shop" (creates the OWNER and an isolated tenant). OWNER adds ADMIN / CASHIER users in Staff Users.

## Sidebar colours
Every menu group and item has its own colour (coloured icon chip, group dot, coloured active highlight). Colours are the 4th value of each item in `NAV` in `src/App.jsx`. Works in dark and light mode, and when the sidebar is collapsed.

## Appearance
Dark / light / auto, accent colour, density, font and text size are set in Appearance and saved in the browser. Fonts load from Google Fonts (Plus Jakarta Sans, Inter, Manrope, Sora, Space Grotesk, JetBrains Mono, Noto Sans Devanagari for Hindi/Marathi); offline it falls back to system fonts.

## Structure
- `src/modules.json` drives the generic CRUD screens (shop profile, catalog, stock, purchasing, khata, payments, staff).
- `src/App.jsx` layout, billing (POS) and dashboard. `src/Reports.jsx` reports and AI assistant. `src/theme.jsx` appearance. `src/api.js` API client.
