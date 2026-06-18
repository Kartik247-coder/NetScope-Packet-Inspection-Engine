# Run the DPI Engine + MERN Dashboard (Step-by-step)

This file is kept for backward compatibility.  
Use the canonical runbook: `RUNBOOK.md`.

## Prerequisites

- Java 17+
- Node.js 18+

## 1) Start the Java DPI Engine (Terminal 1)

```bash
cd /Users/kartikbobde/Desktop/Packet_analyzer-main/dpi-engine
./mvnw clean package
./run.sh --pcap "../test_dpi.pcap" --rules rules.json --serve
```

Keep it running.

### Verify engine API

Open:

- `http://localhost:8080/stats`
- `http://localhost:8080/flows`

You should see JSON.

## 2) Start the dashboard backend (Terminal 2)

```bash
cd /Users/kartikbobde/Desktop/Packet_analyzer-main/dashboard/backend
npm install
npm run dev
```

Verify:
- `http://localhost:3001/health`

## 3) Start the dashboard frontend (Terminal 3)

```bash
cd /Users/kartikbobde/Desktop/Packet_analyzer-main/dashboard/frontend
npm install
npm run dev
```

Open:
- `http://localhost:5173`

## Common issues

### “localhost refused to connect” on `:8080`

- The Java engine is not running, or it finished and exited.
- In PCAP mode, use `--serve` to keep the API alive.

### Dashboard shows API error

- Check backend is running: `http://localhost:3001/health`
- Check engine API is running: `http://localhost:8080/stats`

