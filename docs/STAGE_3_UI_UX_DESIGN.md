# STAGE 3: UI/UX & INTERACTION WORKFLOW DESIGN
**Disaster-Ergonomic Interface Specifications & Jetpack Compose UI Models**
**Capstone Project Lifecycle — Team 12 (ANITS, Dept of CSD)**

---

## 1. Executive Summary & Design Philosophy

In catastrophic infrastructure blackouts (cyclones, structural fires, seismic tremors, or campus power grid collapse), standard consumer application interfaces fail catastrophically. Users in crisis experience acute cognitive tunneling, fine-motor skill degradation, elevated stress tremors, and severe perceptual narrowing. Furthermore, electrical blackouts induce pitch-dark ambient conditions while smartphones face terminal battery depletion.

Therefore, the **Decentralized Offline-First Campus Mesh Network** rejects decorative, cognitive-heavy consumer UI paradigms in favor of **Disaster Ergonomics & Zero-Friction Operational Design**:

```
┌────────────────────────────────────────────────────────────────────────┐
│                   CORE DISASTER UI/UX PRINCIPLES                       │
├────────────────────────────────────────────────────────────────────────┤
│ 1. Zero Cognitive Overhead: Critical actions require <= 1 tap.         │
│ 2. Fitts's Law Maximization: Primary emergency touch targets >= 84 dp. │
│ 3. True OLED Dark Mode: Pure #000000 background saves up to 42% battery.│
│ 4. Cryptographic Visual Trust: Gold Shield badges prevent panic rumors.│
│ 5. Multi-Sensory Feedback: Audio sirens + haptic vibration confirmation│
│ 6. Offline Spatial Orientation: Vector campus map with muster zones.   │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Ergonomic Foundations & Visual Design Tokens

### 2.1 WCAG 2.1 AAA High-Contrast Color Palette
The interface is anchored on high-contrast accessibility standards to remain legible through thick smoke, glare, or darkness:

| Design Token Name | Hex Code | Purpose & Semantic Role |
| :--- | :--- | :--- |
| **`EmergencyRed`** | `#EF4444` | High-visibility emergency SOS triggers, injury alerts, urgent hazard warnings. |
| **`AuthorityGold`** | `#F59E0B` | Verified Institutional Gold Shield broadcasts (Principal/Dean/HOD directives). |
| **`MeshGreen`** | `#10B981` | Positive delivery ACKs, verified peer connectivity, active mesh routing health. |
| **`DirectBlue`** | `#3B82F6` | Direct 1:1 peer-to-peer secure messaging channels and navigation vectors. |
| **`OledBlack`** | `#000000` | True-black base background (turns off OLED pixels, minimizing battery draw). |
| **`SurfaceDark`** | `#111827` | Elevated card surfaces and message containers (distinct from true black). |
| **`SurfaceBorder`** | `#374151` | High-contrast container borders for tactile visual hierarchy. |
| **`TextPrimary`** | `#FFFFFF` | Maximum legibility headlines, critical instructions, and emergency text. |
| **`TextMuted`** | `#9CA3AF` | Secondary operational metadata, timestamps, and hop distance counts. |

### 2.2 Typography Hierarchy
All typography relies on system-native `Roboto` / `Inter` to avoid font rendering delays or external asset dependencies:

* **Display Emergency (32 sp, Bold):** Reserved for immediate SOS status and panic countdowns.
* **Heading 1 (22 sp, Bold):** Screen titles, official bulletin headlines, and modal headers.
* **Heading 2 (16 sp, SemiBold):** Section dividers, peer identities, and triage categories.
* **Body Primary (14 sp, Medium):** Notice text, chat messages, and evacuation instructions.
* **Caption / Metadata (11 sp, Regular):** Hop distance counters, TTL status, and cryptographic key hashes.

### 2.3 Tactile Haptic & Audio Feedback Patterns
In disaster zones, optical displays may be obscured by dust or smoke, and phones may remain in pockets:
* **SOS Trigger Haptic:** Triple-pulse high-amplitude haptic burst (`VibrationEffect.createWaveform(longArrayOf(0, 150, 100, 150, 100, 300), -1)`).
* **Delivery Confirmation Haptic:** Sustained smooth haptic pulse indicating successful arrival at the clinic.
* **Incoming Authority Directive:** High-decibel audio beacon overriding system silent mode via Android `USAGE_ALARM`.

---

## 3. Granular Screen Workflows & ASCII Wireframes

```
┌────────────────────────────────────────────────────────────────────────┐
│                      CORE APPLICATION SCREEN GRAPH                     │
│                                                                        │
│   ┌─────────────────────┐                 ┌─────────────────────┐      │
│   │   1. EMERGENCY SOS  │◄───────────────►│  2. CAMPUS BULLETINS│      │
│   │   One-Tap Trigger   │                 │  Gold Shield Alerts │      │
│   └──────────┬──────────┘                 └──────────┬──────────┘      │
│              │                                       │                 │
│              ▼                                       ▼                 │
│   ┌─────────────────────┐                 ┌─────────────────────┐      │
│   │   3. P2P MESH CHAT  │◄───────────────►│  4. MUSTER MAP & GPS│      │
│   │   Encrypted Relays  │                 │  Offline Navigation │      │
│   └─────────────────────┘                 └─────────────────────┘      │
└────────────────────────────────────────────────────────────────────────┘
```

---

### 3.1 Screen 1: Emergency SOS & Panic Dispatch Flow

#### Operational Specification:
* **Trigger Mechanism:** Prominent circular button ($\ge 140$ dp) centered on the screen.
* **Accidental Trigger Barrier:** Requires a continuous **1.5-second hold** OR a single tap followed by an automatic **3-second visual/haptic cancel countdown**.
* **Automatic Context Enrichment:** App automatically queries cached BLE beacons to identify the current ANITS block and floor (e.g., *"CSE Block — Floor 3"*) and appends it to the packet payload.
* **State 1: Standby:** Ready to broadcast; shows live mesh peer count indicator.
* **State 2: Transmitting:** Pulsing red strobe, multi-hop broadcast in progress.
* **State 3: Acknowledged (Closed-Loop Receipt):** Screen background transitions to vibrant **MeshGreen (`#10B981`)** with an audible confirmation: *"Campus Clinic Acknowledged — Team Dispatched"*.

#### Screen 1 Wireframe:
```
+-------------------------------------------------------+
|  ANITS OFFLINE MESH       [● 14 Peers]      [Batt: 84%]|
+-------------------------------------------------------+
|                                                       |
|                     CAMPUS STATUS                     |
|           [!] CELLULAR & GRID BLACKOUT DETECTED       |
|                                                       |
|  Current Location:                                    |
|  CSE Block, Floor 3 (Corridor A)                      |
|                                                       |
|                  +-----------------+                  |
|                  |                 |                  |
|                  |       SOS       |                  |
|                  |    EMERGENCY    |                  |
|                  |                 |                  |
|                  +-----------------+                  |
|                  [ HOLD 1.5 SECONDS ]                 |
|                                                       |
|  SELECT EMERGENCY TYPE (Optional Quick-Tap):          |
|  [ Medical / Trauma ]     [ Trapped by Debris ]       |
|  [ Fire / Smoke ]         [ Structural Hazard ]       |
|                                                       |
|  ---------------------------------------------------  |
|  Relay Network Active: Transmitting through 14 nearby |
|  student devices within 100m.                         |
+-------------------------------------------------------+
| [ SOS (Active) ] [ Bulletins ] [ Mesh Chat ] [ Map ]  |
+-------------------------------------------------------+
```

---

### 3.2 Screen 2: Official Authority Noticeboard & Evacuation Directives

#### Operational Specification:
* **Anti-Rumor Protection:** During crises, fake panic messages cause stampedes. This screen strictly displays messages signed with **Tier 1 (Principal/HOD)** or **Tier 2 (Clinic/Disaster Warden)** Ed25519 private keys.
* **Visual Verification Badge:** An unmistakable **Gold Shield Badge** (`[★ GOLD SHIELD]`) is displayed alongside the verified signer's name, role, and department.
* **Local Verification Status:** Displays *"Cryptographically Verified Offline via Local Root Directory"*.
* **Routing Metadata:** Displays hop count and time elapsed (e.g., *"Hop 2 of 7 • 42s ago"*).

#### Screen 2 Wireframe:
```
+-------------------------------------------------------+
|  CAMPUS BULLETINS         [● 14 Peers]      [Batt: 84%]|
+-------------------------------------------------------+
|  OFFICIAL CAMPUS BROADCASTS (OFFLINE VERIFIED)        |
|                                                       |
|  +-------------------------------------------------+  |
|  | [★ GOLD SHIELD] OFFICIAL DIRECTIVE               |  |
|  | From: Dr. K. Venkata Lakshmi (HOD - CSD)        |  |
|  | Time: 10:14 AM  |  Relayed via: 2 Hops          |  |
|  | Status: [✓ Cryptographically Verified Offline]  |  |
|  +-------------------------------------------------+  |
|  | IMMEDIATE EVACUATION ORDER:                     |  |
|  | Minor tremor detected. Avoid Central Elevators.  |  |
|  | Proceed via Northern Fire Stairwell to the       |  |
|  | Central Cricket Ground Muster Point. Medical     |  |
|  | triage team stationed at Ground Tent 1.          |  |
|  +-------------------------------------------------+  |
|                                                       |
|  +-------------------------------------------------+  |
|  | [★ GOLD SHIELD] CAMPUS CLINIC ADVISORY           |  |
|  | From: Health Center Emergency Warden            |  |
|  | First-aid supplies available at Admin Block B.   |  |
|  +-------------------------------------------------+  |
+-------------------------------------------------------+
| [ SOS ] [ Bulletins (Active) ] [ Mesh Chat ] [ Map ]  |
+-------------------------------------------------------+
```

---

### 3.3 Screen 3: Peer-to-Peer Mesh Chat & Local Coordination

#### Operational Specification:
* **Peer Selection:** Lists discovered nearby students and faculty within physical multi-hop radio range.
* **End-to-End Encryption Indicator:** Clear lock icon indicating peer public key encryption (relays cannot inspect message content).
* **Delivery State Machine Indicators:**
  * `[O]` **Queued in Cache:** Holding in local SQLite memory; waiting for peer contact.
  * `[>>]` **Relayed / In Flight:** Broadcast to intermediate hop nodes.
  * `[✓✓]` **Delivered:** Target peer device acknowledged reception.

#### Screen 3 Wireframe:
```
+-------------------------------------------------------+
|  < P2P MESH CHAT: Rahul (Roll: 5124)       [Encrypted]|
+-------------------------------------------------------+
|  Target Peer: CSE Floor 2 (Stairwell Relay)           |
|  Security: E2EE via Ed25519/Curve25519                |
+-------------------------------------------------------+
|                                                       |
|  [Rahul - 10:18 AM]                                   |
|  Are you stuck on floor 3? Northern stairs are clear. |
|                                                 10:18 |
|                                                       |
|                      [You - 10:19 AM]                 |
|                      Yes, 3 of us here. One sprained  |
|                      ankle. Moving down stairs now.   |
|                                            [>> In Mesh|
|                                                       |
|                      [You - 10:20 AM]                 |
|                      Reached Floor 2 landing!         |
|                                            [✓✓ Delivered|
|                                                       |
+-------------------------------------------------------+
| [ Type offline message...              ] [ SEND (Mesh)]
+-------------------------------------------------------+
| [ SOS ] [ Bulletins ] [ Mesh Chat (Active) ] [ Map ]  |
+-------------------------------------------------------+
```

---

### 3.4 Screen 4: Offline Campus Map & Evacuation Muster Zones

#### Operational Specification:
* **100% Vector Pre-Cached Assets:** Renders from bundled local GeoJSON/SVG assets; zero external map tiles requested.
* **ANITS Campus Spatial Reference:** Displays CSE Block, ECE Block, Mechanical Block, Civil Block, Administrative Quadrangle, and Central Cricket Grounds.
* **Safe Evacuation Paths:** Highlights designated structural fire stairwells and green open-ground muster points.
* **Location Pinning:** Approximate position determined by strongest nearby BLE campus beacon.

#### Screen 4 Wireframe:
```
+-------------------------------------------------------+
|  CAMPUS MUSTER MAP        [● 14 Peers]      [Batt: 84%]|
+-------------------------------------------------------+
|  [!] USE STAIRWELLS ONLY - DO NOT USE ELEVATORS       |
|                                                       |
|  +-------------------+        +--------------------+  |
|  |  CSE BLOCK (You)  |        |    ECE BLOCK       |  |
|  |  [Floor 3 Alert]  |===\    |    [Cleared]       |  |
|  +---------+---------+    \   +---------+----------+  |
|            | Fire Exit     \            |             |
|            v                \           v             |
|  +-------------------------------------------------+  |
|  |        CENTRAL CRICKET GROUND MUSTER POINT      |  |
|  |        [★ SAFE ZONE - FIRST AID TENT 1]         |  |
|  +-------------------------------------------------+  |
|            ^                            ^             |
|            |                            |             |
|  +---------+---------+        +---------+----------+  |
|  |  MECHANICAL BLOCK |        |    ADMIN BLOCK     |  |
|  +-------------------+        +--------------------+  |
|                                                       |
|  Legend: [===] Safe Evacuation Corridor  [★] First Aid|
+-------------------------------------------------------+
| [ SOS ] [ Bulletins ] [ Mesh Chat ] [ Map (Active) ]  |
+-------------------------------------------------------+
```

---

## 4. Jetpack Compose Architecture & UI Component Specifications

### 4.1 Unidirectional Data Flow (UDF) Architecture
The UI layer adheres to Android's modern reactive declarative architecture:

```
┌────────────────────────────────────────────────────────┐
│                   JETPACK COMPOSE UI                   │
│   (State-Less Composables observing StateFlow)         │
└───────────────────────────▲────────────────────────────┘
                            │ UIState (Immutable Data Class)
                            │ User Intent / Events
┌───────────────────────────┴────────────────────────────┐
│                    VIEWMODEL LAYER                     │
│   (SosViewModel • BulletinViewModel • MeshViewModel)   │
└───────────────────────────▲────────────────────────────┘
                            │ Kotlin Coroutines Flow
┌───────────────────────────┴────────────────────────────┐
│                  MESH REPOSITORY LAYER                 │
│   (Room Database + BLE/Wi-Fi Aware Engine Daemons)     │
└────────────────────────────────────────────────────────┘
```

---

### 4.2 Jetpack Compose Theme & Color System Specification

```kotlin
package in.anits.mesh.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

val EmergencyRed = Color(0xFFEF4444)
val EmergencyRedDark = Color(0xFF991B1B)
val AuthorityGold = Color(0xFFF59E0B)
val AuthorityGoldDark = Color(0xFF78350F)
val MeshGreen = Color(0xFF10B981)
val DirectBlue = Color(0xFF3B82F6)
val OledBlack = Color(0xFF000000)
val SurfaceDark = Color(0xFF111827)
val SurfaceBorder = Color(0xFF374151)
val TextPrimary = Color(0xFFFFFFFF)
val TextMuted = Color(0xFF9CA3AF)

val DisasterColorScheme = darkColorScheme(
    primary = EmergencyRed,
    onPrimary = TextPrimary,
    secondary = AuthorityGold,
    onSecondary = TextPrimary,
    tertiary = MeshGreen,
    background = OledBlack,
    surface = SurfaceDark,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)
```

---

### 4.3 High-Fidelity Jetpack Compose Component Implementations

#### 1. Circular Emergency SOS Trigger Button (`EmergencySosButton.kt`)

```kotlin
package in.anits.mesh.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import in.anits.mesh.ui.theme.*

@Composable
fun EmergencySosButton(
    isTriggered: Boolean,
    onSosTriggered: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Pulse animation when standby or triggered
    val infiniteTransition = rememberInfiniteTransition(label = "SosPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isTriggered) 1.15f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isTriggered) 600 else 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(220.dp)
            .scale(pulseScale)
    ) {
        // Outer glow ring
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = if (isTriggered) EmergencyRed.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f),
                    shape = CircleShape
                )
        )

        // Interactive core button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(170.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isTriggered) listOf(MeshGreen, Color(0xFF047857))
                        else listOf(EmergencyRed, EmergencyRedDark)
                    ),
                    shape = CircleShape
                )
                .border(width = 4.dp, color = Color.White.copy(alpha = 0.8f), shape = CircleShape)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { onSosTriggered() },
                        onTap = { onSosTriggered() }
                    )
                }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isTriggered) "HELP\nEN ROUTE" else "EMERGENCY\nSOS",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isTriggered) "ACK RECEIVED" else "TAP OR HOLD",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
```

---

#### 2. Cryptographically Verified Gold Shield Notice Card (`GoldShieldNoticeCard.kt`)

```kotlin
package in.anits.mesh.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import in.anits.mesh.ui.theme.*

@Composable
fun GoldShieldNoticeCard(
    senderName: String,
    senderRole: String,
    timestampText: String,
    hopCount: Int,
    content: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = SurfaceDark, shape = RoundedCornerShape(12.dp))
            .border(width = 1.5.dp, color = AuthorityGold, shape = RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        // Top Header: Gold Shield + Verification Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "Gold Shield Authority",
                tint = AuthorityGold,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = senderName,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$senderRole • $timestampText",
                    color = AuthorityGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            // Hop Counter Badge
            Box(
                modifier = Modifier
                    .background(color = Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$hopCount Hops",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Directive Content Body
        Text(
            text = content,
            color = TextPrimary,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Cryptographic Verification Stamp
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(color = AuthorityGoldDark.copy(alpha = 0.25f), shape = RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Verified",
                tint = MeshGreen,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Cryptographically Verified Offline via Root PKI Ring",
                color = TextMuted,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
```

---

#### 3. Real-Time Mesh Health & Peer Count Bar (`MeshStatusBar.kt`)

```kotlin
package in.anits.mesh.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import in.anits.mesh.ui.theme.*

@Composable
fun MeshStatusBar(
    peerCount: Int,
    batteryPercent: Int,
    isBlackoutActive: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color.Black)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Mesh peer status
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color = if (peerCount > 0) MeshGreen else EmergencyRed, shape = CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (peerCount > 0) "$peerCount Nearby Peers Active" else "Scanning for Peers...",
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Battery & Mode
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isBlackoutActive) {
                Box(
                    modifier = Modifier
                        .background(color = EmergencyRedDark, shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "BLACKOUT",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = "Batt: $batteryPercent%",
                color = if (batteryPercent > 20) TextMuted else EmergencyRed,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
```

---

## 5. UI State Machine & Disaster Interaction Flow

```
                      [ USER LAUNCHES APP ]
                                │
                                ▼
                   ┌─────────────────────────┐
                   │  STATE 0: DISCOVERY     │
                   │  • Scanning BLE beacons │
                   │  • Locates ANITS Block  │
                   └────────────┬────────────┘
                                │
         ┌──────────────────────┴──────────────────────┐
         │                                             │
         ▼ (Taps SOS Button)                           ▼ (Receives Authority Broadcast)
┌─────────────────────────────┐               ┌─────────────────────────────┐
│  STATE 1: SOS ARMED         │               │  STATE 4: AUTHORITY DISPLAY │
│  • 3-Second Abort Countdown │               │  • Validates Ed25519 Sign   │
│  • Haptic Warning Pulse     │               │  • Displays Gold Shield Card│
└──────────────┬──────────────┘               └─────────────────────────────┘
               │ (Countdown expires)
               ▼
┌─────────────────────────────┐
│  STATE 2: BROADCASTING      │
│  • Emits Emergency Packet   │
│  • BLE Beacon + Wi-Fi Burst │
│  • Pulses Red Glow UI       │
└──────────────┬──────────────┘
               │
               ▼ (Clinic Node returns ACK)
┌─────────────────────────────┐
│  STATE 3: CONFIRMED (GREEN) │
│  • Screen transitions Green │
│  • Audio Alert: Help En Route│
│  • Displays Distance / Block│
└─────────────────────────────┘
```

---

## 6. Verification Checklist & Stage 3 Deliverables Complete

* [x] **Disaster Ergonomics & OLED Black Baseline:** High-contrast WCAG AAA color palette and zero-glare battery-conserving tokens defined.
* [x] **Primary Screen Workflows:** 4 full ASCII wireframes created for SOS, Authority Bulletins, P2P Chat, and Offline Muster Map.
* [x] **Low-Cognitive Load Interaction:** One-tap SOS armed with a 3-second abort window and auto-detected campus block location.
* [x] **Cryptographic Trust Indicators:** Gold Shield badge UI model for offline authentic authority directives.
* [x] **Production Jetpack Compose Components:** Fully written Kotlin Compose code for `EmergencySosButton`, `GoldShieldNoticeCard`, and `MeshStatusBar`.
* [x] **State Machine Specifications:** Formal reactive UI states mapped from standby to emergency dispatch and green return receipt.

---
*Next Step in Lifecycle: **Stage 4 — Modular Incremental Implementation (Sprint 1: Android Project Scaffolding, Room DB & Packet Wire Serializers)***.
