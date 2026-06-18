# Dashboard (MERN) – Real-time DPI UI

This folder contains a lightweight MERN-style dashboard that reads live traffic stats from the Java DPI engine.

For the full step-by-step instructions, see `RUNBOOK.md` in the repository root.

## Architecture

- Java DPI Engine (embedded HTTP server)
  - `GET http://localhost:8080/stats`
  - `GET http://localhost:8080/flows`
- Node/Express Backend (proxy)
  - `GET http://localhost:3001/api/stats` → forwards to Java `/stats`
  - `GET http://localhost:3001/api/flows` → forwards to Java `/flows`
- React Frontend
  - Fetches from Express backend (same-origin) and renders cards/charts/tables

## Run (local)

### 1) Start the Java engine

From `dpi-engine/`:

```bash
./run.sh --pcap ../test_dpi.pcap --rules rules.json --serve
```

Keep it running. Confirm:

- `http://localhost:8080/stats`
- `http://localhost:8080/flows`

### 2) Start dashboard backend

```bash
cd ../dashboard/backend
npm install
npm run dev
```

Backend runs at `http://localhost:3001`.

### 3) Start dashboard frontend

```bash
cd ../dashboard/frontend
npm install
npm run dev
```

Frontend runs at `http://localhost:5173`.

## What you should see

- Cards for packets processed/forwarded/dropped, packets/sec, active flows
- A live packets/sec line chart
- An application distribution pie chart
- Active flows table with search + pagination
- Blocked traffic table (recent events derived from blocked flows)

