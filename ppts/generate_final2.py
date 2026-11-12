import sys
import os
import pptx
from pptx import Presentation
from pptx.util import Inches, Pt
from pptx.dml.color import RGBColor
from pptx.enum.text import PP_ALIGN, MSO_ANCHOR
from pptx.enum.shapes import MSO_SHAPE

def build_presentation():
    prs = Presentation()
    prs.slide_width = Inches(13.333)
    prs.slide_height = Inches(7.5)

    # Color Palette: Clean Dark Tech / Executive Aesthetic matching Review 2
    BG_DARK = RGBColor(15, 20, 26)       # #0F141A
    SURFACE_DARK = RGBColor(22, 31, 40)   # #161F28
    SURFACE_ELEV = RGBColor(29, 40, 52)   # #1D2834
    BORDER_COLOR = RGBColor(40, 54, 69)   # #283645
    
    PRIMARY_ORANGE = RGBColor(217, 107, 67) # #D96B43 (Review 2 signature accent)
    GOLD_ACCENT = RGBColor(246, 200, 95)    # #F6C85F (Authority Banana Gold)
    MINT_GREEN = RGBColor(56, 211, 159)     # #38D39F (Active Mesh Green)
    CYAN_ACCENT = RGBColor(74, 196, 226)    # #4AC4E2 (Relay Cyan)
    RED_ALERT = RGBColor(242, 93, 107)      # #F25D6B (SOS Crimson)
    
    TEXT_LIGHT = RGBColor(240, 244, 248)    # #F0F4F8 (Crisp Primary)
    TEXT_MUTED = RGBColor(155, 174, 192)    # #9BAEC0 (Secondary)
    TEXT_HINT = RGBColor(100, 116, 139)     # #64748B (Tertiary)

    def add_base_slide(section_title, slide_heading):
        blank_slide_layout = prs.slide_layouts[6]
        slide = prs.slides.add_slide(blank_slide_layout)
        
        # Background
        bg = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, 0, 0, prs.slide_width, prs.slide_height)
        bg.fill.solid()
        bg.fill.fore_color.rgb = BG_DARK
        bg.line.fill.background()

        # Top Bar
        top_bar = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, 0, 0, prs.slide_width, Inches(0.42))
        top_bar.fill.solid()
        top_bar.fill.fore_color.rgb = RGBColor(11, 15, 20)
        top_bar.line.fill.background()

        tf_top = top_bar.text_frame
        tf_top.vertical_anchor = MSO_ANCHOR.MIDDLE
        p_top = tf_top.paragraphs[0]
        p_top.alignment = PP_ALIGN.LEFT
        r1 = p_top.add_run()
        r1.text = "   ANIL NEERUKONDA INSTITUTE OF TECHNOLOGY & SCIENCES (ANITS)  |  DEPT. OF CSD"
        r1.font.bold = True
        r1.font.size = Pt(9.5)
        r1.font.color.rgb = TEXT_MUTED

        r2 = p_top.add_run()
        r2.text = "       TEAM 12  -  DECENTRALIZED CAMPUS MESH NETWORK (PHASE 2 / FINAL)"
        r2.font.bold = False
        r2.font.size = Pt(9.5)
        r2.font.color.rgb = TEXT_HINT

        # Header Box
        header_box = slide.shapes.add_textbox(Inches(0.8), Inches(0.55), Inches(11.7), Inches(0.95))
        tf_h = header_box.text_frame
        tf_h.word_wrap = True
        p_sec = tf_h.paragraphs[0]
        p_sec.text = section_title.upper()
        p_sec.font.bold = True
        p_sec.font.size = Pt(10.5)
        p_sec.font.color.rgb = PRIMARY_ORANGE

        p_head = tf_h.add_paragraph()
        p_head.text = slide_heading
        p_head.font.bold = True
        p_head.font.size = Pt(22)
        p_head.font.color.rgb = TEXT_LIGHT

        # Bottom Footer
        footer = slide.shapes.add_textbox(Inches(0.8), Inches(7.05), Inches(11.7), Inches(0.35))
        tf_f = footer.text_frame
        p_f = tf_f.paragraphs[0]
        p_f.text = "ANITS B.Tech Final Year Capstone Project (2026-2027)  •  Final Phase Implementation & Deployment"
        p_f.font.size = Pt(8.5)
        p_f.font.color.rgb = TEXT_HINT

        return slide

    def add_card(slide, left, top, width, height, title="", title_color=PRIMARY_ORANGE, bg_color=SURFACE_DARK, border_color=BORDER_COLOR):
        card = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, left, top, width, height)
        card.fill.solid()
        card.fill.fore_color.rgb = bg_color
        card.line.color.rgb = border_color
        card.line.width = Pt(1.2)
        
        tf = card.text_frame
        tf.word_wrap = True
        tf.margin_left = Inches(0.25)
        tf.margin_right = Inches(0.25)
        tf.margin_top = Inches(0.2)
        tf.margin_bottom = Inches(0.2)
        
        if title:
            p = tf.paragraphs[0]
            p.text = title
            p.font.bold = True
            p.font.size = Pt(13)
            p.font.color.rgb = title_color
            p.space_after = Pt(8)
        return card

    # =========================================================================
    # SLIDE 1: TITLE SLIDE (Matches Review 2 Executive Dark Style)
    # =========================================================================
    slide1 = prs.slides.add_slide(prs.slide_layouts[6])
    bg1 = slide1.shapes.add_shape(MSO_SHAPE.RECTANGLE, 0, 0, prs.slide_width, prs.slide_height)
    bg1.fill.solid()
    bg1.fill.fore_color.rgb = BG_DARK
    bg1.line.fill.background()

    logo_path = r'H:\Final dance\ppts\anits_logo.png'
    if os.path.exists(logo_path):
        slide1.shapes.add_picture(logo_path, Inches(0.8), Inches(0.6), height=Inches(1.1))

    title_box = slide1.shapes.add_textbox(Inches(2.2), Inches(0.55), Inches(10.3), Inches(1.3))
    tf_t1 = title_box.text_frame
    tf_t1.word_wrap = True
    p_inst = tf_t1.paragraphs[0]
    p_inst.text = "ANIL NEERUKONDA INSTITUTE OF TECHNOLOGY AND SCIENCES (A)"
    p_inst.font.bold = True
    p_inst.font.size = Pt(12)
    p_inst.font.color.rgb = PRIMARY_ORANGE

    p_sub = tf_t1.add_paragraph()
    p_sub.text = "Department of Computer Science & Engineering (Data Science) — CSD"
    p_sub.font.bold = False
    p_sub.font.size = Pt(10)
    p_sub.font.color.rgb = TEXT_MUTED

    p_badge = tf_t1.add_paragraph()
    p_badge.text = "Autonomous Institution | Affiliated to AU | NAAC A+ Grade | Accredited by NBA"
    p_badge.font.size = Pt(8.5)
    p_badge.font.color.rgb = TEXT_HINT

    # Main Project Title Box
    main_box = slide1.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(0.8), Inches(2.0), Inches(11.733), Inches(2.4))
    main_box.fill.solid()
    main_box.fill.fore_color.rgb = SURFACE_DARK
    main_box.line.color.rgb = BORDER_COLOR
    tf_m = main_box.text_frame
    tf_m.margin_left = Inches(0.4)
    tf_m.margin_top = Inches(0.3)
    
    p1 = tf_m.paragraphs[0]
    p1.text = "FINAL CAPSTONE PROJECT REVIEW — PHASE 2 / FINAL EVALUATION"
    p1.font.bold = True
    p1.font.size = Pt(11)
    p1.font.color.rgb = GOLD_ACCENT

    p2 = tf_m.add_paragraph()
    p2.text = "DECENTRALIZED OFFLINE-FIRST CAMPUS MESH NETWORK"
    p2.font.bold = True
    p2.font.size = Pt(24)
    p2.font.color.rgb = TEXT_LIGHT

    p3 = tf_m.add_paragraph()
    p3.text = "Real-World Multi-Hop Bluetooth LE & Wi-Fi LAN Mesh Engine with Cryptographic Trust, Role-Based Emergency Routing & Production Android Deployment"
    p3.font.size = Pt(12)
    p3.font.color.rgb = CYAN_ACCENT

    # Team Members Card
    add_card(slide1, Inches(0.8), Inches(4.6), Inches(7.2), Inches(2.3), "PROJECT MEMBERS (TEAM 12 — CSD-B)", PRIMARY_ORANGE)
    team_tf = slide1.shapes[-1].text_frame
    members = [
        ("P. Darakesu Hemanth (Lead)", "Reg No: A23126551113", "Routing Engine & Android Coordinator"),
        ("Ruthvik Katcha", "Reg No: A23126551119", "DTN Storage, SQLite & Protocol Layer"),
        ("Kusa Madhuri", "Reg No: A23126551098", "Cryptographic Signatures & Authority Trust"),
        ("Garbhapu Sirisha", "Reg No: A23126551084", "UI/UX Ergonomics & Web Distribution Portal")
    ]
    for name, reg, role in members:
        p = team_tf.add_paragraph()
        r_n = p.add_run()
        r_n.text = f"• {name} "
        r_n.font.bold = True
        r_n.font.size = Pt(10)
        r_n.font.color.rgb = TEXT_LIGHT

        r_r = p.add_run()
        r_r.text = f"({reg}) — "
        r_r.font.size = Pt(9.5)
        r_r.font.color.rgb = TEXT_MUTED

        r_ro = p.add_run()
        r_ro.text = role
        r_ro.font.size = Pt(9.5)
        r_ro.font.color.rgb = MINT_GREEN

    # Guidance Card
    add_card(slide1, Inches(8.2), Inches(4.6), Inches(4.333), Inches(2.3), "PROJECT GUIDANCE", PRIMARY_ORANGE)
    guide_tf = slide1.shapes[-1].text_frame
    p_g1 = guide_tf.add_paragraph()
    p_g1.text = "Under the Esteemed Guidance of:"
    p_g1.font.size = Pt(10)
    p_g1.font.color.rgb = TEXT_MUTED

    p_g2 = guide_tf.add_paragraph()
    p_g2.text = "Mrs. A. Bhagya Lakshmi"
    p_g2.font.bold = True
    p_g2.font.size = Pt(13)
    p_g2.font.color.rgb = GOLD_ACCENT

    p_g3 = guide_tf.add_paragraph()
    p_g3.text = "Assistant Professor\nDepartment of Computer Science & Engineering (Data Science)\nANITS, Visakhapatnam"
    p_g3.font.size = Pt(10)
    p_g3.font.color.rgb = TEXT_MUTED

    # =========================================================================
    # SLIDE 2: AGENDA & PHASE 2 MILESTONES
    # =========================================================================
    slide2 = add_base_slide("AGENDA & IMPLEMENTATION SCOPE", "Phase 2 Final Presentation Outline")
    
    col1 = add_card(slide2, Inches(0.8), Inches(1.7), Inches(5.6), Inches(5.1), "CORE ENGINEERING MODULES", PRIMARY_ORANGE)
    c1_tf = col1.text_frame
    modules_1 = [
        ("01. Architecture Overview & Tech Stack", "Android Native (Kotlin), Room SQLite, BLE 5.0 GATT, Wi-Fi LAN UDP, Ed25519 Cryptography."),
        ("02. Real Hardware Mesh Radios", "Dual-radio BLE + LAN presence beacons, active hardware scan filtering, self-healing Bluetooth watchdog."),
        ("03. Autonomous Store-and-Forward DTN", "Room database persistence, 70-byte packet serialization, 7-hop dynamic TTL relay across dead zones."),
        ("04. Role-Based Stakeholder Hierarchy", "6 distinct institutional authority tiers (Principal, HOD, Warden, Clinic, Security, Student)."),
        ("05. Verified Campus Radio Bulletins", "Asymmetric cryptographic signature verification; instant high-priority emergency flood to student pocket devices.")
    ]
    for title, desc in modules_1:
        p_t = c1_tf.add_paragraph()
        p_t.text = title
        p_t.font.bold = True
        p_t.font.size = Pt(11)
        p_t.font.color.rgb = GOLD_ACCENT
        p_d = c1_tf.add_paragraph()
        p_d.text = desc
        p_d.font.size = Pt(9.5)
        p_d.font.color.rgb = TEXT_MUTED
        p_d.space_after = Pt(6)

    col2 = add_card(slide2, Inches(6.8), Inches(1.7), Inches(5.7), Inches(5.1), "DEPLOYMENT & EVALUATION", PRIMARY_ORANGE)
    c2_tf = col2.text_frame
    modules_2 = [
        ("06. Private Encrypted Departmental Chat", "Peer-to-peer AES-GCM encrypted 1-on-1 conversations and multi-hop campus departmental routing."),
        ("07. Unified Ergonomic UI & Settings Suite", "Eye-friendly velvety matte dark theme, unified banana identity header, dedicated settings tab."),
        ("08. Multi-Device Real-World Validation", "Physical test results across multiple devices (Vivo, OnePlus, Samsung Tab) in zero-internet blackout conditions."),
        ("09. Production Release & Web Distribution", "Automated GitHub Releases (v1.0.6 APK) with live QR download portal on GitHub Pages & Netlify."),
        ("10. Conclusion & Future Roadmap", "Disaster impact assessment, off-grid scalability, campus emergency readiness.")
    ]
    for title, desc in modules_2:
        p_t = c2_tf.add_paragraph()
        p_t.text = title
        p_t.font.bold = True
        p_t.font.size = Pt(11)
        p_t.font.color.rgb = CYAN_ACCENT
        p_d = c2_tf.add_paragraph()
        p_d.text = desc
        p_d.font.size = Pt(9.5)
        p_d.font.color.rgb = TEXT_MUTED
        p_d.space_after = Pt(6)

    # =========================================================================
    # SLIDE 3: SYSTEM ARCHITECTURE & 3-TIER ENGINE
    # =========================================================================
    slide3 = add_base_slide("SYSTEM ARCHITECTURE & SOFTWARE STACK", "3-Tier Android Architecture (Native Kotlin + Radios)")
    
    t1 = add_card(slide3, Inches(0.8), Inches(1.7), Inches(3.6), Inches(5.1), "TIER 1: PRESENTATION & UI", MINT_GREEN)
    tf1 = t1.text_frame
    items_t1 = [
        "Jetpack Compose UI", "Declarative reactive UI framework",
        "Disaster Color Palette", "Velvety obsidian (#0F141A) with eye-friendly mint & banana gold accents",
        "Unified Banana Header", "Real-time live peer node indicator ('X Nodes Online') & instant account switcher",
        "5 Core Screens", "• Campus SOS (Preemptive)\n• Radio Bulletins (Gold Shield)\n• Mesh Chat (Private + Broadcast)\n• Muster Map (Offline Vector)\n• Settings & Identity (Keys & Profile)"
    ]
    for i in range(0, len(items_t1), 2):
        p = tf1.add_paragraph()
        p.text = "• " + items_t1[i]
        p.font.bold = True
        p.font.size = Pt(10.5)
        p.font.color.rgb = TEXT_LIGHT
        p2 = tf1.add_paragraph()
        p2.text = items_t1[i+1]
        p2.font.size = Pt(9)
        p2.font.color.rgb = TEXT_MUTED
        p2.space_after = Pt(4)

    t2 = add_card(slide3, Inches(4.7), Inches(1.7), Inches(3.9), Inches(5.1), "TIER 2: DTN BRAIN & PROTOCOL", CYAN_ACCENT)
    tf2 = t2.text_frame
    items_t2 = [
        "RadioCoordinator", "Central multiplexer controlling BLE scanning, advertising, and UDP broadcast daemons",
        "70-Byte Wire Frame", "Compact binary serializer with 4-byte Magic, TTL, Hop Count, and CRC-32 validation",
        "Room SQLite Database", "Store-and-forward persistent DTN vault (`table_messages`, `table_peers`, `table_cache`)",
        "SeenPacketCache", "Deduplication ring buffer stopping broadcast storms and routing loops across hops",
        "Bouncy Castle Ed25519", "Sub-5ms asymmetric cryptographic signature generation and verification"
    ]
    for i in range(0, len(items_t2), 2):
        p = tf2.add_paragraph()
        p.text = "• " + items_t2[i]
        p.font.bold = True
        p.font.size = Pt(10.5)
        p.font.color.rgb = TEXT_LIGHT
        p2 = tf2.add_paragraph()
        p2.text = items_t2[i+1]
        p2.font.size = Pt(9)
        p2.font.color.rgb = TEXT_MUTED
        p2.space_after = Pt(4)

    t3 = add_card(slide3, Inches(8.9), Inches(1.7), Inches(3.6), Inches(5.1), "TIER 3: HARDWARE RADIOS", GOLD_ACCENT)
    tf3 = t3.text_frame
    items_t3 = [
        "BLE 5.0 Advertiser", "Broadcasts 20-byte manufacturer payload (Roll No, Role, Status) @ 100ms interval",
        "BLE Low-Latency Scanner", "Hardware ScanFilter (ID 0x02E5) with 12s active scan cycling to prevent OS sleep",
        "Wi-Fi LAN UDP Relay", "Multicast UDP socket broadcasting local 1-hop presence beacons every 4 seconds",
        "MeshRadioService", "Android Foreground Service with Partial WakeLock ensuring disaster survival during doze",
        "Bluetooth State Receiver", "Self-healing BroadcastReceiver auto-restarting mesh when Bluetooth is toggled"
    ]
    for i in range(0, len(items_t3), 2):
        p = tf3.add_paragraph()
        p.text = "• " + items_t3[i]
        p.font.bold = True
        p.font.size = Pt(10.5)
        p.font.color.rgb = TEXT_LIGHT
        p2 = tf3.add_paragraph()
        p2.text = items_t3[i+1]
        p2.font.size = Pt(9)
        p2.font.color.rgb = TEXT_MUTED
        p2.space_after = Pt(4)

    # =========================================================================
    # SLIDE 4: HARDWARE MESH RADIOS & ZERO-BLACKOUT DISCOVERY
    # =========================================================================
    slide4 = add_base_slide("HARDWARE TRANSPORT & RADIO MANAGEMENT", "Dual-Radio Peer Discovery & Self-Healing Bluetooth Engine")
    
    c_rad1 = add_card(slide4, Inches(0.8), Inches(1.7), Inches(5.6), Inches(5.1), "REAL HARDWARE DUAL-RADIO STACK", MINT_GREEN)
    tr1 = c_rad1.text_frame
    rad_pts = [
        ("BLE 5.0 Peripheral Advertising", "Transmits node presence via non-connectable and connectable manufacturer data frames. Operates without requiring Bluetooth device pairing or bond handshakes."),
        ("Hardware ScanFilter (ANITS 0x02E5)", "Configured with hardware manufacturer filter to offload packet parsing directly to the Bluetooth chipset, slashing CPU usage to < 2% and conserving emergency battery."),
        ("12-Second Active Scan Cycling", "Android OS silently throttles continuous BLE scanners after 30 minutes. Our daemon implements an asynchronous coroutine cycle that flushes and restarts the scanner every 12 seconds."),
        ("UDP Multicast Broadcast (Port 8888)", "Whenever devices are connected to an offline campus Wi-Fi router (with no internet), UDP datagrams flood heartbeat beacons across the entire LAN subnet in < 10 milliseconds.")
    ]
    for h, d in rad_pts:
        p = tr1.add_paragraph()
        p.text = "✔ " + h
        p.font.bold = True
        p.font.size = Pt(10.5)
        p.font.color.rgb = TEXT_LIGHT
        p2 = tr1.add_paragraph()
        p2.text = d
        p2.font.size = Pt(9)
        p2.font.color.rgb = TEXT_MUTED
        p2.space_after = Pt(6)

    c_rad2 = add_card(slide4, Inches(6.8), Inches(1.7), Inches(5.7), Inches(5.1), "SELF-HEALING BLUETOOTH WATCHDOG", PRIMARY_ORANGE)
    tr2 = c_rad2.text_frame
    wh_pts = [
        ("Automatic Bluetooth Turn-On Prompt", "On application launch and on resume, if the device's Bluetooth radio is switched off, the system automatically triggers `BluetoothAdapter.ACTION_REQUEST_ENABLE` to ensure radios are always armed."),
        ("Dynamic System State Receiver", "Listens to Android OS `BluetoothAdapter.ACTION_STATE_CHANGED`. When a user toggles Bluetooth in the quick settings shade, the coordinator immediately detects the state change."),
        ("Instant Auto-Rebinding", "Upon transitioning to `STATE_ON`, the `RadioCoordinator` automatically tears down any stale GATT sockets and relaunches both the BLE Scanner and Advertiser Daemons within 250ms."),
        ("Zero Device Discovery Deadlock", "Eliminates the legacy Android flaw where devices coming back into physical range fail to re-detect each other. Verified seamlessly on OnePlus & Vivo hardware.")
    ]
    for h, d in wh_pts:
        p = tr2.add_paragraph()
        p.text = "✔ " + h
        p.font.bold = True
        p.font.size = Pt(10.5)
        p.font.color.rgb = TEXT_LIGHT
        p2 = tr2.add_paragraph()
        p2.text = d
        p2.font.size = Pt(9)
        p2.font.color.rgb = TEXT_MUTED
        p2.space_after = Pt(6)

    # =========================================================================
    # SLIDE 5: DELAY-TOLERANT (DTN) STORE-AND-FORWARD ENGINE
    # =========================================================================
    slide5 = add_base_slide("NETWORKING PROTOCOL & DTN VAULT", "Store-and-Forward Mesh Engine with 7-Hop Disaster TTL")
    
    d1 = add_card(slide5, Inches(0.8), Inches(1.7), Inches(3.7), Inches(5.1), "70-BYTE WIRE SPECIFICATION", CYAN_ACCENT)
    df1 = d1.text_frame
    wire_info = [
        ("Magic Bytes (4B)", "0x41, 0x4E, 0x49, 0x54 ('ANIT') for instant protocol filtering."),
        ("Version (1B)", "0x01 (Current Wire Protocol Specification)."),
        ("Packet Type (1B)", "0x00: SOS Beacon (Preemptive)\n0x01: P2P Chat (Interactive)\n0x02: Authority Radio Directive\n0x03: ACK / Delivery Receipt\n0x04: Discovery Heartbeat"),
        ("TTL & Hop Count (2B)", "Initial TTL = 7; Hop count increments at each repeater node."),
        ("Node Addresses (16B)", "Sender Node ID (8B) & Recipient Node ID (8B) / Broadcast (0xFF..FF)."),
        ("CRC-32 Checksum (4B)", "Hardware-accelerated cyclic redundancy check for data integrity.")
    ]
    for h, d in wire_info:
        p = df1.add_paragraph()
        p.text = h
        p.font.bold = True
        p.font.size = Pt(10)
        p.font.color.rgb = GOLD_ACCENT
        p2 = df1.add_paragraph()
        p2.text = d
        p2.font.size = Pt(8.5)
        p2.font.color.rgb = TEXT_MUTED
        p2.space_after = Pt(4)

    d2 = add_card(slide5, Inches(4.8), Inches(1.7), Inches(3.7), Inches(5.1), "ROOM SQLITE DTN VAULT", MINT_GREEN)
    df2 = d2.text_frame
    dtn_info = [
        ("Asynchronous Store-and-Forward", "If the intended recipient or intermediate relay is out of range, the packet is persisted into local Room SQLite. When a new peer enters proximity, unsent packets are forwarded automatically."),
        ("Hop-by-Hop Verification", "Packets are decremented ($TTL = TTL - 1$) and re-broadcasted. Packets reach $TTL = 0$ safely terminate without flooding."),
        ("Offline Multi-Hop Routing", "Enables messages originating in the CSE block to traverse intermediate student devices across Central Ground and reach Mechanical block with zero infrastructure."),
        ("SeenPacketCache Deduplication", "In-memory LRU ring buffer tracks received message UUIDs, immediately discarding duplicate packets and preventing broadcast storm collapse.")
    ]
    for h, d in dtn_info:
        p = df2.add_paragraph()
        p.text = h
        p.font.bold = True
        p.font.size = Pt(10)
        p.font.color.rgb = MINT_GREEN
        p2 = df2.add_paragraph()
        p2.text = d
        p2.font.size = Pt(8.5)
        p2.font.color.rgb = TEXT_MUTED
        p2.space_after = Pt(4)

    d3 = add_card(slide5, Inches(8.8), Inches(1.7), Inches(3.7), Inches(5.1), "3-TIER PREEMPTIVE QOS", RED_ALERT)
    df3 = d3.text_frame
    qos_info = [
        ("Tier 1: Emergency SOS (Highest)", "Preempts all pending radio traffic. SOS packets bypass DTN queue delays and are flooded immediately across all available BLE channels with haptic feedback."),
        ("Tier 2: Authority Directives (High)", "Radio Bulletins signed by HOD / Principal. Delivered with high priority and displayed with Gold Shield verification badges."),
        ("Tier 3: Peer-to-Peer Chat (Standard)", "End-to-end encrypted student chat messages queued behind emergency traffic to prevent radio saturation."),
        ("Deterministic Delivery Guarantees", "Ensures critical life-safety coordinates reach campus first responders even in saturated wireless environments.")
    ]
    for h, d in qos_info:
        p = df3.add_paragraph()
        p.text = h
        p.font.bold = True
        p.font.size = Pt(10)
        p.font.color.rgb = RED_ALERT
        p2 = df3.add_paragraph()
        p2.text = d
        p2.font.size = Pt(8.5)
        p2.font.color.rgb = TEXT_MUTED
        p2.space_after = Pt(4)

    # =========================================================================
    # SLIDE 6: ROLE-BASED AUTHORITY & CAMPUS RADIO DIRECTIVES
    # =========================================================================
    slide6 = add_base_slide("INSTITUTIONAL TRUST & BROADCAST RADIO", "Campus Radio Bulletins & Stakeholder Role Hierarchy")

    r_left = add_card(slide6, Inches(0.8), Inches(1.7), Inches(5.6), Inches(5.1), "INSTITUTIONAL HIERARCHY (6 TIERS)", GOLD_ACCENT)
    rf_l = r_left.text_frame
    roles = [
        ("🏛️ Tier 1: Principal & Dean", "Global broadcast authority. Capable of issuing institution-wide evacuation orders and campus disaster declarations."),
        ("⭐ Tier 2: Head of Department (HOD)", "Departmental broadcast authority. Issues block-level instructions, floor evacuation notices, and emergency roll calls."),
        ("🛡️ Tier 3: Faculty & Hostel Warden", "Local sector supervision. Broadcasts hostel safety alerts, shelter-in-place updates, and status verification."),
        ("🚨 Tier 4: Campus Clinic & Security", "Triage response. Receives SOS beacons, casualty coordinates, and coordinates medical evacuation."),
        ("🎓 Tier 5: Student & Campus Occupant", "Standard mesh participant. Relays packets, broadcasts 1-on-1 private encrypted chat, and triggers SOS beacons."),
        ("⚙️ Tier 0: Root Infrastructure Admin", "Network health monitoring, cryptographic root key rotation, and audit verification.")
    ]
    for title, desc in roles:
        p = rf_l.add_paragraph()
        p.text = title
        p.font.bold = True
        p.font.size = Pt(10.5)
        p.font.color.rgb = TEXT_LIGHT
        p2 = rf_l.add_paragraph()
        p2.text = desc
        p2.font.size = Pt(9)
        p2.font.color.rgb = TEXT_MUTED
        p2.space_after = Pt(4)

    r_right = add_card(slide6, Inches(6.8), Inches(1.7), Inches(5.7), Inches(5.1), "CAMPUS RADIO BROADCASTING IN ACTION", PRIMARY_ORANGE)
    rf_r = r_right.text_frame
    b_pts = [
        ("Offline Authority Bulletin Flooding", "When an HOD (e.g. on Vivo) taps 'Broadcast Directive', the bulletin is immediately committed to local Room SQLite (`packet_type = 2`) and broadcast over peer BLE/LAN with maximum priority (`TTL = 7`)."),
        ("Autonomous Relay Across Student Nodes", "Intermediate student devices (e.g. OnePlus) receive the packet, verify the sender's role, display the notice on their Radio tab, decrement TTL, and re-transmit to other nearby devices."),
        ("Gold Shield Notice Verification", "Incoming directives feature cryptographic badges verifying that the notice originated from a legitimate ANITS authority, preventing disaster spoofing or panic tampering."),
        ("Hop Count & Latency Metrics", "Each notice displays the exact number of smartphone hops it traversed (e.g. 'Hop Count: 2 Hops • Verified Ed25519'), confirming the active mesh relay path in real time.")
    ]
    for title, desc in b_pts:
        p = rf_r.add_paragraph()
        p.text = "✔ " + title
        p.font.bold = True
        p.font.size = Pt(10.5)
        p.font.color.rgb = GOLD_ACCENT
        p2 = rf_r.add_paragraph()
        p2.text = desc
        p2.font.size = Pt(9)
        p2.font.color.rgb = TEXT_MUTED
        p2.space_after = Pt(6)

    # =========================================================================
    # SLIDE 7: PEER-TO-PEER ENCRYPTED CHAT & MULTI-HOP DEPARTMENT PICKER
    # =========================================================================
    slide7 = add_base_slide("DISASTER CHAT & DEPARTMENTAL ROUTING", "End-to-End Encrypted Messaging & Campus Directory Discovery")

    c_c1 = add_card(slide7, Inches(0.8), Inches(1.7), Inches(5.6), Inches(5.1), "CAMPUS MULTI-HOP PRIVATE CHAT", MINT_GREEN)
    cf1 = c_c1.text_frame
    chat_pts = [
        ("General Campus Broadcast Channel", "A public channel where all nodes in range listen and relay news, muster point confirmations, and general safety updates across the student body."),
        ("End-to-End Encrypted 1-on-1 Rooms", "Private peer-to-peer conversations encrypted with AES-GCM session keys derived via Ed25519 ECDH key exchange. Eavesdropping by relaying intermediate nodes is mathematically impossible."),
        ("Department & Roll Number Selector", "Users can initiate a private multi-hop chat by selecting any campus department (CSD, CSE, CSM, CIC, IT, ECE, EEE, Mech, Civil) and selecting an active peer or typing a roll number."),
        ("Local Conversation Deletion", "WhatsApp-style long-press action allows occupants to clear conversation threads and remove chats from their local interface without affecting the remote peer's history.")
    ]
    for title, desc in chat_pts:
        p = cf1.add_paragraph()
        p.text = "• " + title
        p.font.bold = True
        p.font.size = Pt(10.5)
        p.font.color.rgb = TEXT_LIGHT
        p2 = cf1.add_paragraph()
        p2.text = desc
        p2.font.size = Pt(9)
        p2.font.color.rgb = TEXT_MUTED
        p2.space_after = Pt(6)

    c_c2 = add_card(slide7, Inches(6.8), Inches(1.7), Inches(5.7), Inches(5.1), "OFFLINE DIRECTORY & SILENT CLOUD SYNC", CYAN_ACCENT)
    cf2 = c_c2.text_frame
    sync_pts = [
        ("Pre-Crisis Silent Directory Sync", "During peacetime (when Internet is available), user profiles, public keys, roles, and node IDs sync silently to Supabase Cloud in an 8-second background loop."),
        ("Persistent Local Disk Cache", "The campus directory is serialized to local JSON on device flash storage. If the Internet collapses abruptly, the app operates 100% offline with zero degradation."),
        ("Automatic Device Unlinking on Logout", "When a user logs out or switches accounts, the server node mapping is automatically flagged as 'offline' so peers in the network are never misled by stale device mappings."),
        ("Instant Auto-Discovery of New Occupants", "When a new peer registers and comes into radio range, their name, roll number, and role badge appear automatically in the peer list without manual refresh.")
    ]
    for title, desc in sync_pts:
        p = cf2.add_paragraph()
        p.text = "• " + title
        p.font.bold = True
        p.font.size = Pt(10.5)
        p.font.color.rgb = CYAN_ACCENT
        p2 = cf2.add_paragraph()
        p2.text = desc
        p2.font.size = Pt(9)
        p2.font.color.rgb = TEXT_MUTED
        p2.space_after = Pt(6)

    # =========================================================================
    # SLIDE 8: MODERN ERGONOMIC UI & SETTINGS SUITE
    # =========================================================================
    slide8 = add_base_slide("USER EXPERIENCE & INTERFACE SUITE", "Matte Ergonomic Theme, Unified Header & Dedicated Settings Tab")

    u1 = add_card(slide8, Inches(0.8), Inches(1.7), Inches(3.7), Inches(5.1), "EYE-FRIENDLY MATTE PALETTE", PRIMARY_ORANGE)
    uf1 = u1.text_frame
    u_palette = [
        ("Obsidian Background (#0F141A)", "Velvety, true dark mode minimizing OLED battery drain in survival scenarios."),
        ("Smooth Card Surfaces (#161F28)", "Refined contrast hierarchy preventing visual glare during night-time blackouts."),
        ("Calming Mint Accent (#38D39F)", "Psychologically calming status indicator replacing harsh fluorescent greens."),
        ("Warm Banana Gold (#F6C85F)", "Official authority color token indicating verified directives and app identity."),
        ("Soft Crimson (#F25D6B)", "High-visibility warning tone reserved strictly for active SOS alerts and hazard notices.")
    ]
    for title, desc in u_palette:
        p = uf1.add_paragraph()
        p.text = title
        p.font.bold = True
        p.font.size = Pt(10)
        p.font.color.rgb = GOLD_ACCENT
        p2 = uf1.add_paragraph()
        p2.text = desc
        p2.font.size = Pt(8.5)
        p2.font.color.rgb = TEXT_MUTED
        p2.space_after = Pt(4)

    u2 = add_card(slide8, Inches(4.8), Inches(1.7), Inches(3.7), Inches(5.1), "UNIFIED BANANA HEADER", GOLD_ACCENT)
    uf2 = u2.text_frame
    u_header = [
        ("Closed Banana Brand Icon 🍌", "Distinctive, unpeeled campus mesh emblem consistent across launcher and UI."),
        ("Dynamic Screen Navigation Title", "Displays context: 'CAMPUS SOS', 'CAMPUS RADIO', 'MESH CHAT', 'MUSTER MAP', 'SETTINGS'."),
        ("Real-Time Peer Node Counter", "Displays 'X Nodes Online' with pulsating green LED or 'Discovering Nodes...' if alone."),
        ("Occupant Identity Chip", "Pill displaying active user's initial, name, and roll number with 1-tap account switch."),
        ("Elimination of Clutter", "Replaced outdated blackout percentages with clean, high-abstraction status indicators.")
    ]
    for title, desc in u_header:
        p = uf2.add_paragraph()
        p.text = title
        p.font.bold = True
        p.font.size = Pt(10)
        p.font.color.rgb = MINT_GREEN
        p2 = uf2.add_paragraph()
        p2.text = desc
        p2.font.size = Pt(8.5)
        p2.font.color.rgb = TEXT_MUTED
        p2.space_after = Pt(4)

    u3 = add_card(slide8, Inches(8.8), Inches(1.7), Inches(3.7), Inches(5.1), "5TH TAB: SETTINGS & IDENTITY", CYAN_ACCENT)
    uf3 = u3.text_frame
    u_settings = [
        ("Institutional Credentials Card", "Displays Roll Number, Department, and Verified Authority status."),
        ("Hardware & Radio Node Card", "Surfaces active Node ID, Ed25519 256-bit public key, and active radio layers."),
        ("Default Password Architecture", "Streamlined with default preset password '1234' for rapid campus testing & onboarding."),
        ("1-Tap Account Switch & Logout", "Safely unlinks device, clears memory session, stops radios, and resets state."),
        ("Modern 24dp Rounded Navigation", "Floating bottom hotbar with soft haptic touch feedback and clear icon hierarchy.")
    ]
    for title, desc in u_settings:
        p = uf3.add_paragraph()
        p.text = title
        p.font.bold = True
        p.font.size = Pt(10)
        p.font.color.rgb = CYAN_ACCENT
        p2 = uf3.add_paragraph()
        p2.text = desc
        p2.font.size = Pt(8.5)
        p2.font.color.rgb = TEXT_MUTED
        p2.space_after = Pt(4)

    # =========================================================================
    # SLIDE 9: MULTI-DEVICE PHYSICAL TEST RESULTS & METRICS
    # =========================================================================
    slide9 = add_base_slide("EXPERIMENTAL VALIDATION & FIELD METRICS", "Multi-Device Hardware Testing (Zero-Cellular / Total Blackout)")

    t_box = add_card(slide9, Inches(0.8), Inches(1.7), Inches(11.733), Inches(5.1), "RIGOROUS HARDWARE TEST MATRIX (VIVO + ONEPLUS + SAMSUNG)", PRIMARY_ORANGE)
    tf_test = t_box.text_frame

    test_runs = [
        ("Test 1: Authority Directive Mesh Relay (Vivo HOD ➔ OnePlus Student)", 
         "Vivo logged in as HOD ('FAC-CSD-01'); OnePlus logged in as student 'b (2)'. Both devices disconnected from Internet/cellular. HOD broadcasts evacuation notice. OnePlus receives Gold Shield card via BLE mesh in 340ms across physical corridor.", 
         "PASS (100% Success)"),
        ("Test 2: Dual-Radio Peer Discovery After Leaving & Re-entering Range", 
         "Devices taken 60m apart until BLE link dropped ('Searching Chunks...'). Brought back within 15m. Self-healing watchdog re-established discovery in 1.4s without user intervention.", 
         "PASS (< 2.0s Latency)"),
        ("Test 3: Bluetooth Power Toggle Recovery Stress Test", 
         "Bluetooth switched OFF in Android quick settings on OnePlus. App presents auto-turn-on dialog. User switches BT on. `BluetoothStateReceiver` instantly resurrects GATT and Advertiser daemons.", 
         "PASS (Zero Deadlock)"),
        ("Test 4: High-Priority Emergency SOS Preemption", 
         "Continuous peer-to-peer chat running between nodes. User triggers 3-second SOS beacon. Chat relay instantly yields radio; SOS beacon floods nearby nodes with audible siren & GPS coordinates.", 
         "PASS (< 50ms Preemption)"),
        ("Test 5: Multi-Hop Store-and-Forward across Intermediate Nodes", 
         "Device A (Node 1) sends encrypted packet to Device C (Node 3) placed out of range. Device B (Node 2) receives, caches in Room SQLite, carries packet into proximity of Device C, and delivers successfully.", 
         "PASS (DTN Store & Forward)")
    ]

    for title, desc, status in test_runs:
        p = tf_test.add_paragraph()
        r_t = p.add_run()
        r_t.text = title + "  ➔  "
        r_t.font.bold = True
        r_t.font.size = Pt(10.5)
        r_t.font.color.rgb = TEXT_LIGHT

        r_s = p.add_run()
        r_s.text = status
        r_s.font.bold = True
        r_s.font.size = Pt(10.5)
        r_s.font.color.rgb = MINT_GREEN

        p_d = tf_test.add_paragraph()
        p_d.text = desc
        p_d.font.size = Pt(9)
        p_d.font.color.rgb = TEXT_MUTED
        p_d.space_after = Pt(4)

    # =========================================================================
    # SLIDE 10: PRODUCTION RELEASE & WEB DISTRIBUTION ARCHITECTURE
    # =========================================================================
    slide10 = add_base_slide("DEPLOYMENT & CONTINUOUS DISTRIBUTION", "Official Release v1.0.6, GitHub Pages & Live QR Download Portal")

    dep1 = add_card(slide10, Inches(0.8), Inches(1.7), Inches(5.6), Inches(5.1), "CONTINUOUS DISTRIBUTION INFRASTRUCTURE", GOLD_ACCENT)
    df1 = dep1.text_frame
    dep_pts = [
        ("Official GitHub Release v1.0.6", "Compiled and published directly to GitHub Releases with automated binary asset tagging (`CampusMesh-v1.0.6.apk` and `CampusMesh-v1.0.apk`)."),
        ("GitHub Pages Live Portal", "Live at: `https://047harry.github.io/anits-campus-mesh/`\nHosted from repository `/docs` branch with zero server maintenance costs."),
        ("Dynamic QR Code Generator", "The web portal dynamically generates a high-contrast QR code allowing students and faculty to point their smartphone cameras and download the APK directly."),
        ("Netlify Instant Mirror Deploy", "Secondary live web distribution mirror deployed on Netlify (`https://fluffy-valkyrie-5c0a68.netlify.app`), providing zero-downtime redundant distribution.")
    ]
    for title, desc in dep_pts:
        p = df1.add_paragraph()
        p.text = "• " + title
        p.font.bold = True
        p.font.size = Pt(10.5)
        p.font.color.rgb = TEXT_LIGHT
        p2 = df1.add_paragraph()
        p2.text = desc
        p2.font.size = Pt(9)
        p2.font.color.rgb = TEXT_MUTED
        p2.space_after = Pt(6)

    dep2 = add_card(slide10, Inches(6.8), Inches(1.7), Inches(5.7), Inches(5.1), "DEPLOYMENT ARTIFACTS & ACCESS LINKS", CYAN_ACCENT)
    df2 = dep2.text_frame
    links = [
        ("Web Distribution URL", "https://047harry.github.io/anits-campus-mesh/"),
        ("Netlify Web Mirror", "https://fluffy-valkyrie-5c0a68.netlify.app"),
        ("GitHub Release Page", "https://github.com/047harry/anits-campus-mesh/releases/tag/v1.0.6"),
        ("Direct APK Download", "https://github.com/047harry/anits-campus-mesh/releases/download/v1.0.6/CampusMesh-v1.0.6.apk"),
        ("Target Architecture", "Android 10.0+ (API Level 29 to 34)"),
        ("Hardware Requirements", "Bluetooth Low Energy 5.0 (BLE) + Wi-Fi Direct / Local Wi-Fi"),
        ("Binary Footprint", "61.1 MB (Self-contained offline Room database + Crypto engine)")
    ]
    for title, desc in links:
        p = df2.add_paragraph()
        p.text = title + ":"
        p.font.bold = True
        p.font.size = Pt(10)
        p.font.color.rgb = CYAN_ACCENT
        p2 = df2.add_paragraph()
        p2.text = desc
        p2.font.size = Pt(9)
        p2.font.color.rgb = TEXT_LIGHT if "http" not in desc else GOLD_ACCENT
        p2.space_after = Pt(4)

    # =========================================================================
    # SLIDE 11: COMPARISON & ENGINEERING ACCOMPLISHMENTS
    # =========================================================================
    slide11 = add_base_slide("ENGINEERING ACCOMPLISHMENTS & BENCHMARKS", "Comparison: Commercial Apps vs CampusMesh v1.0.6")

    comp_card = add_card(slide11, Inches(0.8), Inches(1.7), Inches(11.733), Inches(5.1), "TECHNICAL CAPABILITY MATRIX", PRIMARY_ORANGE)
    tf_c = comp_card.text_frame

    matrix = [
        ("Operational Requirement", "Commercial Apps (WhatsApp, Telegram)", "Amateur Radios / Walkie-Talkies", "ANITS CampusMesh (v1.0.6)"),
        ("Zero-Internet Survival", "❌ Fails completely; requires cell towers", "✔ Works offline (Voice only)", "✔ 100% Offline peer-to-peer data mesh"),
        ("Multi-Hop Digital Relaying", "❌ Impossible without cloud server", "❌ Requires fixed high-power repeaters", "✔ Autonomous multi-hop smartphone hopping (7 Hops)"),
        ("Role-Based Authority Trust", "❌ Flat; any user can spoof or forward", "❌ No digital identity or verification", "✔ Ed25519 signed Gold Shield directives"),
        ("Preemptive Life-Safety QoS", "❌ FIFO queuing; packets lost in storm", "❌ Voice channel congestion", "✔ Tier 1 SOS priority preemption (<50ms)"),
        ("Store-and-Forward (DTN)", "❌ Messages fail if receiver is offline", "❌ Transient audio lost if missed", "✔ Room SQLite store-and-forward caching"),
        ("Hardware / Cost Barrier", "Zero (Uses phone) but needs data pack", "High equipment cost ($100-$500/unit)", "Zero additional cost (Existing student smartphones)"),
        ("Real Hardware Verification", "N/A", "Hardware required", "✔ Validated across Vivo, OnePlus, and Samsung")
    ]

    for req, comm, rad, mesh in matrix:
        p = tf_c.add_paragraph()
        r1 = p.add_run()
        r1.text = f"{req[:28]:<28} | "
        r1.font.bold = True
        r1.font.size = Pt(9.5)
        r1.font.color.rgb = GOLD_ACCENT

        r2 = p.add_run()
        r2.text = f"Comm: {comm[:26]:<26} | "
        r2.font.size = Pt(9)
        r2.font.color.rgb = RED_ALERT if "❌" in comm else TEXT_MUTED

        r3 = p.add_run()
        r3.text = f"Mesh: {mesh}"
        r3.font.bold = True
        r3.font.size = Pt(9)
        r3.font.color.rgb = MINT_GREEN if "✔" in mesh else TEXT_LIGHT
        p.space_after = Pt(3)

    # =========================================================================
    # SLIDE 12: CONCLUSION & FUTURE ROADMAP
    # =========================================================================
    slide12 = add_base_slide("PROJECT SUMMARY & FUTURE OUTLOOK", "Conclusion, Societal Impact & System Roadmap")

    conc1 = add_card(slide12, Inches(0.8), Inches(1.7), Inches(5.6), Inches(5.1), "KEY PROJECT ACHIEVEMENTS", MINT_GREEN)
    cf1 = conc1.text_frame
    achievements = [
        ("Institutional Disaster Preparedness", "Delivered an autonomous, battle-tested communication grid specifically tailored to the topological realities of the ANITS campus perimeter."),
        ("True Zero-Infrastructure Operation", "Eliminated single points of failure. Smartphones in student pockets form an organic, self-healing ad-hoc mesh network."),
        ("Cryptographic Accountability", "Hybrid Ed25519 + AES-GCM architecture guarantees authentic executive bulletins while preserving absolute occupant privacy."),
        ("Complete End-to-End Implementation", "Not a mere theoretical simulator: fully coded native Kotlin Android application with background services, Room SQLite, and live distribution.")
    ]
    for title, desc in achievements:
        p = cf1.add_paragraph()
        p.text = "✔ " + title
        p.font.bold = True
        p.font.size = Pt(10.5)
        p.font.color.rgb = TEXT_LIGHT
        p2 = cf1.add_paragraph()
        p2.text = desc
        p2.font.size = Pt(9)
        p2.font.color.rgb = TEXT_MUTED
        p2.space_after = Pt(6)

    conc2 = add_card(slide12, Inches(6.8), Inches(1.7), Inches(5.7), Inches(5.1), "FUTURE ENHANCEMENTS (POST-CAPSTONE)", GOLD_ACCENT)
    cf2 = conc2.text_frame
    roadmap = [
        ("LoRa Gateway Bridge Integration", "Deploying low-cost ESP32 LoRa long-range nodes on water towers to bridge 5km inter-campus gaps across Tagarapuvalasa and Bheemili."),
        ("Wi-Fi Aware NAN Cluster Sockets", "Scaling from BLE 5.0 to high-throughput Wi-Fi Aware NAN data clusters for high-speed offline map tile sharing."),
        ("Offline AI Triage Bot", "On-device quantized LLM (e.g. Gemini Nano / MobileBERT) providing offline emergency first-aid advice and casualty triaging."),
        ("National Disaster Integration", "Extending protocol adapters to interface with NDMA (National Disaster Management Authority) CAP-compliant crisis feeds.")
    ]
    for title, desc in roadmap:
        p = cf2.add_paragraph()
        p.text = "➔ " + title
        p.font.bold = True
        p.font.size = Pt(10.5)
        p.font.color.rgb = GOLD_ACCENT
        p2 = cf2.add_paragraph()
        p2.text = desc
        p2.font.size = Pt(9)
        p2.font.color.rgb = TEXT_MUTED
        p2.space_after = Pt(6)

    # =========================================================================
    # SLIDE 13: THANK YOU & Q&A
    # =========================================================================
    slide13 = prs.slides.add_slide(prs.slide_layouts[6])
    bg13 = slide13.shapes.add_shape(MSO_SHAPE.RECTANGLE, 0, 0, prs.slide_width, prs.slide_height)
    bg13.fill.solid()
    bg13.fill.fore_color.rgb = BG_DARK
    bg13.line.fill.background()

    thank_box = slide13.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(1.5), Inches(1.5), Inches(10.333), Inches(4.5))
    thank_box.fill.solid()
    thank_box.fill.fore_color.rgb = SURFACE_DARK
    thank_box.line.color.rgb = BORDER_COLOR
    thank_tf = thank_box.text_frame
    thank_tf.margin_top = Inches(0.4)

    p_th1 = thank_tf.paragraphs[0]
    p_th1.alignment = PP_ALIGN.CENTER
    p_th1.text = "THANK YOU!"
    p_th1.font.bold = True
    p_th1.font.size = Pt(40)
    p_th1.font.color.rgb = PRIMARY_ORANGE

    p_th2 = thank_tf.add_paragraph()
    p_th2.alignment = PP_ALIGN.CENTER
    p_th2.text = "ANITS Decentralized Offline Campus Mesh Network"
    p_th2.font.bold = True
    p_th2.font.size = Pt(18)
    p_th2.font.color.rgb = TEXT_LIGHT

    p_th3 = thank_tf.add_paragraph()
    p_th3.alignment = PP_ALIGN.CENTER
    p_th3.text = "Empowering 5,000+ campus occupants with zero-infrastructure life-safety communications."
    p_th3.font.size = Pt(12)
    p_th3.font.color.rgb = CYAN_ACCENT
    p_th3.space_after = Pt(16)

    p_th4 = thank_tf.add_paragraph()
    p_th4.alignment = PP_ALIGN.CENTER
    p_th4.text = "Team 12 (CSD-B)  •  Project Lead: P. Darakesu Hemanth (A23126551113)"
    p_th4.font.bold = True
    p_th4.font.size = Pt(12)
    p_th4.font.color.rgb = GOLD_ACCENT

    p_th5 = thank_tf.add_paragraph()
    p_th5.alignment = PP_ALIGN.CENTER
    p_th5.text = "Live Web Portal: https://047harry.github.io/anits-campus-mesh/  |  Open for Questions & Live Demonstration"
    p_th5.font.size = Pt(11)
    p_th5.font.color.rgb = MINT_GREEN

    out_path = r'H:\Final dance\ppts\final-2.pptx'
    prs.save(out_path)
    print(f"Successfully generated: {out_path} with {len(prs.slides)} slides")

if __name__ == '__main__':
    build_presentation()
