# Git Workflow & Version Control Standards

**Author:** David (Senior DevOps Engineer & Release Manager)
**Project:** LogSentinel
**Philosophy:** Git history is a public ledger of engineering discipline. We do not just "save" code; we document the evolution of a system.

---

## 1. Commit Message Standard: Conventional Commits

We strictly adhere to the [Conventional Commits specification](https://www.conventionalcommits.org/). This ensures the history is machine-readable and human-navigable.

### Format
`<type>: <description>`

### Mandatory Types
- **feat:** A new functional requirement (e.g., `feat: implement GeoIP lookup engine`)
- **fix:** A bug fix (e.g., `fix: resolve sliding window boundary off-by-one`)
- **docs:** Documentation only changes (e.g., `docs: update deployment guide`)
- **style:** Changes that do not affect the meaning of the code (white-space, formatting, missing semi-colons, etc)
- **refactor:** A code change that neither fixes a bug nor adds a feature
- **perf:** A code change that improves performance
- **test:** Adding missing tests or correcting existing tests
- **chore:** Changes to the build process or auxiliary tools and libraries such as documentation generation

---

## 2. Branching Strategy: Trunk-Based Development (TBD)

For LogSentinel, we employ **Trunk-Based Development**. 

- **Main Branch:** The `main` branch is the source of truth. It is always in a deployable state.
- **Short-Lived Features:** If a feature takes more than a few hours, use a short-lived branch (`feat/feature-name`) and merge back to `main` via a Pull Request (PR) immediately.
- **No Long-Lived Branches:** We do not use `develop` or long-running release branches. 
- **Tags:** Releases are marked with semantic versioning tags (e.g., `v1.0.0`, `v2.0.0`).

---

## 3. Initialization & Historical Simulation Plan

To prepare LogSentinel for public release, we will simulate the project's evolution. This provides a clean, professional narrative for stakeholders.

### Step 1: Initialize Repository
```bash
git init
git add .gitignore .gitattributes
git commit -m "chore: initial repository structure and configuration"
```

### Step 2: Milestone V1 - Core Engine
*Focus: Log parsing, sliding window detection, and basic reporting.*
```bash
git add src/main/java/ubb/bmad/logsentinel/core/ # Or relevant V1 files
git commit -m "feat: implement core log ingestion and sliding window detection (V1)"
git tag -a v1.0.0 -m "Release V1.0.0: Core LogSentinel Engine"
```

### Step 3: Milestone V2 - Live Monitoring
*Focus: Real-time file tailing and active monitoring.*
```bash
git add src/main/java/ubb/bmad/logsentinel/monitor/
git commit -m "feat: add live log monitoring and real-time alerts (V2)"
git tag -a v2.0.0 -m "Release V2.0.0: Live Monitoring"
```

### Step 4: Milestone V3 - GeoIP & Threat Intel
*Focus: Geographic enrichment of detected IPs.*
```bash
git add src/main/java/ubb/bmad/logsentinel/geoip/ GeoLite2-City.mmdb
git commit -m "feat: integrate GeoIP lookup for geographic threat enrichment (V3)"
git tag -a v3.0.0 -m "Release V3.0.0: GeoIP Integration"
```

### Step 5: Milestone V4 - Dashboard & Visualization
*Focus: Visual representation of attack patterns.*
```bash
git add src/main/resources/static/ src/main/resources/templates/
git commit -m "feat: implement web-based dashboard for threat visualization (V4)"
git tag -a v4.0.0 -m "Release V4.0.0: Visualization Dashboard"
```

### Step 6: Milestone V5 - Persistence & Historical Analysis
*Focus: H2 Database integration for long-term storage.*
```bash
git add data/ src/main/resources/application.properties
git commit -m "feat: implement H2 persistence for historical incident analysis (V5)"
git tag -a v5.0.0 -m "Release v5.0.0: Persistence Layer"
```

### Step 7: Milestone V6 - Security Hardening & Incident Response
*Focus: Remediation logs, security audits, and performance tuning.*
```bash
git add _bmad-output/implementation-artifacts/
git commit -m "feat: add automated remediation logging and security audit artifacts (V6)"
git tag -a v6.0.0 -m "Release v6.0.0: Enterprise Hardening"
```

### Step 8: Finalize and Push
```bash
git remote add origin <your-github-repo-url>
git branch -M main
git push -u origin main --tags
```

---

## 4. Enforcement

Every engineer is responsible for maintaining this standard. Pull Requests that do not follow Conventional Commits or that introduce branch divergence will be rejected by the Release Manager. 

**Stability is not a feature; it is a prerequisite.**
