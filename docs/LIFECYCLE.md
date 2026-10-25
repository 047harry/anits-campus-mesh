# Software Engineering Lifecycle Roadmap: Campus Mesh Network
**Capstone Project Lifecycle & Execution Framework (Team 12 — ANITS)**

---

## 1. Lifecycle Overview (Stage-by-Stage Plan)

We will execute this project methodically, completing one stage completely before advancing to the next:

```
╔════════════════════════════════════════════════════════════════════════╗
║ 🟢 [COMPLETED] STAGE 1: REQUIREMENT ENGINEERING & FEASIBILITY ANALYSIS║
║ Formulate formal boundaries, personas, FRs, NFRs, and use cases.       ║
╚════════════════════════════════════════════════════════════════════════╝
                                   │
                                   ▼
╔════════════════════════════════════════════════════════════════════════╗
║ 🟢 [COMPLETED] STAGE 2: SYSTEM ARCHITECTURAL & PROTOCOL DESIGN         ║
║ Wire protocol, packet byte structure, state machine, and DB schemas.   ║
╚════════════════════════════════════════════════════════════════════════╝
                                   │
                                   ▼
╔════════════════════════════════════════════════════════════════════════╗
║ 🟢 [COMPLETED] STAGE 3: UI/UX & INTERACTION WORKFLOW DESIGN             ║
║ Disaster-mode wireframes, one-tap SOS UI, and Gold Badge noticeboard.  ║
╚════════════════════════════════════════════════════════════════════════╝
                                   │
                                   ▼
╔════════════════════════════════════════════════════════════════════════╗
║ 🟢 [COMPLETED] STAGE 4: MODULAR INCREMENTAL IMPLEMENTATION              ║
║ Sprints 1 to 5 (Backend -> SQLite -> Crypto -> BLE -> NAN -> UI).      ║
╚════════════════════════════════════════════════════════════════════════╝
                                   │
                                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│ 🔵 [ACTIVE] STAGE 5: SYSTEM INTEGRATION, VERIFICATION & BENCHMARKS     │
│ Physical multi-phone corridor tests, PDR, latency, and battery drain.  │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │
                                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│ ⚪ [UPCOMING] STAGE 6: ACADEMIC DEFENSE & FINAL DOCUMENTATION          │
│ B.Tech capstone thesis, IEEE conference manuscript, and viva defense.  │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Granular Stage Breakdown

### 🟢 STAGE 1: Requirement Engineering & Feasibility Analysis [COMPLETED]
*Goal: Formulate the formal Software Requirements Specification (SRS) grounded in ANITS campus reality.*
* **Step 1.1: Operational Environment & Hardware Boundaries**
  * ANITS campus physical footprint (~12 acres, 5–6 floor blocks, 10–15 ft floor heights, brick masonry & RCC attenuation).
  * Node density sizing (~6,000 students across 8–10 branches + ~500 faculty/staff).
  * Hardware constraints: Android 10+ (BLE 5.0+ Peripheral Mode + Wi-Fi Aware NAN), offline SQLite, zero-cellular operation.
* **Step 1.2: Stakeholder Personas & Role Hierarchy Matrix**
  * Formalize roles, access levels, and cryptographic permissions:
    * *Tier 0:* Campus System Admin (Root CA, directory management).
    * *Tier 1:* Institutional Authority (Principal, HODs, Wardens — verified Gold Badge broadcasts).
    * *Tier 2:* Emergency & Medical Responders (Campus Health Clinic, NSS volunteers — SOS triage).
    * *Tier 3:* Student & Staff Occupants (Peer messaging, routing relays, one-tap SOS).
* **Step 1.3: Formal Functional Requirements Catalog (FR-01 to FR-15)**
  * Discrete software capabilities across 4 categories:
    * Category A: Neighbor Discovery & Dual-Radio Management.
    * Category B: Controlled Routing, Hop-to-Hop Relaying & Loop/Storm Prevention.
    * Category C: Role-Based QoS & Emergency SOS Preemption.
    * Category D: Cloud Sync & Asymmetric Cryptographic Trust (PKI).
* **Step 1.4: Non-Functional Requirements & Performance Benchmarks (NFR-01 to NFR-10)**
  * Quantifiable engineering thresholds:
    * Battery consumption limit ($\le 3.5\%$ per hour in standby mesh discovery).
    * Single-hop latency ($< 1.5$s) and multi-hop SOS delivery delay ($< 8.0$s within 4 hops).
    * Packet Delivery Ratio (PDR $\ge 90\%$ for SOS beacons; $\ge 75\%$ for multi-hop standard messages).
    * Broadcast storm reduction ($> 65\%$ suppression vs. naive flooding).
    * Cryptographic verification overhead ($< 5$ ms per message).
* **Step 1.5: System Use Cases & Crisis Workflow Scenarios**
  * Formal sequence diagrams and user interaction specifications for:
    * Medical SOS beacon broadcast and responder ACK return path.
    * Official faculty evacuation directive broadcast and offline Gold Badge verification.
    * Zero-knowledge 1-to-1 encrypted private chat relay.

---

### 🟢 STAGE 2: System Architectural & Protocol Design [COMPLETED]
*Goal: Create the formal technical blueprints, protocol rules, and database specifications.*
* **Step 2.1: Packet Wire Frame Specification**
  * Exact byte layout of packet headers, role tags, sequence numbers, TTL, signatures, and payloads.
* **Step 2.2: Dual-Radio Finite State Machine (FSM)**
  * State transitions governing BLE passive discovery vs. Wi-Fi Aware (NAN) high-throughput data path escalation.
* **Step 2.3: Routing Daemon & Storm Suppression Specification**
  * Rolling Message ID cache, sequence counter tracking, and Bloom filter deduplication algorithms.
* **Step 2.4: Database & Cloud Sync Schema Design**
  * SQLite / Room DB schema (Peers, Messages, Bundles, Keys) and Cloud Firestore directory schema.

---

### 🟢 STAGE 3: UI/UX & Interaction Workflow Design [COMPLETED]
*Goal: Design the human interaction model optimized for disaster and crisis environments.*
* **Step 3.1: Disaster Ergonomics & Dark Crisis Mode**
  * High-contrast UI readable in dark, power-cut environments with minimal battery drain.
* **Step 3.2: One-Tap SOS & Medical Triage Dialogs**
  * Rapid emergency trigger flows requiring $< 2$ taps under stress.
* **Step 3.3: Official Authority Noticeboard & Offline Campus Map**
  * Gold Shield verification visual indicators and offline vector map with designated muster zones.

---

### 🟢 STAGE 4: Modular Incremental Implementation [COMPLETED]
*Goal: Build the software bottom-up following Agile / Iterative sprints.*
* **🟢 Sprint 1: Backend Infrastructure & Wire Protocol [COMPLETED]**
  * Binary wire frame serializer (70B Header + CRC-32) and domain models in campus-mesh-android.
* **🟢 Sprint 2: Local Database & Cryptography Engine [COMPLETED]**
  * Embedded SQLite (Room DB with 4 entities/DAOs) and Ed25519 asymmetric cryptography.
* **🟢 Sprint 3: Dual-Radio Background Services [COMPLETED]**
  * Duty-cycled BLE Scanner/Advertiser, Wi-Fi Aware (NAN) session controller, and Foreground Service.
* **🟢 Sprint 4: Routing Daemon & 3-Tier QoS Engine [COMPLETED]**
  * Preemption queue (SOS > Faculty > Chat), TTL hop decrement, storm barrier, and closed-loop ACK manager.
* **🟢 Sprint 5: UI/UX & Engine Integration [COMPLETED]**
  * Connected Compose disaster UI screens to ViewModels, LiveData/StateFlow, and background mesh daemons.

---

### 🔵 STAGE 5: System Integration, Verification & Benchmarking [ACTIVE]
*Goal: Empirical evaluation to demonstrate engineering correctness to the review panel.*
* **Step 5.1: Physical Multi-Hop Corridor Field Trials**
  * 3+ device physical verification across ANITS CSE block corridors and stairwells.
* **Step 5.2: Packet Delivery Ratio (PDR) & Latency Measurements**
  * Benchmarking delivery success and end-to-end delay across 1 to 5 hops.
* **Step 5.3: Broadcast Storm & Collision Stress Tests**
  * Evaluating duplicate drop efficiency compared to naive flooding.
* **Step 5.4: Battery Consumption Profiling**
  * Profiling battery drain per hour using Android Battery Historian.

---

### ⚪ STAGE 6: Academic Defense & Final Documentation
*Goal: Package all engineering artifacts for institutional review and academic publication.*
* **Step 6.1: Capstone Project Thesis / Dissertation**
  * Comprehensive final dissertation structured per university guidelines.
* **Step 6.2: Research Paper Publication Draft**
  * IEEE conference formatted manuscript detailing architecture and benchmarks.
* **Step 6.3: Final Viva Review & Live Demonstration**
  * Slide deck and live multi-phone demo setup for external examiners.
