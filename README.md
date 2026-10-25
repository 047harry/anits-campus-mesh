# ANITS Decentralized Offline Campus Mesh Network
**Capstone Project (Team 12 — Dept of CSD, ANITS)**
*Students: P. Darakesu Hemanth & Team 12*

---

## Project Directory Organization

This repository is organized into distinct functional directories:

```
h:\Final dance\
│
├── docs/                      # Formal Engineering Documentation
│   ├── BLUEPRINT.md           # Master System Architecture & Threat Model
│   ├── LIFECYCLE.md           # 6-Stage Software Engineering Lifecycle Roadmap
│   ├── STAGE_1_REQUIREMENTS.md# Formal SRS (Personas, FRs, NFRs, Use Cases)
│   ├── STAGE_2_ARCHITECTURE.md# Technical Specs (70B Wire Protocol, FSM, Room DB)
│   └── STAGE_3_UI_UX_DESIGN.md# Disaster Ergonomics, WCAG AAA & Compose Specs
│
├── ppts/                      # Institutional Review Presentations
│   ├── Review 1.pptx          # Review Phase 1 Presentation
│   └── Review 2.pptx          # Review Phase 2 System Architecture Presentation
│
├── ui_ux/                     # Design Kits, Vector Artboards & Prototypes
│   ├── figma_mesh_ui_kit.svg  # Combined 4-Screen Vector Artboard (Drag to Figma)
│   ├── figma_screen_1_sos.svg # Screen 1: Emergency SOS Wireframe
│   ├── figma_screen_2_bulletins.svg # Screen 2: Gold Shield Notices Wireframe
│   ├── figma_screen_3_chat.svg# Screen 3: P2P Encrypted Mesh Chat Wireframe
│   ├── figma_screen_4_map.svg # Screen 4: Offline Campus Muster Map Wireframe
│   ├── interactive_prototype.html # Interactive Clickable Mobile Prototype
│   ├── diagram_workflow_mesh.png # Fig 2: Human-to-Human Mesh Relay Diagram
│   ├── diagram_inside_phone.png  # Fig 3: Smartphone 3-Tier Architecture Diagram
│   └── diagram_peacetime_vs_crisis.png # Fig 4: Peacetime vs Crisis & Guarantees
│
└── campus-mesh-android/       # Native Android Kotlin Implementation Codebase
    ├── build.gradle.kts
    ├── settings.gradle.kts
    └── app/                   # Android App Module (Room DB, BLE/NAN, Packet Serializer)
        ├── src/main/AndroidManifest.xml
        └── src/main/java/in/anits/mesh/
            ├── model/         # PacketType, UserRole, MeshPacket
            ├── protocol/      # PacketSerializer (70-byte header, CRC-32)
            ├── crypto/        # CryptoEngine (Ed25519 signing/verifying)
            ├── data/          # Room Database, Entities, DAOs
            └── ui/            # Jetpack Compose UI (Theme & Components)
```
