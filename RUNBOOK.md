# RUNBOOK – DPI Engine + MERN Dashboard (Step-by-step)

This is the single “source of truth” for running the full project:

- **Java DPI engine** (PCAP + live capture)  
- **Embedded engine API** (`/stats`, `/flows`)  
- **Dashboard backend** (Express proxy)  
- **Dashboard frontend** (React monitoring UI)  

---

## What to run from where

Repository root:

```bash
cd /Users/kartikbobde/Desktop/Packet_analyzer-main
```

- Java engine project: `dpi-engine/`
- Dashboard project: `dashboard/`

---

## Ports used

- **8080**: Java engine API (`/stats`, `/flows`)
- **3001**: Dashboard backend (Express proxy `/api/*`)
- **5173**: Dashboard frontend (React UI)

If any of these are already in use, the dashboard won’t open. See **Port conflicts** below.

---

## 0) Prerequisites

- Java **17+**: `java -version`
- Node.js **18+**: `node -v`

---

## 1) Start the Java DPI engine (Terminal 1)

### Option A: PCAP mode (recommended)

This keeps the API running after the PCAP completes (so the dashboard can keep polling):

```bash
cd /Users/kartikbobde/Desktop/Packet_analyzer-main/dpi-engine
./mvnw clean package
./run.sh --pcap "../test_dpi.pcap" --rules rules.json --serve
```

### Option B: Live capture mode

Runs continuously until you press **Ctrl+C**:

```bash
cd /Users/kartikbobde/Desktop/Packet_analyzer-main/dpi-engine
sudo ./run.sh --live en0 --rules rules.json
```

---

## 2) Verify the engine API (browser)

Open these:

- `http://localhost:8080/stats`
- `http://localhost:8080/flows`

You should see JSON.  
If you see “refused to connect”, the engine isn’t running or port 8080 is blocked.

---

## 3) Start dashboard backend (Terminal 2)

```bash
cd /Users/kartikbobde/Desktop/Packet_analyzer-main/dashboard/backend
npm install
npm run dev
```

Verify:

- `http://localhost:3001/health`

---

## 4) Start dashboard frontend (Terminal 3)

```bash
cd /Users/kartikbobde/Desktop/Packet_analyzer-main/dashboard/frontend
npm install
npm run dev
```

Open:

- `http://localhost:5173`

---

## 5) Verification checklist (fast)

Run these:

```bash
curl -s http://localhost:8080/stats | head
curl -s http://localhost:3001/health
curl -s http://localhost:3001/api/stats | head
```

Expected:
- 8080 returns JSON stats
- 3001/health returns `{"ok":true}`
- 3001/api/stats returns the engine stats JSON

---

## Port conflicts (most common cause of “dashboard not opening”)

### See what’s using the ports

```bash
lsof -nP -iTCP:8080 -sTCP:LISTEN
lsof -nP -iTCP:3001 -sTCP:LISTEN
lsof -nP -iTCP:5173 -sTCP:LISTEN
```

### Kill the process (replace PID)

```bash
kill -9 <PID>
```

Then restart the engine/backend/frontend.

---

## Troubleshooting

### “Address already in use” (8080 or 3001)

Free the port using the steps above, then restart.

### Dashboard shows API error banner

- Ensure engine API works: `http://localhost:8080/stats`
- Ensure backend works: `http://localhost:3001/health`
- Ensure proxy works: `http://localhost:3001/api/stats`

### “Permission denied” for scripts

```bash
cd /Users/kartikbobde/Desktop/Packet_analyzer-main/dpi-engine
chmod +x mvnw run.sh
```

