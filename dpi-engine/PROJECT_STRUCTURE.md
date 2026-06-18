# DPI Engine – Project structure and how to run

## Which folder is the actual project?

**The Java project you run is:**  
**`dpi-engine`** (inside `Packet_analyzer-main`)

```
Packet_analyzer-main/          ← repo root (original C++ + Java)
├── dpi-engine/               ← **THIS IS THE JAVA PROJECT – run from here**
│   ├── pom.xml
│   ├── rules.json
│   ├── README.md
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/dpi/   ... all Java sources
│   │   │   └── resources/      ... logback.xml, rules.json
│   │   └── test/
│   │       └── java/com/dpi/   ... JUnit tests
│   └── target/                (created after build)
├── src/                      ← original C++ (CMake)
├── include/
└── ...
```

So:
- **Root of the repo:** `Packet_analyzer-main/` (mixed C++ and Java).
- **Root of the Java app:** `Packet_analyzer-main/dpi-engine/`. All Maven/Java commands must be run from **`dpi-engine`**.

---

## Directory layout (inside `dpi-engine/`)

```
dpi-engine/
├── pom.xml                          # Maven build and dependencies
├── rules.json                       # Blocking rules (used by default)
├── README.md
├── PROJECT_STRUCTURE.md             # This file
│
├── src/main/java/com/dpi/
│   ├── Main.java                    # Entry point (CLI)
│   ├── capture/
│   │   ├── PacketCaptureService.java   # Read PCAP files
│   │   └── LiveCaptureService.java     # Live interface capture
│   ├── parser/
│   │   ├── Packet.java
│   │   ├── PacketParser.java
│   │   ├── EthernetParser.java
│   │   ├── IPParser.java
│   │   ├── TCPParser.java
│   │   └── UDPParser.java
│   ├── flow/
│   │   ├── FiveTuple.java
│   │   ├── Flow.java
│   │   └── FlowTracker.java
│   ├── classification/
│   │   ├── AppType.java
│   │   ├── SNIExtractor.java
│   │   ├── HTTPHostExtractor.java
│   │   └── TrafficClassifier.java
│   ├── rules/
│   │   ├── RuleConfig.java
│   │   └── RuleManager.java
│   ├── processing/
│   │   ├── PacketProcessor.java
│   │   └── WorkerPool.java
│   ├── analytics/
│   │   ├── TrafficStats.java
│   │   └── ReportGenerator.java
│   └── util/
│       ├── ByteUtils.java
│       └── HashUtils.java
│
├── src/main/resources/
│   ├── logback.xml                  # Logging config
│   └── rules.json                   # Example rules
│
└── src/test/java/com/dpi/
    ├── parser/PacketParserTest.java
    ├── classification/SNIExtractorTest.java
    ├── rules/RuleManagerTest.java
    └── flow/FlowTrackerTest.java
```

---

## How to run

All commands are run from **`dpi-engine`**:

```bash
cd /path/to/Packet_analyzer-main/dpi-engine
```

### 1. Build (requires Maven)

```bash
mvn clean package
```

If `mvn` is not installed:

- **macOS:** `brew install maven`
- **Ubuntu/Debian:** `sudo apt install maven`
- Then open a **new terminal** so `mvn` is on your PATH, `cd` to `dpi-engine`, and run the commands above.

### 2. Run with a PCAP file

```bash
java -jar target/dpi-engine-1.0.0-SNAPSHOT.jar --pcap /path/to/capture.pcap
```

With custom rules:

```bash
java -jar target/dpi-engine-1.0.0-SNAPSHOT.jar --pcap capture.pcap --rules rules.json
```

### 3. Run live capture (often needs sudo)

```bash
sudo java -jar target/dpi-engine-1.0.0-SNAPSHOT.jar --live eth0
```

### 4. Run tests

```bash
mvn test
# or
./mvnw test
```

### 5. Help

```bash
java -jar target/dpi-engine-1.0.0-SNAPSHOT.jar --help
```

---

## Summary

| What              | Where / Command |
|-------------------|-----------------|
| **Actual project folder** | `Packet_analyzer-main/dpi-engine/` |
| **Build**         | From `dpi-engine`: `mvn clean package` (or `./mvnw`) |
| **Run**           | `java -jar target/dpi-engine-1.0.0-SNAPSHOT.jar --pcap <file>` |
| **Tests**         | From `dpi-engine`: `mvn test` |
