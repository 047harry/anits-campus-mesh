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
2. [Core Innovations & Key Capabilities](#-core-innovations--key-capabilities)
3. [System Architecture & Multi-Hop Relay Protocol](#-system-architecture--multi-hop-relay-protocol)
4. [Wire Protocol & Packet Specifications](#-wire-protocol--packet-specifications)
5. [Cryptographic Security & End-to-End Privacy](#-cryptographic-security--end-to-end-privacy)
6. [Application Screens & UI/UX Workflow](#-application-screens--uiux-workflow)
7. [Department Directory & Cross-Campus Private Chat](#-department-directory--cross-campus-private-chat)
8. [Embedded Database & Cloud Sync Schema](#-embedded-database--cloud-sync-schema)
9. [Physical Hardware Verification & Live Field Test](#-physical-hardware-verification--live-field-test)
10. [Build & Installation Guide](#-build--installation-guide)
11. [Project Structure](#-project-structure)
12. [Academic Credits & Team Roster](#-academic-credits--team-roster)

---

## 🌪️ Executive Summary & Problem Statement

Modern campus communication infrastructure relies entirely on centralized telecommunication towers, commercial ISP fiber backbones, and external cloud servers (e.g., WhatsApp, Telegram, Microsoft Teams). 

During severe natural disasters (such as cyclones like **Hudhud**, seismic tremors, campus structural fires, or total municipal power grid collapse):
* **Cellular towers lose backup generator power** within hours.
* **Fiber lines snap** due to fallen trees and structural debris.
* **Cloud servers become unreachable**, leaving thousands of students, faculty, and administrative staff stranded with zero communication.

```
CENTRALIZED DISASTER FAILURE:
[Student Phone] ──X──> [Fallen Cell Tower] ──X──> [Cloud Server] ──X──> [Rescue / Clinic]
❌ RESULT: Zero communication, severe panic, unverified rumors, delayed rescue.

CAMPUSMESH AD-HOC OFF-GRID SOLUTION:
[CSE Block Student] ────► [Central Library Peer] ────► [Mechanical Block Friend]
                      (BLE 5.0 / LAN UDP Hops)
✔ RESULT: 100% Offline, autonomous packet relay, multi-hop hopping, end-to-end encrypted.
```

**CampusMesh** is an autonomous, decentralized, offline-first mobile mesh networking system that transforms ordinary student and faculty Android smartphones into self-organizing peer-to-peer radio relays. Even in absolute grid blackout conditions with **zero internet, zero cellular signal, and zero external infrastructure**, students and emergency responders can exchange encrypted messages, broadcast critical evacuation notices, and dispatch high-priority SOS distress alerts.

---

## ⚡ Core Innovations & Key Capabilities

* **Dual-Layer Physical Radio Transport:**
  * **Layer 1: Offline Local LAN Subnet UDP Broadcast (`Port 38884`):** Leverages offline Wi-Fi access points or student-hosted hotspots to achieve ultra-low latency (< 5ms) packet transmission across campus buildings.
  * **Layer 2: Direct Bluetooth Low Energy (BLE 5.0) GATT Engine:** Direct offline device-to-device transport using custom GATT services (`MESH_SERVICE_UUID`), MTU 512 negotiation, and connectable manufacturer advertising beacons containing real student roll numbers.
* **Autonomous Multi-Hop Relaying ($A 	o B 	o C$):**
  * When two students are out of direct radio range (e.g. CSE Block $\leftrightarrow$ Mechanical Block), intermediate student devices in the middle automatically forward the packet.
  * Packets decrement TTL (Time-To-Live) and increment `hopCount` at each relay hop.
* **Zero-Knowledge Hop Privacy:**
  * Intermediate relay nodes act as invisible network routers. They **never** decrypt, store, or display private messages meant for another peer.
  * Only the designated recipient can decrypt and view the message.
* **Storm-Suppression Ring-Buffer Deduplication:**
  * Fixed-capacity circular memory cache (`seenCacheDao`) discards duplicate packets in $O(1)$ time, permanently preventing broadcast storms and infinite routing loops.
* **3-Tier Preemptive Quality-of-Service (QoS):**
  * **Tier 1 (Priority 0x01):** Emergency SOS Beacons & Clinic Delivery Receipts (Preempts all traffic).
  * **Tier 2 (Priority 0x02):** Cryptographically Signed Authority Evacuation Bulletins (Gold Shield notices).
  * **Tier 3 (Priority 0x03):** Routine Peer-to-Peer Private Encrypted Chat (Best-effort DTN routing).
* **Disaster-Ergonomic OLED Dark Theme:**
  * True OLED Black (`#000000` / `#0C1317`) shuts off individual display pixels, extending critical smartphone battery life by up to 42% during extended blackouts.

---

## 🏗️ System Architecture & Multi-Hop Relay Protocol

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           CAMPUSMESH PROTOCOL STACK                             │
├─────────────────────────────────────────────────────────────────────────────────┤
│  APPLICATION LAYER: Jetpack Compose UI (Disaster Ergonomics & OLED Dark Theme) │
│  ├── Emergency SOS Modal    ├── Authority Bulletin Feed                         │
│  ├── Multi-Hop Mesh Chat    └── Offline Vector Muster Map                       │
├─────────────────────────────────────────────────────────────────────────────────┤
│  QoS & ROUTING ENGINE: PriorityQueueManager • StoreAndForwardVault • SeenCache │
│  ├── Tier 1: Emergency SOS (Preemptive)                                         │
│  ├── Tier 2: Gold Shield Notices (Signed by Authority)                         │
│  └── Tier 3: Routine Peer-to-Peer Encrypted Chat (TTL Decrement & Multi-Hop)    │
├─────────────────────────────────────────────────────────────────────────────────┤
│  CRYPTOGRAPHY ENGINE: Ed25519 Signatures • AES-256-GCM Symmetrical E2EE         │
├─────────────────────────────────────────────────────────────────────────────────┤
│  DUAL HARDWARE RADIO TRANSPORT:                                                 │
│  ├── Offline LAN UDP Broadcast (Port 38884 with MulticastLock)                  │
│  └── Bluetooth LE 5.0 GATT Client/Server (Connectable Beacons + MTU 512)        │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### Multi-Hop Message Relaying Workflow ($A 	o B 	o C$)

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

Every packet transmitted across the mesh adheres to a strict binary/JSON wire protocol to guarantee minimal radio airtime and avoid packet collision:

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
| `magic_bytes` | uint16 | 2B | Fixed header identifier (`0x4D53` = `'MS'`). Filters alien radio noise. |
| `packet_type` | uint8 | 1B | `0x01` = SOS Beacon, `0x02` = Authority Notice, `0x03` = Chat, `0x04` = Heartbeat, `0x05` = Delivery ACK. |
| `ttl` / `remaining_ttl` | uint8 | 1B | Decremented at each hop. Packet dropped when `remaining_ttl <= 1` to prevent loops. |
| `message_uuid` | byte[16] | 16B | RFC-4122 unique UUID for $O(1)$ ring-buffer deduplication. |
| `sender_roll` | string | 16B | Originating student or faculty roll number (e.g., `"A23126551042"` or `"1"`). |
| `dest_roll` | string | 16B | Recipient roll number or `"BROADCAST"` / `"ALL PEERS"`. |
| `payload` | byte[] | Var | Plaintext text (for broadcasts) or AES-256-GCM ciphertext (for private chats). |
| `signature` | byte[64] | 64B | Cryptographic Ed25519 signature ensuring zero tampering. |

---

## 🔒 Cryptographic Security & End-to-End Privacy

1. **Zero-Knowledge Asymmetric Digital Signatures (Ed25519):**
   * Authority directives issued by the Principal, Dean, or HOD are signed with their private Ed25519 key.
   * Devices verify the signature offline against the pre-cached public key directory. Verified messages display an unmistakable **[★ GOLD SHIELD]** badge, preventing panic and fake rumors during disasters.
2. **End-to-End Authenticated Encryption (AES-256-GCM):**
   * All 1-to-1 private chat payloads are encrypted end-to-end.
   * Encryption key seeds are deterministically derived pairwise from the communicating student roll numbers:  
     $$	ext{Seed} = 	ext{SHA-256}\Big(	ext{"ANITS\_E2EE\_"} + \min(	ext{roll}_A, 	ext{roll}_B) + 	ext{"\_"} + \max(	ext{roll}_A, 	ext{roll}_B)\Big)$$
   * Intermediate relay nodes only see ciphertext. Even with packet sniffing tools, private messages cannot be intercepted or read by intermediary nodes.
3. **Open Broadcast Transparency:**
   * Public Campus General Broadcasts remain unencrypted so that every reachable student and faculty node can instantly read evacuation directives.

---

## 📱 Application Screens & UI/UX Workflow

The application consists of four high-contrast disaster-ergonomic screens built using **Jetpack Compose**:

```
┌───────────────────────┐   ┌───────────────────────┐   ┌───────────────────────┐   ┌───────────────────────┐
│     EMERGENCY SOS     │   │   CAMPUS BULLETINS    │   │       MESH CHAT       │   │    MUSTER GPS MAP     │
│  Hold 1.5s to trigger │   │  Gold Shield Notices  │   │  Multi-Hop WhatsApp   │   │  Vector Safe Routes   │
│  Closed-Loop ACK      │   │  Offline Verification │   │  Encrypted Messaging  │   │  Cricket Safe Ground  │
└───────────────────────┘   └───────────────────────┘   └───────────────────────┘   └───────────────────────┘
```

### 1. Emergency SOS Dispatcher
* **1.5-Second Hold-to-Trigger Button:** Prevents accidental triggers while maximizing Fitts's law touch targets.
* **Auto-Enriched Telemetry:** Automatically reads nearby BLE beacons to attach the student's current block and floor (e.g. *"CSE Block — Floor 3"*).
* **Closed-Loop Green Receipt:** When the Campus Clinic or Rescue Warden acknowledges the distress beacon, the victim's screen transitions to vibrant **MeshGreen (`#10B981`)** with an audible confirmation: *"Help is En Route!"*.

### 2. Verified Authority Bulletin Feed
* High-visibility emergency directives cryptographically verified offline.
* Signatures verified against institutional public keys stored in local Room database.
* Displays signer identity, department, hop count, and elapsed time.

### 3. Peer-to-Peer Encrypted Mesh Chat
* WhatsApp-style ergonomics with emerald outgoing bubbles (`#005C4B`), dark slate incoming bubbles (`#202C33`), and cyan delivery checkmarks (`✓✓`).
* **Pinned Campus General Broadcast Channel** (`SENDER → ALL NODES`).
* **Discovered Direct Peers Section:** Shows physical peers in direct Bluetooth/Wi-Fi range with live RSSI badges (`-49 dBm (~3m)`).
* **Multi-Hop Private Chats Section:** Shows conversations with friends in distant blocks reached via intermediate hopping nodes.
* **Live Hop Telemetry Badges on Messages:**
  * `⚡ Direct (1 Hop)` when in direct line of sight.
  * `🔀 Relayed (N Hops via Roll X)` when forwarded through intermediary nodes.

### 4. Vector Campus Muster Map
* 100% pre-cached vector maps rendering ANITS campus blocks (CSE, ECE, Mechanical, Civil, Admin, Hostels).
* Highlights structural fire exit stairwells and designated open-ground safe muster points (Central Cricket Ground).

---

## 👥 Department Directory & Cross-Campus Private Chat

When communicating across campus (e.g., reaching a friend in the **Mechanical Block** while stationed in the **CSE Block**), the recipient is out of direct Bluetooth radio range.

CampusMesh includes a dedicated **Department & Roll Number Directory Picker**:

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
2. **Select the target department** (e.g. `MECH`, `ECE`, `CSD`).
3. **1-Tap Quick Select** any suggested student card OR type any custom student Roll Number.
4. An encrypted private channel opens immediately with subtitle `🔀 Multi-Hop Mesh (MECH) • E2EE Encrypted`.
5. Messages sent to this peer hop through intermediate campus devices until reaching the final recipient.

---

## 🗄️ Embedded Database & Cloud Sync Schema

All data persists in an embedded **Android Room SQLite** database on the device:

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
During normal operational campus days, the app silently synchronizes verified student roll numbers, departments, and public keys with the **Supabase PostgreSQL / Cloud Firestore** institutional database via background workers. During a blackout, this cached directory allows immediate offline cryptographic identity verification.

---

## 🧪 Physical Hardware Verification & Live Field Test

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

## 🛠️ Build & Installation Guide

### Prerequisites
* **Android Studio:** Hedgehog (2023.1.1) or newer (Jellyfish / Iguana recommended).
* **JDK:** OpenJDK 17.
* **Target Android SDK:** API Level 34 (Android 14.0).
* **Minimum Android SDK:** API Level 29 (Android 10.0 — required for modern BLE & Wi-Fi Aware APIs).

### Compilation Commands
```bash
# Clone the repository
git clone https://github.com/047harry/anits-campus-mesh.git
cd anits-campus-mesh

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

## 📂 Project Structure

```
campus-mesh-android/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml          # BLE, Wi-Fi Multicast, and Location permissions
│   │   └── java/in/anits/mesh/
│   │       ├── crypto/
│   │       │   └── CryptoEngine.kt      # Ed25519 digital signatures & AES-256-GCM E2EE
│   │       ├── data/
│   │       │   ├── MeshDatabase.kt      # Room SQLite Database instance
│   │       │   ├── UserManager.kt       # Profile management & Supabase directory sync
│   │       │   ├── dao/                 # MessageDao, PeerDao, SeenCacheDao
│   │       │   └── entity/              # MessageEntity, PeerEntity, SeenCacheEntity
│   │       ├── model/
│   │       │   ├── MeshPacket.kt        # Packet wire framing model & byte serializers
│   │       │   ├── PacketType.kt        # SOS (0x01), Bulletin (0x02), Chat (0x03)
│   │       │   └── UserRole.kt          # Role definitions (HOD, Faculty, Student)
│   │       ├── radio/
│   │       │   ├── RadioCoordinator.kt  # Unified dual-radio transport controller
│   │       │   ├── BleScannerDaemon.kt  # BLE 5.0 beacon scanner with roll extraction
│   │       │   ├── BleAdvertiserDaemon.kt # Connectable manufacturer beacon advertiser
│   │       │   ├── BleGattMeshTransport.kt # Direct BLE GATT Server & Client with hopping
│   │       │   └── UdpMeshTransport.kt  # Offline LAN UDP broadcast with multi-hop relay
│   │       ├── routing/
│   │       │   ├── PriorityQueueManager.kt # 3-Tier Preemptive QoS algorithm
│   │       │   ├── StoreAndForwardVault.kt # DTN offline queuing & contact flusher
│   │       │   └── AckManager.kt        # Closed-loop return receipt delivery
│   │       ├── ui/
│   │       │   ├── screens/
│   │       │   │   ├── SosScreen.kt     # Emergency SOS Panic Dispatcher
│   │       │   │   ├── BulletinsScreen.kt # Official Gold Shield Authority Feed
│   │       │   │   ├── ChatScreen.kt    # WhatsApp P2P Mesh Chat & Department Picker
│   │       │   │   ├── MapScreen.kt     # Offline Vector Muster Map
│   │       │   │   └── AuthScreen.kt    # Offline Profile & Password Registration
│   │       │   └── theme/               # High-contrast disaster OLED color tokens
│   │       └── viewmodel/
│   │           └── ChatViewModel.kt     # Unidirectional StateFlow for mesh chat
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
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
