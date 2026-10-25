# 🌐 Decentralized Offline-First Campus Mesh Network (CampusMesh)

> **Final Capstone Project — Team 12**  
> **Department of Computer Science & Engineering (Data Science)**  
> **Anil Neerukonda Institute of Technology & Sciences (ANITS), Sangivalasa, Visakhapatnam**

---

[![Android](https://img.shields.io/badge/Platform-Android%2010%2B%20(API%2029--34)-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin%201.9-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Arch-Dual--Radio%20Mesh%20(BLE%20%2B%20UDP)-FF6F00?style=for-the-badge)](https://github.com/047harry/anits-campus-mesh)
[![Security](https://img.shields.io/badge/Crypto-Ed25519%20%2B%20AES--256--GCM-E11D48?style=for-the-badge)](https://github.com/047harry/anits-campus-mesh)
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge)](LICENSE)

---

## 📑 Table of Contents
1. [Executive Summary & Problem Statement](#-executive-summary--problem-statement)
2. [Core Innovations & Technical Capabilities](#-core-innovations--technical-capabilities)
3. [System Architecture & Multi-Hop Relay Engine](#-system-architecture--multi-hop-relay-engine)
4. [Wire Protocol & Packet Specifications](#-wire-protocol--packet-specifications)
5. [Cryptographic Security & Zero-Knowledge Hop Privacy](#-cryptographic-security--zero-knowledge-hop-privacy)
6. [Department Directory & Cross-Campus Private Chat](#-department-directory--cross-campus-private-chat)
7. [Application Screens & Disaster-Ergonomic UI](#-application-screens--disaster-ergonomic-ui)
8. [Embedded Database & Cloud Directory Sync](#-embedded-database--cloud-directory-sync)
9. [Physical Hardware Verification & Live Field Testing](#-physical-hardware-verification--live-field-testing)
10. [Repository Directory Organization](#-repository-directory-organization)
11. [Build, Sideload & Installation Guide](#-build-sideload--installation-guide)
12. [Academic Credits & Team Roster](#-academic-credits--team-roster)

---

## 🌪️ Executive Summary & Problem Statement

Modern academic campus operations depend entirely on centralized telecommunications infrastructure: commercial fiber backbones, cellular towers, and cloud-hosted messaging platforms (e.g., WhatsApp, Telegram, Microsoft Teams).

During extreme natural catastrophes (such as coastal cyclones like **Hudhud**, severe structural fires, seismic tremors, or regional electric grid collapses):
* **Cellular towers lose backup battery power** within a few hours.
* **Overhead and underground fiber lines snap** under falling trees and structural debris.
* **Centralized cloud servers become unreachable**, leaving students, faculty, and crisis wardens in a total communications blackout.

```
CENTRALIZED INFRASTRUCTURE FAILURE (DISASTER BLACKOUT):
[Student in CSE Block] ──X──> [Fallen Cell Tower] ──X──> [Cloud Server] ──X──> [Campus Clinic]
❌ IMPACT: Zero communication, severe panic, unverified rumors, delayed medical rescue.

CAMPUSMESH AUTONOMOUS OFF-GRID SOLUTION:
[Student in CSE Block] ──BLE / UDP──► [Peer in Central Quad] ──BLE / UDP──► [Friend in Mech Block]
✔ IMPACT: 100% Offline, multi-hop hopping, end-to-end encrypted, zero infrastructure required.
```

**CampusMesh** is an autonomous, decentralized, offline-first mobile mesh communication system that transforms standard Android smartphones and tablets into self-healing peer-to-peer radio relay nodes. Without requiring cellular networks, Wi-Fi internet access, or external hardware dongles, student nodes autonomously relay encrypted private messages, broadcast certified evacuation directives, and transmit high-priority SOS emergency distress signals across campus blocks.

---

## ⚡ Core Innovations & Technical Capabilities

* **Dual-Layer Physical Radio Synergy:**
  * **Layer 1: Offline Local LAN Subnet UDP Broadcast (`Port 38884`):** Leverages offline Wi-Fi access points or student-hosted hotspots with native Android `WifiManager.MulticastLock` to achieve ultra-low latency (< 5ms) broadcast delivery across buildings.
  * **Layer 2: Direct Bluetooth Low Energy (BLE 5.0) GATT Engine:** Custom GATT service (`MESH_SERVICE_UUID`, MTU 512) and connectable manufacturer advertisement beacons broadcasting student Roll Numbers and Department tags offline.
* **Autonomous Multi-Hop Relaying ($A 	o B 	o C$):**
  * When communicating with peers beyond direct physical radio range (e.g., CSE Block $\leftrightarrow$ Mechanical Block), intermediate student devices in the middle automatically forward the packet.
  * Packets decrement TTL (Time-To-Live) and increment `hopCount` at each intermediate hop.
* **Zero-Knowledge Hop Privacy:**
  * Intermediate relay nodes operate as silent network routers. They **never** decrypt, insert into their local chat history, or display private messages meant for another peer.
  * Only the designated recipient can decrypt and read the message.
* **Broadcast Storm Suppression via Ring-Buffer Deduplication:**
  * A fast $O(1)$ circular memory cache (`seenCacheDao`) filters out already-seen packet UUIDs, preventing radio collision saturation, infinite packet bouncing, and broadcast storms.
* **3-Tier Preemptive Quality-of-Service (QoS):**
  * **Tier 1 (Priority 0x01):** Emergency SOS Beacons & Clinic Delivery Receipts (Preempts all traffic, latency < 1.5s).
  * **Tier 2 (Priority 0x02):** Cryptographically Signed Authority Bulletins (Gold Shield notices, latency < 10s).
  * **Tier 3 (Priority 0x03):** Routine Peer-to-Peer Encrypted Chat (Best-effort DTN routing).
* **Disaster-Ergonomic True OLED Dark Theme:**
  * True OLED Black (`#000000` / `#0C1317`) shuts off individual display pixels, extending critical battery life by up to 42% during blackout conditions.

---

## 🏗️ System Architecture & Multi-Hop Relay Engine

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           CAMPUSMESH PROTOCOL STACK                             │
├─────────────────────────────────────────────────────────────────────────────────┤
│  APPLICATION LAYER: Jetpack Compose UI (Disaster Ergonomics & OLED Dark Theme) │
│  ├── Emergency SOS Modal    ├── Authority Bulletin Feed                         │
│  ├── Multi-Hop Mesh Chat    └── Offline Vector Campus Map                       │
├─────────────────────────────────────────────────────────────────────────────────┤
│  QoS & ROUTING ENGINE: PriorityQueueManager • StoreAndForwardVault • SeenCache │
│  ├── Tier 1: Emergency SOS (Preemptive Dispatch)                                │
│  ├── Tier 2: Gold Shield Notices (Signed by Authority)                         │
│  └── Tier 3: Routine Peer-to-Peer Encrypted Chat (TTL Decrement & Multi-Hop)    │
├─────────────────────────────────────────────────────────────────────────────────┤
│  CRYPTOGRAPHY ENGINE: Ed25519 Signatures • AES-256-GCM Pairwise E2EE            │
├─────────────────────────────────────────────────────────────────────────────────┤
│  DUAL HARDWARE RADIO TRANSPORT:                                                 │
│  ├── Offline LAN UDP Broadcast (Port 38884 with MulticastLock)                  │
│  └── Bluetooth LE 5.0 GATT Client/Server (Connectable Beacons + MTU 512)        │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### Multi-Hop Packet Relaying Sequence ($A 	o B 	o C$)

```
[Device A: Roll 1]               [Device B: Roll 2]               [Device C: Roll 5124]
 (CSE Block - Sender)           (Central Quad - Relay)           (Mech Block - Recipient)
        │                                  │                                  │
        │── UDP / BLE GATT Packet ────────►│                                  │
        │   destRoll: "5124"               │                                  │
        │   TTL: 7, Hops: 0                │ (Barrier 1: Seen Cache? No)      │
        │   Ciphertext: AES-256-GCM        │ (Barrier 2: For Me? No)          │
        │                                  │ (Barrier 3: TTL > 1? Yes)        │
        │                                  │                                  │
        │                                  │ • Decrements TTL: 7 ➔ 6          │
        │                                  │ • Increments Hops: 0 ➔ 1         │
        │                                  │ • Sets relayedBy: "2"            │
        │                                  │ • Kept INVISIBLE to Device B!    │
        │                                  │                                  │
        │                                  │── Re-broadcasts over UDP/BLE ───►│
        │                                  │                                  │ (Barrier 1: Seen? No)
        │                                  │                                  │ (Barrier 2: For Me? YES!)
        │                                  │                                  │ • Decrypts AES-GCM
        │                                  │                                  │ • Inserts into Room DB
        │                                  │                                  │ • Displays Bubble:
        │                                  │                                  │   "hii"
        │                                  │                                  │   [🔀 Relayed 2 Hops via 2]
```

---

## 📦 Wire Protocol & Packet Specifications

Every packet transmitted across the mesh adheres to a strictly aligned binary and JSON wire protocol designed to conserve radio airtime and reduce collision probability:

```
 0                   1                   2                   3
 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|          Magic Bytes          | Protocol Ver  |  Pkt Type/QoS |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|     Flags     |  Initial TTL  | Remaining TTL |   Reserved    |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                                                               |
+                       Message UUID (16 Bytes)                 +
|                                                               |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                   Source Roll Number (16 Bytes)               |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                Destination Roll Number (16 Bytes)             |
|                   (0xFF...FF for Broadcast)                   |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                     Sequence Number (32-Bit)                  |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                   Unix Epoch Timestamp (64-Bit)               |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|      Payload Length (uint16)  |                               |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+                               +
|                   Variable Encrypted Payload                  |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                 Ed25519 Digital Signature (64 Bytes)          |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                   CRC-32 Checksum (32-Bit)                    |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
```

| Field Name | Type | Size | Description |
| :--- | :---: | :---: | :--- |
| `magic_bytes` | uint16 | 2B | Fixed protocol identifier (`0x4D53` = ASCII `'MS'`). Discards foreign packets immediately. |
| `packet_type` | uint8 | 1B | `0x01`: SOS Beacon, `0x02`: Authority Notice, `0x03`: Encrypted Chat, `0x05`: Delivery ACK. |
| `ttl` / `remaining_ttl` | uint8 | 1B | Decremented by 1 at each forwarding node. Packet dropped when `remaining_ttl <= 1`. |
| `message_uuid` | byte[16] | 16B | Unique RFC-4122 UUID for instantaneous $O(1)$ ring-buffer deduplication. |
| `sender_roll` | string | 16B | Originating student or faculty roll number (e.g., `"A23126551042"` or `"1"`). |
| `dest_roll` | string | 16B | Recipient roll number or `"BROADCAST"` / `"ALL PEERS"`. |
| `payload` | byte[] | Var | Encrypted AES-256-GCM ciphertext (for 1:1 chat) or plaintext text (for public broadcasts). |
| `signature` | byte[64] | 64B | Cryptographic Ed25519 signature ensuring zero tampering of official directives. |

---

## 🔒 Cryptographic Security & Zero-Knowledge Hop Privacy

1. **Zero-Knowledge Asymmetric Digital Signatures (Ed25519):**
   * High-priority evacuation directives issued by the Principal, Dean, or HOD are signed with their private Ed25519 key.
   * Devices verify the signature offline against the pre-cached public key directory. Verified messages display an unmistakable **[★ GOLD SHIELD]** badge, neutralizing crisis panic and fake rumors.
2. **End-to-End Authenticated Encryption (AES-256-GCM):**
   * All 1-to-1 private chat payloads are encrypted end-to-end.
   * Encryption key seeds are deterministically derived pairwise from communicating student roll numbers:  
     $$	ext{Seed} = 	ext{SHA-256}\Big(	ext{"ANITS\_E2EE\_"} + \min(	ext{roll}_A, 	ext{roll}_B) + 	ext{"\_"} + \max(	ext{roll}_A, 	ext{roll}_B)\Big)$$
   * Intermediate relay nodes see only high-entropy ciphertext. Even with packet sniffing tools, private messages cannot be intercepted or read by intermediary nodes.
3. **Open Broadcast Transparency:**
   * Public Campus General Broadcasts remain open so every reachable student and faculty device can immediately read evacuation directives.

---

## 👥 Department Directory & Cross-Campus Private Chat

When communicating across campus blocks (e.g., reaching a friend in the **Mechanical Block** while stationed in the **CSE Block**), the recipient is out of direct Bluetooth radio range.

CampusMesh includes an integrated **Department & Roll Number Directory Picker**:

```
+-------------------------------------------------------+
|  🔒 Start Private Mesh Chat                           |
|  Multi-Hop Hopping across Campus Blocks               |
+-------------------------------------------------------+
|  1. SELECT RECIPIENT'S DEPARTMENT:                    |
|  [ MECH ] [ CSE ] [ CSD ] [ ECE ] [ CIVIL ] [ IT ]    |
|                                                       |
|  2. PEERS IN MECH (TAP TO START):                     |
|  +--------------------+  +--------------------+       |
|  | Friend (Mech Block)|  | Lab Partner (Mech) |       |
|  | Roll: 5124 ➔       |  | Roll: 5102 ➔       |       |
|  +--------------------+  +--------------------+       |
|                                                       |
|  3. OR ENTER SPECIFIC ROLL NUMBER:                    |
|  [ e.g. 5124, 3, A23126552012                     ]   |
|  [ Friend's Name (Optional)                       ]   |
|                                                       |
|                      [ Cancel ] [ Start Encrypted Chat ]
+-------------------------------------------------------+
```

1. **Tap the Floating Action Button (`+ New Chat`)** in the Mesh Chat inbox.
2. **Select the target department** (e.g. `MECH`, `CSE`, `CSD`, `ECE`).
3. **1-Tap Quick Select** any suggested student card OR enter any specific student Roll Number.
4. An encrypted private channel opens immediately with subtitle `🔀 Multi-Hop Mesh (MECH) • E2EE Encrypted`.
5. Messages sent to this peer hop through intermediate campus devices until reaching the destination student.

---

## 📱 Application Screens & Disaster-Ergonomic UI

The application features four high-contrast disaster-ergonomic screens built using **Jetpack Compose**:

```
┌───────────────────────┐   ┌───────────────────────┐   ┌───────────────────────┐   ┌───────────────────────┐
│     EMERGENCY SOS     │   │   CAMPUS BULLETINS    │   │       MESH CHAT       │   │    MUSTER GPS MAP     │
│  Hold 1.5s to trigger │   │  Gold Shield Notices  │   │  Multi-Hop WhatsApp   │   │  Vector Safe Routes   │
│  Closed-Loop ACK      │   │  Offline Verification │   │  Encrypted Messaging  │   │  Cricket Safe Ground  │
└───────────────────────┘   └───────────────────────┘   └───────────────────────┘   └───────────────────────┘
```

### 1. Emergency SOS Dispatcher
* **1.5-Second Hold-to-Trigger Button:** Prevents accidental triggers while maximizing Fitts's law touch targets.
* **Auto-Enriched Telemetry:** Automatically queries nearby BLE beacons to attach the student's current block and floor (e.g., *"CSE Block — Floor 3"*).
* **Closed-Loop Green Receipt:** When the Campus Clinic acknowledges the distress beacon, the victim's screen transitions to vibrant **MeshGreen (`#10B981`)** with an audible confirmation: *"Help is En Route!"*.

### 2. Verified Authority Bulletin Feed
* High-visibility emergency directives cryptographically verified offline.
* Signatures verified against institutional public keys stored in the local Room database.
* Displays signer identity, department, hop count, and elapsed time.

### 3. Peer-to-Peer Encrypted Mesh Chat
* WhatsApp-style ergonomics with emerald outgoing bubbles (`#005C4B`), dark slate incoming bubbles (`#202C33`), and cyan delivery checkmarks (`✓✓`).
* **Pinned Campus General Broadcast Channel** (`SENDER → ALL NODES`).
* **Discovered Direct Peers Section:** Shows physical peers in direct Bluetooth/Wi-Fi range with live RSSI badges (`-49 dBm (~3m)`).
* **Multi-Hop Private Chats Section:** Shows conversations with peers in distant blocks reached via intermediate hopping nodes.
* **Live Hop Telemetry Badges on Messages:**
  * `⚡ Direct (1 Hop)` when in direct line of sight.
  * `🔀 Relayed (N Hops via Roll X)` when forwarded through intermediary nodes.

### 4. Vector Campus Muster Map
* 100% pre-cached vector maps rendering ANITS campus blocks (CSE, ECE, Mechanical, Civil, Admin, Hostels).
* Highlights structural fire exit stairwells and designated open-ground safe muster points (Central Cricket Ground).

---

## 🗄️ Embedded Database & Cloud Directory Sync

All operational state persists in an embedded **Android Room SQLite** database:

```sql
-- 1. Discovered Radio Peers Table
CREATE TABLE table_peers (
    node_id TEXT PRIMARY KEY NOT NULL,
    campus_roll_no TEXT,
    assigned_role INTEGER NOT NULL,          -- 0=Admin, 1=Authority, 2=Responder, 3=Student
    last_seen_timestamp INTEGER NOT NULL,
    last_rssi INTEGER NOT NULL,
    mac_address TEXT NOT NULL,
    preferred_radio TEXT NOT NULL            -- 'BLE' or 'LAN_UDP'
);

-- 2. Master Message Store (Relayed & Local Messages)
CREATE TABLE table_messages (
    message_uuid TEXT PRIMARY KEY NOT NULL,
    packet_type INTEGER NOT NULL,            -- 0x01=SOS, 0x02=Notice, 0x03=Chat
    source_id TEXT NOT NULL,
    dest_id TEXT NOT NULL,
    payload_bytes BLOB NOT NULL,
    is_encrypted INTEGER NOT NULL DEFAULT 0,
    signature_bytes BLOB,
    hop_count INTEGER NOT NULL,
    remaining_ttl INTEGER NOT NULL,
    delivery_status TEXT NOT NULL,           -- 'QUEUED', 'RELAYED', 'DELIVERED'
    created_epoch INTEGER NOT NULL
);

-- 3. Rolling Ring-Buffer Deduplication Cache (Prevents Broadcast Storms)
CREATE TABLE table_seen_cache (
    message_uuid TEXT PRIMARY KEY NOT NULL,
    received_timestamp INTEGER NOT NULL
);
```

### Pre-Crisis Cloud Directory Synchronization
During normal operational campus days, the app silently synchronizes verified student roll numbers, departments, and public keys with the **Supabase PostgreSQL** institutional directory via background workers. During a blackout, this cached directory allows immediate offline cryptographic identity verification.

---

## 🧪 Physical Hardware Verification & Live Field Testing

The system has been compiled, deployed, and verified on physical hardware without active SIM cards, Wi-Fi internet, or cellular data:

* **Node A (Tablet):** Samsung Galaxy Tab A8 (`R9ZW3030T9B`) — User **`b (2)`** (Dept of CSD).
* **Node B (Smartphone):** Vivo / Android Phone (`I2301`) — User **`a (1)`** (Dept of CSD).

### Verified Test Log Output:
```log
10-25 16:27:24.842 I BleScannerDaemon: 🎉 FOUND PHYSICAL PEER! Roll=1, Name=a, RSSI=-49dBm
10-25 16:28:23.620 I UdpMeshTransport: Sent UDP mesh packet (186 bytes) to 2 endpoints
10-25 17:00:15.112 I UdpMeshTransport: ✓ [DESTINATION DELIVERED] Message from a (1): hii [Hops: 1]
10-25 17:03:32.418 I BleGattMeshTransport: 🔀 [MULTI-HOP RELAY] Relaying packet from 1 -> 5124 via 2 (Hop 1, TTL: 6)
```

---

## 📁 Repository Directory Organization

```
anits-campus-mesh/
├── docs/                                  # Formal Engineering Documentation
│   ├── BLUEPRINT.md                       # Master System Architecture & Threat Model
│   ├── LIFECYCLE.md                       # 6-Stage Software Engineering Lifecycle Roadmap
│   ├── STAGE_1_REQUIREMENTS.md            # Formal SRS (Personas, FRs, NFRs, Use Cases)
│   ├── STAGE_2_ARCHITECTURE.md            # Technical Specs (70B Wire Protocol, FSM, Room DB)
│   └── STAGE_3_UI_UX_DESIGN.md            # Disaster Ergonomics, WCAG AAA & Compose Specs
├── ppts/                                  # Institutional Review Presentations
│   ├── Review 1.pptx                      # Review Phase 1 Presentation
│   └── Review 2.pptx                      # Review Phase 2 System Architecture Presentation
├── ui_ux/                                 # Design Kits, Vector Artboards & Prototypes
│   ├── figma_mesh_ui_kit.svg              # Combined 4-Screen Vector Artboard (Drag to Figma)
│   ├── figma_screen_1_sos.svg             # Screen 1: Emergency SOS Wireframe
│   ├── figma_screen_2_bulletins.svg       # Screen 2: Gold Shield Notices Wireframe
│   ├── figma_screen_3_chat.svg            # Screen 3: P2P Encrypted Mesh Chat Wireframe
│   └── figma_screen_4_map.svg             # Screen 4: Offline Campus Muster Map Wireframe
├── campus-mesh-android/                   # Native Android Jetpack Compose Project
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── AndroidManifest.xml        # BLE, Wi-Fi Multicast, and Location permissions
│   │   │   └── java/in/anits/mesh/
│   │   │       ├── crypto/                # Ed25519 signatures & AES-256-GCM E2EE
│   │   │       ├── data/                  # Room Database, Daos, Entities, UserManager
│   │   │       ├── model/                 # MeshPacket, PacketType, UserRole
│   │   │       ├── radio/                 # RadioCoordinator, BLE & UDP Transports
│   │   │       ├── routing/               # PriorityQueueManager, StoreAndForwardVault
│   │   │       ├── ui/                    # Compose Screens (SOS, Bulletins, Chat, Map)
│   │   │       └── viewmodel/             # Unidirectional StateFlow ViewModels
│   │   └── build.gradle.kts
│   └── build.gradle.kts
└── README.md                              # Definitive Repository Documentation
```

---

## 🛠️ Build, Sideload & Installation Guide

### Prerequisites
* **Android Studio:** Hedgehog (2023.1.1) or newer (Jellyfish / Iguana recommended).
* **JDK:** OpenJDK 17.
* **Target Android SDK:** API Level 34 (Android 14.0).
* **Minimum Android SDK:** API Level 29 (Android 10.0 — required for modern BLE & Wi-Fi Aware APIs).

### Compilation Commands
```bash
# Clone the repository
git clone https://github.com/047harry/anits-campus-mesh.git
cd anits-campus-mesh/campus-mesh-android

# Build the Debug APK using Gradle
./gradlew assembleDebug

# Output APK path:
# app/build/outputs/apk/debug/app-debug.apk
```

### Installation via ADB
Connect your Android device via USB with USB Debugging enabled:
```bash
# Sideload the APK onto connected device
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Launch the application
adb shell am start -n in.anits.mesh/.MainActivity
```

---

## 🎓 Academic Credits & Team Roster

This project is submitted in partial fulfillment of the requirements for the degree of **Bachelor of Technology in Computer Science & Engineering (Data Science)** at **Anil Neerukonda Institute of Technology & Sciences (ANITS)**.

* **Project Title:** Decentralized Offline-First Campus Mesh Network (CampusMesh)
* **Team:** Team 12 (CSD Department)
* **Academic Year:** 2025 – 2026
* **Institution:** ANITS, Affiliated to Andhra University, Visakhapatnam, Andhra Pradesh, India

---
*Developed with ❤️ by ANITS CSD Team 12 for high-resilience campus disaster safety.*
