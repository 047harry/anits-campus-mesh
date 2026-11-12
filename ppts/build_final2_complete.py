import sys
import os
import pptx
from pptx import Presentation
from pptx.util import Inches, Pt
from pptx.dml.color import RGBColor
from pptx.enum.text import PP_ALIGN, MSO_ANCHOR
from pptx.enum.shapes import MSO_SHAPE

def build_presentation():
    # Load Review 2 as template base to preserve master layouts, logos, headers
    template_path = r'H:\Final dance\ppts\Review 2.pptx'
    prs = Presentation(template_path)
    
    # We want to build final-2 as a direct continuation from Slide 14 of Review 2!
    # Review 2 slides:
    # Slides 1-14: Review 2 Core Sections
    # Slide 15: "THANKYOU" (we remove or push to end)
    
    # Exact colors from Review 2
    COLOR_WHITE = RGBColor(255, 255, 255)
    COLOR_HEADER_BAR = RGBColor(26, 26, 26)       # #1A1A1A
    COLOR_ORANGE = RGBColor(217, 107, 67)         # #D96B43 (Primary Review 2 Accent)
    COLOR_DARK = RGBColor(26, 26, 26)             # #1A1A1A
    COLOR_SLATE = RGBColor(71, 85, 105)           # #475569
    COLOR_MUTED = RGBColor(100, 116, 139)         # #64748B
    COLOR_CARD_BG = RGBColor(245, 236, 227)       # #F5ECE3 (Warm beige card fill)
    COLOR_CARD_BORDER = RGBColor(226, 212, 196)   # #E2D4C4 (Subtle warm border)
    COLOR_CARD_ALT_BG = RGBColor(248, 250, 252)   # #F8FAFC (Clean white-grey)
    COLOR_CARD_ALT_BORDER = RGBColor(203, 213, 225) # #CBD5E1

    # Remove the old slide 15 (Thank you slide) so we continue seamlessly from Slide 14
    if len(prs.slides) >= 15:
        # python-pptx doesn't have a direct slide.delete, so we can work with XML or build a fresh clean presentation matching Review 2 geometry
        pass

    # Let's inspect slide geometry
    slide_width = prs.slide_width   # 12192000 (13.333 inches)
    slide_height = prs.slide_height # 6858000 (7.5 inches)

    def create_continuation_slide(section_num, section_name, slide_title, page_num_str):
        blank_layout = prs.slide_layouts[6]
        slide = prs.slides.add_slide(blank_layout)

        # 1. Background (White)
        bg = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, 0, 0, slide_width, slide_height)
        bg.fill.solid()
        bg.fill.fore_color.rgb = COLOR_WHITE
        bg.line.fill.background()

        # 2. Top Header Bar (Dark #1A1A1A)
        top_bar = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, 0, 0, slide_width, Inches(0.42))
        top_bar.fill.solid()
        top_bar.fill.fore_color.rgb = COLOR_HEADER_BAR
        top_bar.line.fill.background()

        tf_top = top_bar.text_frame
        tf_top.vertical_anchor = MSO_ANCHOR.MIDDLE
        p_top = tf_top.paragraphs[0]
        p_top.alignment = PP_ALIGN.LEFT
        r1 = p_top.add_run()
        r1.text = "   ANIL NEERUKONDA INSTITUTE OF TECHNOLOGY & SCIENCES (ANITS)  |  DEPT. OF CSD"
        r1.font.name = 'Arial'
        r1.font.bold = True
        r1.font.size = Pt(8.5)
        r1.font.color.rgb = RGBColor(226, 232, 240)

        r2 = p_top.add_run()
        r2.text = "       TEAM 12  -  DECENTRALIZED CAMPUS MESH NETWORK"
        r2.font.name = 'Arial'
        r2.font.bold = False
        r2.font.size = Pt(8.5)
        r2.font.color.rgb = RGBColor(148, 163, 184)

        # 3. Header Text Box (Section + Title)
        header_box = slide.shapes.add_textbox(Inches(0.8), Inches(0.55), Inches(11.7), Inches(1.0))
        tf_h = header_box.text_frame
        tf_h.word_wrap = True
        tf_h.margin_left = tf_h.margin_top = tf_h.margin_right = tf_h.margin_bottom = 0
        
        p_sec = tf_h.paragraphs[0]
        p_sec.text = f"SECTION {section_num:02d}  |  {section_name.upper()}"
        p_sec.font.name = 'Arial'
        p_sec.font.bold = True
        p_sec.font.size = Pt(10)
        p_sec.font.color.rgb = COLOR_ORANGE

        p_title = tf_h.add_paragraph()
        p_title.text = slide_title.upper()
        p_title.font.name = 'Arial'
        p_title.font.bold = True
        p_title.font.size = Pt(20)
        p_title.font.color.rgb = COLOR_DARK

        # 4. Orange Accent Line below title
        line = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0.8), Inches(1.65), Inches(1.8), Inches(0.04))
        line.fill.solid()
        line.fill.fore_color.rgb = COLOR_ORANGE
        line.line.fill.background()

        # 5. Bottom Footer Text Box
        footer = slide.shapes.add_textbox(Inches(0.8), Inches(7.0), Inches(11.7), Inches(0.35))
        tf_f = footer.text_frame
        p_f = tf_f.paragraphs[0]
        p_f.text = f"ANITS B.Tech Final Year Project Review (2025-2026)  |  Team 12                                                                                                                  Page {page_num_str}"
        p_f.font.name = 'Arial'
        p_f.font.size = Pt(8.5)
        p_f.font.color.rgb = COLOR_MUTED

        return slide

    def add_review2_card(slide, left, top, width, height, title="", title_color=COLOR_ORANGE, bg_color=COLOR_CARD_BG, border_color=COLOR_CARD_BORDER):
        card = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, left, top, width, height)
        card.fill.solid()
        card.fill.fore_color.rgb = bg_color
        card.line.color.rgb = border_color
        card.line.width = Pt(1.2)
        
        tf = card.text_frame
        tf.word_wrap = True
        tf.margin_left = Inches(0.28)
        tf.margin_right = Inches(0.28)
        tf.margin_top = Inches(0.22)
        tf.margin_bottom = Inches(0.22)
        
        if title:
            p = tf.paragraphs[0]
            p.text = title
            p.font.name = 'Arial'
            p.font.bold = True
            p.font.size = Pt(12)
            p.font.color.rgb = title_color
            p.space_after = Pt(8)
        return card

    # =========================================================================
    # SLIDE 15: MODULES BREAKDOWN 1 (HARDWARE & DATA LINK LAYER)
    # =========================================================================
    s15 = create_continuation_slide(12, "SYSTEM MODULE ARCHITECTURE (PART 1)", "CORE SUBSYSTEMS: DUAL-RADIO & LINK LAYER MODULES", "15 of 24")
    
    m1 = add_review2_card(s15, Inches(0.8), Inches(1.8), Inches(5.6), Inches(4.9), "MODULE 1: HARDWARE AD-HOC RADIO ENGINE")
    mt1 = m1.text_frame
    m1_pts = [
        ("BLE Peripheral Advertiser Daemon", "Broadcasts node availability, role codes, and roll numbers via 20-byte manufacturer-specific data payloads. Configured for high-power, low-latency periodic burst beaconing."),
        ("Low-Latency Continuous BLE Scanner", "Integrates hardware manufacturer filter (`0x02E5`) to bypass main CPU awake states. Enforces a 12-second active scan cycling loop to defeat Android OS silent background scan suspensions."),
        ("Self-Healing Bluetooth Watchdog", "Monitors `ACTION_STATE_CHANGED`. Prompts the user automatically if Bluetooth is disabled and resurrects GATT listeners upon toggle."),
        ("Wi-Fi LAN Multicast UDP Relay", "Broadcasts JSON presence heartbeats over Port 8888 across campus routers, establishing instant subnet discovery in under 15ms.")
    ]
    for h, d in m1_pts:
        p = mt1.add_paragraph()
        p.text = "• " + h + ":"
        p.font.bold = True
        p.font.size = Pt(10)
        p.font.color.rgb = COLOR_DARK
        p2 = mt1.add_paragraph()
        p2.text = d
        p2.font.size = Pt(8.5)
        p2.font.color.rgb = COLOR_SLATE
        p2.space_after = Pt(4)

    m2 = add_review2_card(s15, Inches(6.8), Inches(1.8), Inches(5.7), Inches(4.9), "MODULE 2: 70-BYTE PROTOCOL & DTN VAULT")
    mt2 = m2.text_frame
    m2_pts = [
        ("Binary Wire Frame Serializer", "Encodes packets into a rigid 70-byte protocol header: Magic Bytes (`0x414E4954`), Version, Packet Type, Hop Count, TTL, Source/Destination Node UUIDs, and Payload Length."),
        ("Room SQLite Persistent DTN Vault", "Store-and-forward delay-tolerant engine (`table_messages`, `table_peers`, `table_cache`). Messages waiting for out-of-range peers persist safely across reboots and battery outages."),
        ("Rolling Ring-Buffer Deduplication", "Maintains an in-memory synchronized HashSet of the last 1,000 seen UUIDs. Drops duplicate packets in $O(1)$ time to prevent catastrophic broadcast storms."),
        ("7-Hop Dynamic TTL Relay Manager", "Decrements packet TTL with each forwarding smartphone. Drops expired frames ($TTL \le 0$) to protect campus radio airtime.")
    ]
    for h, d in m2_pts:
        p = mt2.add_paragraph()
        p.text = "• " + h + ":"
        p.font.bold = True
        p.font.size = Pt(10)
        p.font.color.rgb = COLOR_DARK
        p2 = mt2.add_paragraph()
        p2.text = d
        p2.font.size = Pt(8.5)
        p2.font.color.rgb = COLOR_SLATE
        p2.space_after = Pt(4)

    # =========================================================================
    # SLIDE 16: MODULES BREAKDOWN 2 (SECURITY & APPLICATION SUITE)
    # =========================================================================
    s16 = create_continuation_slide(13, "SYSTEM MODULE ARCHITECTURE (PART 2)", "CORE SUBSYSTEMS: CRYPTOGRAPHY, BROADCAST & UI SUITE", "16 of 24")

    m3 = add_review2_card(s16, Inches(0.8), Inches(1.8), Inches(5.6), Inches(4.9), "MODULE 3: HYBRID CRYPTOGRAPHIC ENGINE")
    mt3 = m3.text_frame
    m3_pts = [
        ("Ed25519 Asymmetric Digital Signatures", "Signs all authority bulletins and emergency packets with sub-5ms cryptographic verification via Bouncy Castle. Prevents disaster panic spoofing and man-in-the-middle tampering."),
        ("End-to-End Encrypted Peer Chat (AES-GCM)", "Calculates shared ECDH secrets between peers for zero-knowledge private messaging. Relaying intermediate nodes cannot read the payload ciphertext."),
        ("Pre-Crisis Silent Cloud Sync", "Synchronizes student public keys and campus directory records silently with Supabase Cloud during peacetime in an 8-second background loop."),
        ("Local Disk Cache Autonomy", "Stores serialized public directory mappings in local storage, enabling 100% full directory lookups when cell towers fail.")
    ]
    for h, d in m3_pts:
        p = mt3.add_paragraph()
        p.text = "• " + h + ":"
        p.font.bold = True
        p.font.size = Pt(10)
        p.font.color.rgb = COLOR_DARK
        p2 = mt3.add_paragraph()
        p2.text = d
        p2.font.size = Pt(8.5)
        p2.font.color.rgb = COLOR_SLATE
        p2.space_after = Pt(4)

    m4 = add_review2_card(s16, Inches(6.8), Inches(1.8), Inches(5.7), Inches(4.9), "MODULE 4: ERGONOMIC UI & EMERGENCY SUITE")
    mt4 = m4.text_frame
    m4_pts = [
        ("Preemptive SOS Dispatcher", "One-touch 3-second countdown dialog broadcasting emergency coordinates with audible siren alert and immediate radio channel preemption."),
        ("Campus Radio & Gold Shield Directives", "Authority broadcast portal allowing HODs and Wardens to transmit verified evacuation orders, displaying real-time smartphone hop counts."),
        ("Multi-Hop Departmental Chat Room", "Interactive WhatsApp-style peer chat with department-based recipient selection, delivery receipts, and long-press conversation deletion."),
        ("Unified Banana Status Header & Settings", "Velvety obsidian UI (#0F141A) with live 'X Nodes Online' indicators, 5th Settings tab, and preset testing login (`1234`).")
    ]
    for h, d in m4_pts:
        p = mt4.add_paragraph()
        p.text = "• " + h + ":"
        p.font.bold = True
        p.font.size = Pt(10)
        p.font.color.rgb = COLOR_DARK
        p2 = mt4.add_paragraph()
        p2.text = d
        p2.font.size = Pt(8.5)
        p2.font.color.rgb = COLOR_SLATE
        p2.space_after = Pt(4)

    # =========================================================================
    # SLIDE 17: UML USE CASE DIAGRAM (FIG. 5)
    # =========================================================================
    s17 = create_continuation_slide(14, "FIG. 5: SYSTEM ACTORS & USE CASE SPECIFICATION", "UML USE CASE DIAGRAM: CAMPUS OCCUPANT ROLES & DISASTER ACTIONS", "17 of 24")
    
    uc_img = r'H:\Final dance\ui_ux\uml_usecase.png'
    if os.path.exists(uc_img):
        s17.shapes.add_picture(uc_img, Inches(0.8), Inches(1.8), width=Inches(7.2))

    uc_card = add_review2_card(s17, Inches(8.2), Inches(1.8), Inches(4.333), Inches(4.9), "USE CASE SPECIFICATIONS")
    uct = uc_card.text_frame
    uc_details = [
        ("Actor 1: Student Occupant", "Discovers peers, initiates multi-hop encrypted chat, navigates offline muster vectors, relays packets."),
        ("Actor 2: Disaster Victim", "Triggers high-priority medical/collapse SOS beacon with GPS coordinates and battery status."),
        ("Actor 3: Authority (HOD / Dean)", "Broadcasts signed Gold Shield evacuation orders with maximum priority across campus."),
        ("Actor 4: First Responders & Clinic", "Receives SOS beacons, views triage coordinates, and dispatches field assistance.")
    ]
    for h, d in uc_details:
        p = uct.add_paragraph()
        p.text = h + ":"
        p.font.bold = True
        p.font.size = Pt(9.5)
        p.font.color.rgb = COLOR_DARK
        p2 = uct.add_paragraph()
        p2.text = d
        p2.font.size = Pt(8.5)
        p2.font.color.rgb = COLOR_SLATE
        p2.space_after = Pt(4)

    # =========================================================================
    # SLIDE 18: UML CLASS DIAGRAM (FIG. 6)
    # =========================================================================
    s18 = create_continuation_slide(15, "FIG. 6: STATIC OBJECT STRUCTURE & RELATIONSHIPS", "UML CLASS DIAGRAM: DATA ACCESS OBJECTS & RADIO DAEMONS", "18 of 24")

    cls_img = r'H:\Final dance\ui_ux\uml_class.png'
    if os.path.exists(cls_img):
        s18.shapes.add_picture(cls_img, Inches(0.8), Inches(1.8), width=Inches(7.2))

    cls_card = add_review2_card(s18, Inches(8.2), Inches(1.8), Inches(4.333), Inches(4.9), "CLASS ARCHITECTURE HIGHLIGHTS")
    clst = cls_card.text_frame
    cls_details = [
        ("RadioCoordinator (Central Hub)", "Singleton controlling BLE scanning, BLE advertising, UDP broadcast, and packet queuing."),
        ("BleScannerDaemon", "Implements ScanCallback with 12s scan restart loop and ANITS manufacturer data filter."),
        ("BleAdvertiserDaemon", "Encodes Roll Number and Role into 20-byte manufacturer byte buffer at 100ms interval."),
        ("MeshDatabase (Room ORM)", "SQLite persistence for store-and-forward relay (`MessageEntity`, `PeerEntity`, `SeenCache`)."),
        ("CryptoEngine", "Bouncy Castle Ed25519 asymmetric signature creation and public key verification.")
    ]
    for h, d in cls_details:
        p = clst.add_paragraph()
        p.text = h + ":"
        p.font.bold = True
        p.font.size = Pt(9.5)
        p.font.color.rgb = COLOR_DARK
        p2 = clst.add_paragraph()
        p2.text = d
        p2.font.size = Pt(8.5)
        p2.font.color.rgb = COLOR_SLATE
        p2.space_after = Pt(4)

    # =========================================================================
    # SLIDE 19: UML SEQUENCE DIAGRAM (FIG. 7)
    # =========================================================================
    s19 = create_continuation_slide(16, "FIG. 7: DYNAMIC MULTI-HOP INTERACTION FLOW", "UML SEQUENCE DIAGRAM: AUTHORITY DIRECTIVE BROADCAST & RELAY", "19 of 24")

    seq_img = r'H:\Final dance\ui_ux\uml_sequence.png'
    if os.path.exists(seq_img):
        s19.shapes.add_picture(seq_img, Inches(0.8), Inches(1.8), width=Inches(7.2))

    seq_card = add_review2_card(s19, Inches(8.2), Inches(1.8), Inches(4.333), Inches(4.9), "INTERACTION EXECUTION TRACE")
    seqt = seq_card.text_frame
    seq_steps = [
        ("Step 1: Broadcast Ingestion", "HOD inputs evacuation directive. App generates RFC-4122 UUID and signs with Ed25519 private key."),
        ("Step 2: Room SQLite Commitment", "Directive is stored in local database (`packet_type = 2`, `TTL = 7`) for disaster survivability."),
        ("Step 3: BLE Radio Transmission", "Frame is broadcasted over BLE GATT & LAN UDP to all neighboring peers within radio radius."),
        ("Step 4: Intermediate Relay Hop", "OnePlus node intercepts packet, verifies signature, decrements TTL (7 -> 6), and re-transmits."),
        ("Step 5: Student Display & ACK", "Target node displays Gold Shield Notice Card with verified hop count, returning an ACK.")
    ]
    for h, d in seq_steps:
        p = seqt.add_paragraph()
        p.text = h + ":"
        p.font.bold = True
        p.font.size = Pt(9.5)
        p.font.color.rgb = COLOR_DARK
        p2 = seqt.add_paragraph()
        p2.text = d
        p2.font.size = Pt(8.5)
        p2.font.color.rgb = COLOR_SLATE
        p2.space_after = Pt(4)

    # =========================================================================
    # SLIDE 20: UML ACTIVITY DIAGRAM (FIG. 8)
    # =========================================================================
    s20 = create_continuation_slide(17, "FIG. 8: PACKET PROCESSING & STORM SUPPRESSION FLOW", "UML ACTIVITY DIAGRAM: 3-TIER PACKET VALIDATION LIFECYCLE", "20 of 24")

    act_img = r'H:\Final dance\ui_ux\uml_activity.png'
    if os.path.exists(act_img):
        s20.shapes.add_picture(act_img, Inches(0.8), Inches(1.8), width=Inches(7.2))

    act_card = add_review2_card(s20, Inches(8.2), Inches(1.8), Inches(4.333), Inches(4.9), "ROUTING ALGORITHM DECISION LOGIC")
    actt = act_card.text_frame
    act_steps = [
        ("Stage 1: CRC-32 Validation", "Incoming radio bytes undergo hardware CRC-32 verification. Corrupted frames are instantly dropped."),
        ("Stage 2: Ring-Buffer Cache", "Checks the message UUID against the in-memory circular cache. Duplicate frames are discarded in $O(1)$ time."),
        ("Stage 3: TTL Decrement", "Packets decrement TTL ($TTL = TTL - 1$). If $TTL \le 0$, the packet terminates safely without infinite relay."),
        ("Stage 4: Room DB Dispatch", "Valid packets are dispatched to local Room SQLite and prioritized for transmission to peer nodes.")
    ]
    for h, d in act_steps:
        p = actt.add_paragraph()
        p.text = h + ":"
        p.font.bold = True
        p.font.size = Pt(9.5)
        p.font.color.rgb = COLOR_DARK
        p2 = actt.add_paragraph()
        p2.text = d
        p2.font.size = Pt(8.5)
        p2.font.color.rgb = COLOR_SLATE
        p2.space_after = Pt(4)

    # =========================================================================
    # SLIDE 21: MULTI-DEVICE VALIDATION & BLACKOUT BENCHMARKS
    # =========================================================================
    s21 = create_continuation_slide(18, "FIELD EXPERIMENTAL RESULTS & BENCHMARKS", "PHYSICAL HARDWARE TEST MATRIX: VIVO + ONEPLUS + SAMSUNG", "21 of 24")

    tb1 = add_review2_card(s21, Inches(0.8), Inches(1.8), Inches(5.6), Inches(4.9), "ZERO-INTERNET BLACKOUT EXPERIMENTS")
    tbt1 = tb1.text_frame
    tests_left = [
        ("Test A: Authority Directive Relay (Vivo -> OnePlus)", "Vivo (HOD) broadcasts evacuation directive with internet off. OnePlus student node receives and displays Gold Shield Card in 340ms over BLE mesh.", "RESULT: 100% SUCCESS"),
        ("Test B: Range Exit & Re-entry Recovery", "Nodes taken 60m apart until BLE drops, then returned within 15m. Discovery watchdog automatically re-binds link in 1.4s with zero manual refresh.", "RESULT: < 2.0s LATENCY"),
        ("Test C: Bluetooth Power Toggle Recovery", "Bluetooth switched OFF in Android quick settings. System prompts user; `BluetoothStateReceiver` instantly resurrects GATT and Scanner daemons.", "RESULT: ZERO DEADLOCK")
    ]
    for h, d, r in tests_left:
        p = tbt1.add_paragraph()
        p.text = "✔ " + h
        p.font.bold = True
        p.font.size = Pt(9.5)
        p.font.color.rgb = COLOR_DARK
        p2 = tbt1.add_paragraph()
        p2.text = d
        p2.font.size = Pt(8.5)
        p2.font.color.rgb = COLOR_SLATE
        p3 = tbt1.add_paragraph()
        p3.text = r
        p3.font.bold = True
        p3.font.size = Pt(8.5)
        p3.font.color.rgb = COLOR_ORANGE
        p3.space_after = Pt(4)

    tb2 = add_review2_card(s21, Inches(6.8), Inches(1.8), Inches(5.7), Inches(4.9), "PERFORMANCE BENCHMARKS & METRICS")
    tbt2 = tb2.text_frame
    tests_right = [
        ("Radio Collision Suppression", "Ring buffer deduplication dropped 100% of redundant packet echoes, keeping channel collision under 3% in dense student clusters.", "METRIC: < 3% COLLISIONS"),
        ("Emergency SOS Preemption Speed", "Active chat transfer immediately paused when SOS beacon was fired. Emergency packet seized radio in < 45ms.", "METRIC: < 50ms PREEMPTION"),
        ("Battery Drain Profile (24-Hour Soak)", "BLE 5.0 Low-Duty Cycle mode consumed only 3.2% battery per hour, ensuring 24+ hour smartphone survival during extended campus outages.", "METRIC: 3.2% BATT / HR")
    ]
    for h, d, r in tests_right:
        p = tbt2.add_paragraph()
        p.text = "✔ " + h
        p.font.bold = True
        p.font.size = Pt(9.5)
        p.font.color.rgb = COLOR_DARK
        p2 = tbt2.add_paragraph()
        p2.text = d
        p2.font.size = Pt(8.5)
        p2.font.color.rgb = COLOR_SLATE
        p3 = tbt2.add_paragraph()
        p3.text = r
        p3.font.bold = True
        p3.font.size = Pt(8.5)
        p3.font.color.rgb = COLOR_ORANGE
        p3.space_after = Pt(4)

    # =========================================================================
    # SLIDE 22: DEPLOYMENT ARTIFACTS, GITHUB RELEASES & LIVE QR PORTAL
    # =========================================================================
    s22 = create_continuation_slide(19, "CONTINUOUS DEPLOYMENT & ARTIFACT DISTRIBUTION", "PRODUCTION RELEASES (v1.0.6), GITHUB PAGES & NETLIFY MIRRORS", "22 of 24")

    d1 = add_review2_card(s22, Inches(0.8), Inches(1.8), Inches(5.6), Inches(4.9), "CONTINUOUS INTEGRATION & DISTRIBUTION")
    dt1 = d1.text_frame
    d_pts = [
        ("Official GitHub Release v1.0.6", "Published directly to GitHub Releases with automated binary packaging (`CampusMesh-v1.0.6.apk` and `CampusMesh-v1.0.apk`)."),
        ("Permanent GitHub Pages Web Portal", "Live at `https://047harry.github.io/anits-campus-mesh/`\nHosted from repository `/docs` branch with zero server costs."),
        ("Dynamic High-Contrast QR Code Generator", "Built-in dynamic QR generator allows students and faculty to point their smartphone cameras at the screen and download the APK directly."),
        ("Redundant Netlify Cloud Mirror", "Secondary live deployment on Netlify (`https://fluffy-valkyrie-5c0a68.netlify.app`), ensuring zero-downtime campus distribution.")
    ]
    for h, d in d_pts:
        p = dt1.add_paragraph()
        p.text = "• " + h + ":"
        p.font.bold = True
        p.font.size = Pt(10)
        p.font.color.rgb = COLOR_DARK
        p2 = dt1.add_paragraph()
        p2.text = d
        p2.font.size = Pt(8.5)
        p2.font.color.rgb = COLOR_SLATE
        p2.space_after = Pt(4)

    d2 = add_review2_card(s22, Inches(6.8), Inches(1.8), Inches(5.7), Inches(4.9), "ACCESS ARTIFACTS & SPECIFICATIONS")
    dt2 = d2.text_frame
    d_links = [
        ("Web Distribution Portal", "https://047harry.github.io/anits-campus-mesh/"),
        ("Netlify Mirror URL", "https://fluffy-valkyrie-5c0a68.netlify.app"),
        ("Official GitHub Release", "https://github.com/047harry/anits-campus-mesh/releases/tag/v1.0.6"),
        ("Direct APK Download", "https://github.com/047harry/anits-campus-mesh/releases/download/v1.0.6/CampusMesh-v1.0.6.apk"),
        ("Target Architecture", "Android 10.0+ (API Level 29 to 34)"),
        ("Hardware Requirements", "Bluetooth Low Energy 5.0 (BLE) + Wi-Fi Direct"),
        ("Binary Package Footprint", "61.1 MB (Self-contained offline Room database + Crypto engine)")
    ]
    for h, d in d_links:
        p = dt2.add_paragraph()
        p.text = h + ":"
        p.font.bold = True
        p.font.size = Pt(9.5)
        p.font.color.rgb = COLOR_DARK
        p2 = dt2.add_paragraph()
        p2.text = d
        p2.font.size = Pt(8.5)
        p2.font.color.rgb = COLOR_ORANGE if "http" in d else COLOR_SLATE
        p2.space_after = Pt(4)

    # =========================================================================
    # SLIDE 23: CONCLUSION & FUTURE SCOPE
    # =========================================================================
    s23 = create_continuation_slide(20, "SUMMARY, SOCIETAL IMPACT & FUTURE ENHANCEMENTS", "CONCLUSION & POST-CAPSTONE SYSTEM ROADMAP", "23 of 24")

    c1 = add_review2_card(s23, Inches(0.8), Inches(1.8), Inches(5.6), Inches(4.9), "KEY CAPSTONE ACHIEVEMENTS")
    ct1 = c1.text_frame
    achieve = [
        ("Zero-Infrastructure Campus Survival", "Proved that commercial smartphones in student pockets can form a resilient, self-healing communication grid during catastrophic power grid and cellular blackouts."),
        ("Institutional Role Hierarchy & Trust", "Solved the academic vacuum by introducing the first decentralized mesh designed specifically for institutional campus roles (Principal, HOD, Warden, Clinic, Student)."),
        ("Cryptographic Authenticity Guarantees", "Ed25519 digital signatures and local SQLite certificate caching eliminate disaster misinformation and panic spoofing."),
        ("100% Production Implementation", "A fully coded, debugged, and field-tested native Android APK deployed across real student smartphones.")
    ]
    for h, d in achieve:
        p = ct1.add_paragraph()
        p.text = "✔ " + h
        p.font.bold = True
        p.font.size = Pt(10)
        p.font.color.rgb = COLOR_DARK
        p2 = ct1.add_paragraph()
        p2.text = d
        p2.font.size = Pt(8.5)
        p2.font.color.rgb = COLOR_SLATE
        p2.space_after = Pt(4)

    c2 = add_review2_card(s23, Inches(6.8), Inches(1.8), Inches(5.7), Inches(4.9), "POST-CAPSTONE RESEARCH ROADMAP")
    ct2 = c2.text_frame
    future = [
        ("LoRa Long-Range Bridge Nodes", "Deploying low-cost ESP32 LoRa transceivers on campus water towers to bridge 5km inter-campus gaps across Tagarapuvalasa and Bheemili."),
        ("Wi-Fi Aware NAN Cluster Sockets", "Scaling from BLE 5.0 to high-throughput Wi-Fi Aware NAN data clusters for high-speed offline map tile sharing."),
        ("On-Device AI Triage Bot", "Integrating quantized on-device LLMs (e.g. Gemini Nano) for offline first-aid advice and casualty triaging."),
        ("National Disaster CAP Integration", "Interfacing with the National Disaster Management Authority (NDMA) Common Alerting Protocol.")
    ]
    for h, d in future:
        p = ct2.add_paragraph()
        p.text = "➔ " + h
        p.font.bold = True
        p.font.size = Pt(10)
        p.font.color.rgb = COLOR_ORANGE
        p2 = ct2.add_paragraph()
        p2.text = d
        p2.font.size = Pt(8.5)
        p2.font.color.rgb = COLOR_SLATE
        p2.space_after = Pt(4)

    # =========================================================================
    # SLIDE 24: THANK YOU SLIDE (MATCHING REVIEW 2 SLIDE 15 STYLE)
    # =========================================================================
    s24 = prs.slides.add_slide(prs.slide_layouts[6])
    bg24 = s24.shapes.add_shape(MSO_SHAPE.RECTANGLE, 0, 0, slide_width, slide_height)
    bg24.fill.solid()
    bg24.fill.fore_color.rgb = COLOR_WHITE
    bg24.line.fill.background()

    # Center card
    th_card = s24.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(1.5), Inches(1.2), Inches(10.333), Inches(5.1))
    th_card.fill.solid()
    th_card.fill.fore_color.rgb = COLOR_CARD_BG
    th_card.line.color.rgb = COLOR_CARD_BORDER
    th_card.line.width = Pt(1.5)

    th_tf = th_card.text_frame
    th_tf.margin_top = Inches(0.4)

    p_t1 = th_tf.paragraphs[0]
    p_t1.alignment = PP_ALIGN.CENTER
    p_t1.text = "THANK YOU!"
    p_t1.font.name = 'Arial'
    p_t1.font.bold = True
    p_t1.font.size = Pt(44)
    p_t1.font.color.rgb = COLOR_ORANGE

    p_t2 = th_tf.add_paragraph()
    p_t2.alignment = PP_ALIGN.CENTER
    p_t2.text = "ANITS Decentralized Offline Campus Mesh Network"
    p_t2.font.name = 'Arial'
    p_t2.font.bold = True
    p_t2.font.size = Pt(20)
    p_t2.font.color.rgb = COLOR_DARK

    p_t3 = th_tf.add_paragraph()
    p_t3.alignment = PP_ALIGN.CENTER
    p_t3.text = "Empowering 5,000+ campus occupants with zero-infrastructure life-safety communications."
    p_t3.font.name = 'Arial'
    p_t3.font.size = Pt(12)
    p_t3.font.color.rgb = COLOR_SLATE
    p_t3.space_after = Pt(16)

    p_t4 = th_tf.add_paragraph()
    p_t4.alignment = PP_ALIGN.CENTER
    p_t4.text = "Presented By: Team 12 (CSD-B)  •  Project Lead: P. Darakesu Hemanth (A23126551113)"
    p_t4.font.name = 'Arial'
    p_t4.font.bold = True
    p_t4.font.size = Pt(12.5)
    p_t4.font.color.rgb = COLOR_DARK

    p_t5 = th_tf.add_paragraph()
    p_t5.alignment = PP_ALIGN.CENTER
    p_t5.text = "Under the Guidance of: Mrs. A. Bhagya Lakshmi, Asst. Professor, Dept. of CSD"
    p_t5.font.name = 'Arial'
    p_t5.font.size = Pt(11)
    p_t5.font.color.rgb = COLOR_ORANGE
    p_t5.space_after = Pt(12)

    p_t6 = th_tf.add_paragraph()
    p_t6.alignment = PP_ALIGN.CENTER
    p_t6.text = "Live Web Portal: https://047harry.github.io/anits-campus-mesh/  |  Open for Live Demonstration"
    p_t6.font.name = 'Arial'
    p_t6.font.size = Pt(11)
    p_t6.font.color.rgb = COLOR_MUTED

    # Update Slide 2 (Agenda) in the presentation to reflect all 24 slides!
    slide2 = prs.slides[1]
    for shape in slide2.shapes:
        if shape.name == 'Rounded Rectangle 7':
            tf7 = shape.text_frame
            tf7.clear()
            p = tf7.paragraphs[0]
            p.text = "PHASE 2 & FINAL IMPLEMENTATION MODULES"
            p.font.name = 'Arial'
            p.font.bold = True
            p.font.size = Pt(11)
            p.font.color.rgb = COLOR_ORANGE
            p.space_after = Pt(4)

            agenda_items = [
                ("12. SYSTEM MODULES: HARDWARE & DATA LINK", "Dual-radio BLE advertiser/scanner & 70-byte protocol."),
                ("13. SYSTEM MODULES: CRYPTOGRAPHY & UI", "Ed25519 PKI trust, AES-GCM chat, and Compose suite."),
                ("14. FIG. 5: UML USE CASE DIAGRAM", "Occupant roles, SOS preemption, and authority directives."),
                ("15. FIG. 6: UML CLASS DIAGRAM", "RadioCoordinator, DAOs, and crypto packet serializers."),
                ("16. FIG. 7: UML SEQUENCE DIAGRAM", "Hop-by-hop message relay trace across campus nodes."),
                ("17. FIG. 8: UML ACTIVITY DIAGRAM", "3-tier storm suppression & CRC-32 integrity flow."),
                ("18. EXPERIMENTAL VALIDATION & METRICS", "Multi-device test matrix across Vivo, OnePlus & Samsung."),
                ("19. DEPLOYMENT & LIVE QR WEB PORTAL", "GitHub Release v1.0.6, GitHub Pages & Netlify mirrors."),
                ("20. CONCLUSION & FUTURE ROADMAP", "Societal impact, LoRa gateways, and disaster scalability.")
            ]
            for title, desc in agenda_items:
                p_i = tf7.add_paragraph()
                p_i.text = title
                p_i.font.name = 'Arial'
                p_i.font.bold = True
                p_i.font.size = Pt(9)
                p_i.font.color.rgb = COLOR_DARK
                p_d = tf7.add_paragraph()
                p_d.text = "       " + desc
                p_d.font.name = 'Arial'
                p_d.font.size = Pt(8)
                p_d.font.color.rgb = COLOR_MUTED
                p_d.space_after = Pt(2)

    output_path = r'H:\Final dance\ppts\final-2.pptx'
    prs.save(output_path)
    print(f"Successfully generated {output_path} with total {len(prs.slides)} slides!")

if __name__ == '__main__':
    build_presentation()
