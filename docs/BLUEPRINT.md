# MASTER PROJECT BLUEPRINT: DECENTRALIZED CAMPUS MESH NETWORK
**A Disaster-Resilient Peer-to-Peer Communication Architecture with Hybrid Cryptographic Trust and Role-Based QoS**

---

### Project Metadata
* **Institution:** Anil Neerukonda Institute of Technology & Sciences (ANITS), Sangivalasa, Visakhapatnam
* **Department:** Department of Computer Science & Engineering
* **Academic Year:** 2025–2026 (Final Year B.Tech Capstone Project)
* **Team ID:** Team 12 (CSD-B)
* **Team Members:**
  * **P. Darakesu Hemanth (Lead)** — Reg No: `A23126551113`
  * **Ruthvik Katcha** — Reg No: `A23126551119`
  * **Kusa Madhuri** — Reg No: `A23126551098`
  * **Garbhapu Sirisha** — Reg No: `A23126551084`
* **Internal Guide:** Mrs. A. Bhagya Lakshmi, Assistant Professor, Dept. of CSE
* **Development Machine:** HP Victus Development Workstation

---

## 1. Executive Summary & Academic Abstract

This project presents the design and implementation of a decentralized, offline-first mesh network application tailored for university campus environments. Leveraging **Bluetooth Low Energy (BLE)** and **Wi-Fi Aware (NAN)**, the system enables multi-hop, peer-to-peer communication without relying on active cellular base stations (4G/5G) or external campus Wi-Fi infrastructure.

To ensure authenticity and security, the architecture integrates a **centralized cloud backend (Firebase)** that periodically synchronizes public encryption keys and role-based access directories to local device caches (**embedded SQLite**) during normal online operations. During offline crisis operations or complete grid collapses (e.g., severe coastal cyclones), the network switches autonomously to a **role-based packet forwarding protocol** that strictly prioritizes critical communications—such as emergency SOS distress beacons and official faculty evacuation directives—over routine student traffic. 

Furthermore, the system incorporates **end-to-end asymmetric encryption (ECC/Ed25519)** for secure 1-to-1 private messaging (zero-knowledge relaying) and employs a **unique message sequence and rolling ID cache** to eliminate broadcast storms and infinite routing loops. The proposed solution offers a resilient, scalable, and zero-cost communication infrastructure, ensuring uninterrupted campus connectivity during both normal daily operations and catastrophic power outages.

---

## 2. System Architecture & Infrastructure

```
┌────────────────────────────────────────────────────────────────────────┐
│                      CENTRALIZED CLOUD BACKEND                         │
│   (Firebase Auth • Cloud Firestore User Directory • Cloud Functions)   │
│   • Manages Student/Faculty Registration & Official Campus Roles       │
│   • Acts as Root PKI: Publishes and signs official Public Keys         │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │ (Periodic Background Sync via Campus Wi-Fi/4G)
                                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        EDGE CLIENT (MOBILE APP)                        │
│                                                                        │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │ Minimal Low-Cognitive UI (SOS Trigger • Bulletins • 1:1 Chat)  │   │
│   └──────────────────────────────┬─────────────────────────────────┘   │
│                                  │                                     │
│   ┌──────────────────────────────▼─────────────────────────────────┐   │
│   │ Background Sync Worker (Updates local SQLite cache when online)│   │
│   └──────────────────────────────┬─────────────────────────────────┘   │
│                                  │                                     │
│   ┌──────────────────────────────▼─────────────────────────────────┐   │
│   │ Local Embedded SQLite Cache                                    │   │
│   │ • User Roles Table • Public Key Ring • Seen Message ID Cache   │   │
│   └──────────────────────────────┬─────────────────────────────────┘   │
│                                  │                                     │
│   ┌──────────────────────────────▼─────────────────────────────────┐   │
│   │ Cryptography Engine (Asymmetric ECC/Ed25519)                   │   │
│   │ • Encrypts 1:1 Chats with Peer PubKey (Zero-Knowledge Relaying)│   │
│   │ • Verifies Official Faculty Signatures Offline                 │   │
│   └──────────────────────────────┬─────────────────────────────────┘   │
│                                  │                                     │
│   ┌──────────────────────────────▼─────────────────────────────────┐   │
│   │ Role-Based QoS Engine                                          │   │
│   │ Priority 1: SOS (Preempts all)                                 │   │
│   │ Priority 2: Faculty Announcements (Preempts chat)              │   │
│   │ Priority 3: Routine Peer-to-Peer Chat (FIFO)                   │   │
│   └──────────────────────────────┬─────────────────────────────────┘   │
│                                  │                                     │
│   ┌──────────────────────────────▼─────────────────────────────────┐   │
│   │ Loop Prevention & Hop-to-Hop Routing Engine                    │   │
│   │ • UUID + Sequence Counter Deduplication Cache                  │   │
│   │ • TTL Decrement & Forward Logic                                │   │
│   └──────────────────────────────┬─────────────────────────────────┘   │
│                                  │                                     │
│   ┌──────────────────────────────▼─────────────────────────────────┐   │
│   │ Dual-Radio Hardware Layer                                      │   │
│   │ • BLE: Continuous background neighbor discovery & beaconing    │   │
│   │ • Wi-Fi Aware (NAN): High-speed direct payload socket channels │   │
│   └────────────────────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────────────────────┘
```

### 2.1 Backend Master Directory (Cloud Server)
The backend does **not** route real-time messages; rather, it acts as the centralized single source of truth for identity and cryptographic trust:
* **Authentication Engine:** Validates student, faculty, and administrative credentials via university email/roll numbers.
* **Role Management:** Maintains the master directory of user roles and network permissions (Admin, Principal, HOD, Faculty, Class Teacher, Responder, Student).
* **Public Key Infrastructure (PKI):** Stores and distributes public encryption keys for all registered campus users.

### 2.2 Edge Client (Mobile Application)
The mobile application operates as both an end-user communication interface and an autonomous multi-hop router (node):
* **Background Sync Engine:** Executes whenever internet or campus Wi-Fi connectivity is detected, silently fetching the latest role directory and public keys into local SQLite.
* **Hardware Interface Layer:** Manages BLE and Wi-Fi Aware transceivers for continuous scanning, advertising, and high-throughput payload sockets.
* **Minimal User Interface:** Designed with low cognitive load for high-stress disaster scenarios (instant one-tap SOS, gold-shield official bulletins, private peer chats).

---

## 3. Campus Operational Environment & Ground Reality (ANITS Context)

### 3.1 Campus Physical Profile
* **Geographical Footprint:** ~12 acres (~48,500 m²) along NH-16, Sangivalasa, Visakhapatnam (cyclone-prone coastal region).
* **Departments & Blocks:** CSE, CSD, CSM, CIC, IT, ECE, EEE, Mechanical, Civil, Chemical.
* **Building Architecture:** 5 to 6 floors per departmental block; floor heights of 10 to 15 feet (~3.0 to 4.5 meters).
* **Construction Material:** Brick masonry walls with reinforced cement concrete (RCC) floor slabs.
* **Inter-Block Distances:**
  * Adjacent neighboring blocks: **20 to 40 meters** across open courtyards.
  * Extremities (Blocks to Hostels / Grounds): **150 to 300 meters**.

### 3.2 Network Scale & Node Sizing
* **Undergraduate Cohort:** 4 Years $\times$ 3–5 Sections $\times$ ~65 Students $\approx$ **1,000–1,200 students per department block**.
* **Active Nodes:** **~5,500 to 6,500 student smartphones** + **~450 to 550 faculty/staff nodes** = **~6,000 to 7,000 potential nodes**.

### 3.3 RF Path Loss & Indoor/Outdoor Propagation Insights
* **Vertical Slab Isolation:** RCC slabs attenuate signals by 15–25 dB per floor. Vertical propagation naturally travels via **open stairwells** and **exterior window reflections**.
* **Inter-Block Line-of-Sight (LoS):** Because adjacent blocks are only 20–40 meters apart, inter-building window-to-window links are often clearer and faster than penetrating 4 internal concrete walls.
* **Human Mobility ("Data Mules"):** Moving students carry stored message bundles between dead zones, bridging disconnected campus sectors autonomously.

---

## 4. Core Networking Mechanics

### 4.1 Hop-to-Hop Relaying
During offline operation, standard student smartphones act as packet forwarders:
* An intermediate device receives a data packet via BLE or Wi-Fi Aware and re-transmits it to nearby nodes within its radio sphere.
* By chaining hops, data dynamically traverses corridors, staircases, and open grounds to cover the entire campus footprint.

### 4.2 Infinite Loop Prevention & Broadcast Storm Mitigation
To prevent messages from bouncing infinitely between nodes and causing RF channel collapse (>70% packet drops in civilian apps):
* **Globally Unique Identifiers:** Every packet has a tuple: `(Originator_Node_ID, Message_UUID, Sequence_Number)`.
* **Rolling Cache (Bloom Filter / LRU):** Each node tracks seen Message IDs.
* **Drop Logic:** If `Message_ID` is present in local cache $\rightarrow$ **silently drop immediately**. If fresh $\rightarrow$ add to cache, process, decrement $\text{TTL} = \text{TTL} - 1$, and forward if $\text{TTL} > 0$.

### 4.3 Role-Based Quality of Service (QoS) & Prioritization
Nodes inspect the packet's **Role Tag** and prioritize outgoing transmission buffers:
1. **Priority 1 (Emergency SOS):** Bypasses all standard buffers. Forwarded immediately at maximum radio strength. Preempts all other traffic.
2. **Priority 2 (Faculty Announcements):** Official directives from Principal/HODs. Processed ahead of all casual student traffic.
3. **Priority 3 (Routine Student Traffic):** Standard 1-to-1 private messages and group chats processed in standard FIFO order.

---

## 5. Cryptography & Secure Messaging Architecture

* **1-to-1 Private Chat (Zero-Knowledge End-to-End Encryption):**
  * Sender drafts a message and encrypts the payload using the recipient's cached public key (ECC / Curve25519).
  * Encrypted ciphertext is relayed across intermediate hops.
  * Intermediate relay nodes see routing headers, but **cannot decrypt or read the payload**.
  * Only the destination device possessing the matching private key can decrypt the message.
* **Global Campus Broadcasts (Ed25519 Signatures):**
  * Directives from Principal or Wardens are digitally signed with their private key.
  * Receiving student devices verify the signature against the pre-loaded `master_pubkeys.db` **100% offline**.
  * Valid messages display a **Verified Gold Shield Badge**; fake/unverified messages are flagged to prevent panic and rumors.

---

## 6. End-to-End Student Journey: UI vs. Background Engine

```
┌───────────────────────────────────────────────┬───────────────────────────────────────────────┐
│ WHAT THE STUDENT SEES ON SCREEN (UI)          │ WHAT THE PHONE EXECUTES IN THE BACKGROUND     │
├───────────────────────────────────────────────┼───────────────────────────────────────────────┤
│ 1. Setup: Enters Roll No, Name, Branch.       │ Generates Ed25519 Keypair in Android Keystore;│
│    Sees "Campus Mesh Ready • Standby".        │ Syncs college Master Public Keys to SQLite DB.│
├───────────────────────────────────────────────┼───────────────────────────────────────────────┤
│ 2. Crisis Strikes: "No Service / Outage".     │ Senses network loss; starts BLE duty-cycle;   │
│    App switches to Dark Crisis Mode.          │ Discovers nearby classmates; logs RSSI to DB. │
├───────────────────────────────────────────────┼───────────────────────────────────────────────┤
│ 3. Faculty Order: Card with GOLD SHIELD BADGE │ Packet arrives via multi-hop; checks cache;   │
│    "Evacuate CSE Block to Main Ground".       │ Verifies Principal signature offline; relays. │
├───────────────────────────────────────────────┼───────────────────────────────────────────────┤
│ 4. Distress SOS: Taps Big Red "EMERGENCY SOS" │ Assembles SOS packet; signs; pushes to Tier 1;│
│    Selects "Injured"; shows "Broadcasting".   │ Escalates to Wi-Fi Aware NAN high-speed burst.│
├───────────────────────────────────────────────┼───────────────────────────────────────────────┤
│ 5. Walking as "Data Mule": Walks down stairs  │ Store-Carry-Forward: Holds bundle in SQLite;  │
│    Phone stays in pocket with screen off.     │ Detects Responder node in lawn; auto-transfers│
├───────────────────────────────────────────────┼───────────────────────────────────────────────┤
│ 6. Rescue Confirmation: Green banner appears: │ Reverse ACK packet received; matches SOS ID;  │
│    "SOS Acknowledged • Medical Team En Route" │ Silences distress alert; updates local log.   │
└───────────────────────────────────────────────┴───────────────────────────────────────────────┘
```

---

## 7. Master Engineering Lifecycle Roadmap (6 Stages)

```
STAGE 1: REQUIREMENT ENGINEERING & FEASIBILITY ANALYSIS (Current Phase)
  ├── 1.1 Physical Environment & RF Propagation Modeling (ANITS ~12 acres, 5-6 floors)
  ├── 1.2 Stakeholder Personas & Role Hierarchy Matrix (Admin, Authority, Responder, Student)
  ├── 1.3 Formal Functional Requirements (FR-01 to FR-15)
  ├── 1.4 Non-Functional Requirements & Performance Thresholds (Battery <3.5%/hr, Latency <1.5s)
  └── 1.5 System Use Cases & Crisis Workflow Sequence Diagrams
STAGE 2: SYSTEM ARCHITECTURAL & PROTOCOL DESIGN
  ├── 2.1 Packet Wire Frame Specification (Byte layout, Headers, Payloads, Signatures)
  ├── 2.2 Dual-Radio State Machine (BLE discovery ➔ Wi-Fi Aware NAN escalation)
  ├── 2.3 Bloom Filter & Rolling Message Cache Specification
  └── 2.4 SQLite Schema & Cloud Firestore Sync Schema Design
STAGE 3: UI/UX & INTERACTION WORKFLOW DESIGN
  ├── 3.1 Low-Cognitive Load Wireframes & High-Contrast Disaster UI
  ├── 3.2 One-Tap SOS Screen & Medical Triage Dialogs
  └── 3.3 Gold Badge Verification Noticeboard & Offline Campus Vector Map
STAGE 4: MODULAR INCREMENTAL IMPLEMENTATION
  ├── Sprint 1: Backend Setup (Firebase Auth, Firestore Directory, Cloud Sync Worker)
  ├── Sprint 2: Embedded SQLite DB & Local Cryptography Engine (Ed25519)
  ├── Sprint 3: BLE Discovery & Beaconing Service
  ├── Sprint 4: Wi-Fi Aware (NAN) Socket Cluster Channel
  ├── Sprint 5: Routing Daemon, QoS Queues, and Message ID Rolling Cache
  └── Sprint 6: Mobile Frontend & Engine Integration
STAGE 5: SYSTEM INTEGRATION, VERIFICATION & BENCHMARKING
  ├── 5.1 Physical 3-Node Corridor Multi-Hop Tests (CSE Block)
  ├── 5.2 Packet Delivery Ratio (PDR) & Latency Measurements
  └── 5.3 Battery Drain Profiling via Android Battery Historian
STAGE 6: ACADEMIC DEFENSE & DOCUMENTATION
  ├── 6.1 Capstone Project Thesis / Dissertation
  ├── 6.2 IEEE Conference Manuscript Draft
  └── 6.3 Final Review Demonstration & Defense Slide Deck
```
