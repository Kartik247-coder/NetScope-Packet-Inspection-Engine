#!/bin/sh
# Run DPI engine (uses Maven classpath). From dpi-engine folder: ./run.sh [args]
# Example: ./run.sh --help
# Example: ./run.sh --pcap /path/to/capture.pcap
cd "$(dirname "$0")"
./mvnw -q exec:java -Dexec.args="$*"
