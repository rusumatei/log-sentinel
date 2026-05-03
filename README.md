# LogSentinel: Advanced SSH Brute-Force Detection & Response

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java Version](https://img.shields.io/badge/Java-17%2B-blue.svg)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Version](https://img.shields.io/badge/Version-6.0.0-green.svg)](#)

**LogSentinel** is a high-performance, stream-based security monitoring tool designed to detect, visualize, and respond to SSH brute-force attacks in real-time. Originally conceived as an educational tool for log analysis, it has evolved into a comprehensive security suite featuring GeoIP enrichment, live monitoring, and persistent incident tracking.

---

## 🚀 Evolution: From V1 to V6

LogSentinel's development followed a rigorous roadmap, maturing through six major milestones:

- **v1.0.0 (Core Engine):** Implemented stream-based `sshd` log parsing and the signature sliding-window detection algorithm.
- **v2.0.0 (Live Monitoring):** Added real-time log tailing capabilities for active threat detection.
- **v3.0.0 (GeoIP Integration):** Integrated MaxMind GeoIP for geographic enrichment of attacker IPs.
- **v4.0.0 (Visualization):** Launched a web-based dashboard for real-time threat visualization and heatmaps.
- **v5.0.0 (Persistence):** Integrated H2 Database for long-term storage and historical incident analysis.
- **v6.0.0 (Enterprise Hardening):** Added automated remediation logging, security auditing, and performance optimization.

---

## ✨ Key Features

- **Sliding Window Detection:** Advanced temporal logic to differentiate between sporadic failures and coordinated high-velocity attacks.
- **Real-Time Analysis:** Monitor logs as they happen with low-latency event processing.
- **Threat Intelligence:** Automatic lookup of Source IP geography to identify attack origins.
- **Interactive Dashboard:** Modern web interface to monitor active threats and historical trends.
- **Persistence Layer:** All incidents are recorded for forensic analysis and reporting.
- **Security Auditing:** Generates formal security reports and remediation logs for compliance.

---

## 🏗️ Architecture

LogSentinel is built with a modular, decoupled architecture to ensure maintainability and scalability:

1.  **Ingestion Layer:** Handles file streaming and real-time monitoring.
2.  **Parsing Engine:** Regex-based extraction of security events.
3.  **Detection Engine:** State-managed sliding window logic.
4.  **Enrichment Service:** GeoIP and threat intelligence integration.
5.  **Persistence Layer:** H2 JPA/Hibernate storage.
6.  **Presentation Layer:** Spring Boot Web/Thymeleaf dashboard.

---

## 🛠️ Quick Start

### Prerequisites
- Java 17 or higher
- Gradle 8.x

### 1. Clone & Build
```bash
git clone https://github.com/your-repo/LogSentinel.git
cd LogSentinel
./gradlew build
```

### 2. Configure
Adjust `src/main/resources/application.properties` or provide a `config.toml` for custom thresholds.

### 3. Run
```bash
java -jar build/libs/LogSentinel-6.0.0.jar --file /var/log/auth.log --threshold 5 --window 2m
```

---

## 📊 Dashboard

Access the live monitoring dashboard at `http://localhost:8080` once the application is running. 

*Features include:*
- Active attack counters
- IP hit list with GeoIP data
- Historical trend graphs

---

## ⚖️ License

Distributed under the MIT License. See `LICENSE` for more information.

---

**LogSentinel** — *Turning logs into actionable intelligence.*
