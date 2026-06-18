# How to Run the DPI Engine – Step by Step

## Prerequisites

- **Java 17 or higher** (check with `java -version`)
- **Terminal** (macOS Terminal, Linux shell, or Windows Command Prompt / PowerShell)

You do **not** need to install Maven; the project includes the Maven Wrapper (`./mvnw`).

---

## Step 1: Open a terminal and go to the project folder

The project folder is **`dpi-engine`** (inside `Packet_analyzer-main`).

```bash
cd /Users/kartikbobde/Desktop/Packet_analyzer-main/dpi-engine
```

If your repo is elsewhere, use your path, for example:

```bash
cd /path/to/Packet_analyzer-main/dpi-engine
```

**Important:** All following commands must be run from inside this `dpi-engine` folder.

---

## Step 2: Build the project

Run:

```bash
./mvnw clean package
```

- On **Windows**, use: `mvnw.cmd clean package` (if you have `mvnw.cmd`), or run the same in Command Prompt.
- The first run may take 1–2 minutes (Maven will download dependencies).
- When you see **`BUILD SUCCESS`**, the build is done.

---

## Step 3: Run the application

Use the provided script (easiest):

```bash
./run.sh --help
```

You should see the usage message with all options.

### Option A – Analyze a PCAP file

If you have a `.pcap` file (e.g. `capture.pcap` in the current folder or elsewhere):

```bash
./run.sh --pcap capture.pcap
```

If you want the REST API (`/stats`, `/flows`) to stay up after the PCAP finishes (needed for the dashboard), add `--serve`:

```bash
./run.sh --pcap capture.pcap --serve
```

With a custom rules file:

```bash
./run.sh --pcap capture.pcap --rules rules.json
```

Limit how many packets to process (e.g. 5000):

```bash
./run.sh --pcap capture.pcap --limit 5000
```

### Option B – Live capture (advanced)

Capture from a network interface (often requires `sudo`):

```bash
sudo ./run.sh --live eth0
```

Replace `eth0` with your interface (e.g. `en0` on macOS, `wlan0` on Linux).

### Built-in API (for dashboards)

When the engine is running, it exposes:

- `http://localhost:8080/stats`
- `http://localhost:8080/flows`

If you ran PCAP mode without `--serve`, the program will exit quickly and the API will stop.

### Option C – Without the run script

You can use Maven directly:

```bash
./mvnw exec:java -Dexec.args="--help"
./mvnw exec:java -Dexec.args="--pcap capture.pcap"
```

---

## Step 4: Run the tests (optional)

From the same `dpi-engine` folder:

```bash
./mvnw test
```

You should see all tests pass and **`BUILD SUCCESS`**.

---

## Quick reference

| What you want to do   | Command |
|-----------------------|--------|
| Build                 | `./mvnw clean package` |
| Show help             | `./run.sh --help` |
| Analyze a PCAP file   | `./run.sh --pcap <path-to-file.pcap>` |
| Use custom rules      | `./run.sh --pcap file.pcap --rules rules.json` |
| Limit packets         | `./run.sh --pcap file.pcap --limit 5000` |
| Live capture          | `sudo ./run.sh --live eth0` |
| Keep API running      | `./run.sh --pcap file.pcap --serve` |
| Run tests             | `./mvnw test` |

---

## Troubleshooting

1. **`./mvnw: Permission denied`**  
   Make the script executable:  
   `chmod +x mvnw run.sh`

2. **`java: command not found`**  
   Install a JDK 17+ and add it to your PATH, or set `JAVA_HOME`.

3. **PCAP / live capture fails with a native library error**  
   Pcap4J uses native libpcap. On some systems (e.g. certain ARM Macs) you may need the correct native library for your OS/architecture.

4. **Rules file not found**  
   Ensure `rules.json` exists in `dpi-engine` (or pass the full path with `--rules /path/to/rules.json`).
