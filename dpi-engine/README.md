# DPI Engine – Deep Packet Inspection (Java)

A production-style **Java Deep Packet Inspection (DPI) engine** that reads PCAP files or captures live traffic, parses layers 2–4, extracts TLS SNI and HTTP Host, tracks flows by 5-tuple, classifies traffic by application, and applies configurable blocking rules with multithreaded processing and analytics.

## Features

- **PCAP file reading** – Offline analysis using Pcap4J
- **Live packet capture** – Capture on interfaces (e.g. `eth0`, `wlan0`)
- **Protocol parsing** – Ethernet, IPv4, TCP, UDP
- **Flow tracking** – 5-tuple `(srcIP, dstIP, srcPort, dstPort, protocol)` with `ConcurrentHashMap`
- **TLS SNI extraction** – Server Name Indication from Client Hello
- **HTTP Host extraction** – Host header from plain HTTP
- **Application classification** – Domain → app (YouTube, Google, Facebook, DNS, etc.)
- **Rule engine** – Block by domain, IP, app, or port; JSON config and dynamic reload
- **Multithreading** – `BlockingQueue` + `ExecutorService` worker pool
- **Analytics** – Packet/byte counts, forward/drop, rate, top apps and domains
- **Performance benchmarking** – `PerformanceMonitor` prints every 5 seconds (packets/sec, drops, latency, flows)
- **Embedded REST API** – `StatsServer` exposes `/stats` and `/flows` for dashboards
- **Logging** – SLF4J (e.g. classification, flow creation, blocks)

## Architecture

```
                    +------------------+
                    |  PCAP / Live     |
                    |  Capture         |
                    +--------+---------+
                             |
                             v
                    +------------------+
                    |  BlockingQueue    |
                    |  (RawPacket)      |
                    +--------+---------+
                             |
         +-------------------+-------------------+
         v                   v                   v
    +---------+         +---------+         +---------+
    | Worker 1|         | Worker 2|   ...  | Worker N|
    | Parse   |         | Parse   |         | Parse   |
    | Classify|         | Classify|         | Classify|
    | Rules   |         | Rules   |         | Rules   |
    +----+----+         +----+----+         +----+----+
         |                   |                   |
         +-------------------+-------------------+
                             |
                             v
                    +------------------+
                    |  FlowTracker     |
                    |  (ConcurrentHashMap)
                    +------------------+
                             |
                             v
                    +------------------+
                    |  ReportGenerator |
                    |  TrafficStats    |
                    +------------------+
```

**Packet flow:** Capture → Queue → Workers (parse → classify → rule check) → FlowTracker → Stats/Report.

## Dashboard + API (for MERN)

When the engine runs, it starts an embedded HTTP server on port **8080**:

- `GET http://localhost:8080/stats`
- `GET http://localhost:8080/flows`

These endpoints are consumed by the `dashboard/` app (Express proxy + React UI).

## Tech Stack

| Component    | Choice                |
|-------------|------------------------|
| Language    | Java 17+               |
| Capture     | Pcap4J                 |
| Config      | Jackson (JSON)         |
| Logging     | SLF4J + Logback        |
| Concurrency | ExecutorService, BlockingQueue, ConcurrentHashMap |
| Tests       | JUnit 5                |

## Project Structure

```
dpi-engine/
├── src/main/java/com/dpi/
│   ├── capture/       PacketCaptureService, LiveCaptureService
│   ├── parser/        PacketParser, EthernetParser, IPParser, TCPParser, UDPParser, Packet
│   ├── flow/          FiveTuple, Flow, FlowTracker
│   ├── classification/ AppType, SNIExtractor, HTTPHostExtractor, TrafficClassifier
│   ├── rules/         RuleManager, RuleConfig
│   ├── processing/    PacketProcessor, WorkerPool
│   ├── analytics/     TrafficStats, ReportGenerator
│   ├── util/           ByteUtils, HashUtils
│   └── Main.java
├── src/main/resources/
│   ├── rules.json     (example)
│   └── logback.xml
├── src/test/java/     JUnit 5 tests (parser, SNI, rules, flow)
├── rules.json         (default rules at project root)
└── pom.xml
```

## Build

```bash
cd dpi-engine
./mvnw clean package
```

## Run

**PCAP file (with default `rules.json`):**

```bash
./run.sh --pcap path/to/capture.pcap
```

**PCAP with custom rules and packet limit:**

```bash
./run.sh --pcap capture.pcap --rules rules.json --limit 10000
```

**Live capture (e.g. `eth0` or `wlan0`):**

```bash
sudo ./run.sh --live eth0 --rules rules.json
```

**Help:**

```bash
./run.sh --help
```

## Run the MERN Dashboard

### 1) Start the DPI engine

From `dpi-engine/`:

```bash
./run.sh --pcap ../test_dpi.pcap --rules rules.json
```

Verify API:

- `http://localhost:8080/stats`
- `http://localhost:8080/flows`

### 2) Start dashboard backend (Express proxy)

```bash
cd ../dashboard/backend
npm install
npm run dev
```

### 3) Start dashboard frontend (React)

```bash
cd ../frontend
npm install
npm run dev
```

Open `http://localhost:5173`.

## Rules (rules.json)

Example:

```json
{
  "blocked_domains": ["youtube", "tiktok"],
  "blocked_ips": ["192.168.1.10"],
  "blocked_apps": ["FACEBOOK"],
  "blocked_ports": []
}
```

- **blocked_domains** – Substring match (e.g. `"youtube"` blocks `youtube.com`, `www.youtube.com`).
- **blocked_ips** – Exact source IPv4.
- **blocked_apps** – App type names (e.g. `FACEBOOK`, `YOUTUBE`).
- **blocked_ports** – Destination port numbers.

Reload by restarting with `--rules <file>` or by integrating a file watcher in your code.

## Performance Notes

- **Queue size** – Default 10,000; increase for bursty capture.
- **Workers** – Default 4; tune with CPU cores and I/O.
- **Flow table** – Default 100,000 flows; eviction by capacity and stale timeout (e.g. 5 min).
- **Metrics** – Report shows total packets, forwarded/dropped, and packets/sec from `TrafficStats`.

## Tests

```bash
./mvnw test
```

Covers:

- Packet parsing (Ethernet/IP/TCP, FiveTuple)
- TLS SNI extraction (Client Hello)
- Rule matching (domain, IP, app, port)
- Flow tracking (getOrCreate, reverse tuple, classify, block)

## License

Use as a portfolio or internal project as appropriate.
