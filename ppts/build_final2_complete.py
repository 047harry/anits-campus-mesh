import sys
import os
import pptx
from pptx import Presentation
from pptx.util import Inches, Pt
from pptx.dml.color import RGBColor
from pptx.enum.text import PP_ALIGN, MSO_ANCHOR
from pptx.enum.shapes import MSO_SHAPE

def build_presentation():
    template_path = r'H:\Final dance\ppts\Review 2.pptx'
    prs = Presentation(template_path)
    
    # Review 2 Dimensions
    slide_width = prs.slide_width   # 13.333 inches
    slide_height = prs.slide_height # 7.5 inches

    # High-Contrast Colors (True Review 2 palette)
    COLOR_WHITE = RGBColor(255, 255, 255)
    COLOR_HEADER_BAR = RGBColor(26, 26, 26)       # #1A1A1A
    COLOR_ORANGE = RGBColor(217, 107, 67)         # #D96B43 (Primary Review 2 Accent)
    COLOR_DARK = RGBColor(15, 23, 42)             # #0F172A (Deep bold slate-black)
    COLOR_SLATE = RGBColor(51, 65, 85)            # #334155 (Crisp readable dark body)
    COLOR_MUTED = RGBColor(100, 116, 139)         # #64748B
    COLOR_CARD_BG = RGBColor(245, 236, 227)       # #F5ECE3 (Warm beige card fill)
    COLOR_CARD_BORDER = RGBColor(226, 212, 196)   # #E2D4C4

    # Keep only Slides 1 to 14 from Review 2
    while len(prs.slides) > 14:
        rId = prs.slides._sldIdLst[len(prs.slides)-1].rId
        prs.part.drop_rel(rId)
        del prs.slides._sldIdLst[len(prs.slides)-1]

    def create_clean_slide(section_num, section_name, slide_title, page_num_str):
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

        # 3. Header Text Box
        header_box = slide.shapes.add_textbox(Inches(0.8), Inches(0.52), Inches(11.7), Inches(0.95))
        tf_h = header_box.text_frame
        tf_h.word_wrap = True
        tf_h.margin_left = tf_h.margin_top = tf_h.margin_right = tf_h.margin_bottom = 0
        
        p_sec = tf_h.paragraphs[0]
        p_sec.text = f"SECTION {section_num:02d}  |  {section_name.upper()}"
        p_sec.font.name = 'Arial'
        p_sec.font.bold = True
        p_sec.font.size = Pt(11)
        p_sec.font.color.rgb = COLOR_ORANGE

        p_title = tf_h.add_paragraph()
        p_title.text = slide_title.upper()
        p_title.font.name = 'Arial'
        p_title.font.bold = True
        p_title.font.size = Pt(21)
        p_title.font.color.rgb = COLOR_DARK

        # 4. Orange Accent Line
        line = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0.8), Inches(1.55), Inches(1.8), Inches(0.04))
        line.fill.solid()
        line.fill.fore_color.rgb = COLOR_ORANGE
        line.line.fill.background()

        # 5. Bottom Footer Text Box
        footer = slide.shapes.add_textbox(Inches(0.8), Inches(7.05), Inches(11.7), Inches(0.35))
        tf_f = footer.text_frame
        p_f = tf_f.paragraphs[0]
        p_f.text = f"ANITS B.Tech Final Year Project Review (2025-2026)  |  Team 12                                                                                                                  Page {page_num_str}"
        p_f.font.name = 'Arial'
        p_f.font.size = Pt(8.5)
        p_f.font.color.rgb = COLOR_MUTED

        return slide

    def add_clean_card(slide, left, top, width, height, title=""):
        card = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, left, top, width, height)
        card.fill.solid()
        card.fill.fore_color.rgb = COLOR_CARD_BG
        card.line.color.rgb = COLOR_CARD_BORDER
        card.line.width = Pt(1.5)
        
        tf = card.text_frame
        tf.word_wrap = True
        tf.margin_left = Inches(0.32)
        tf.margin_right = Inches(0.32)
        tf.margin_top = Inches(0.25)
        tf.margin_bottom = Inches(0.25)
        
        if title:
            p = tf.paragraphs[0]
            p.text = title
            p.font.name = 'Arial'
            p.font.bold = True
            p.font.size = Pt(14)
            p.font.color.rgb = COLOR_ORANGE
            p.space_after = Pt(12)
        return card

    # =========================================================================
    # SLIDE 15: MODULES (PART 1) — High-Visibility, Clean Bullets
    # =========================================================================
    s15 = create_clean_slide(12, "SYSTEM MODULE ARCHITECTURE", "CORE SUBSYSTEMS: DUAL-RADIO & 70-BYTE DTN VAULT", "15 of 24")
    
    m1 = add_clean_card(s15, Inches(0.8), Inches(1.75), Inches(5.6), Inches(5.1), "MODULE 1: HARDWARE RADIO ENGINE")
    mt1 = m1.text_frame
    m1_items = [
        ("BLE Peripheral Advertiser", "Broadcasts node availability, role codes, and roll numbers via 20-byte manufacturer data frames."),
        ("Low-Latency Continuous Scanner", "Hardware filter (0x02E5) offloads packet parsing to Bluetooth chip; 12s scan restart prevents Android sleep."),
        ("Self-Healing Watchdog", "Auto-prompts user if Bluetooth is off; dynamic state receiver resurrects GATT & scanner upon toggle."),
        ("Wi-Fi LAN Multicast UDP Relay", "Broadcasts JSON heartbeats over Port 8888 across campus routers, establishing instant discovery.")
    ]
    for h, d in m1_items:
        p = mt1.add_paragraph()
        r1 = p.add_run()
        r1.text = "• " + h + ": "
        r1.font.bold = True
        r1.font.size = Pt(12.5)
        r1.font.color.rgb = COLOR_DARK
        r2 = p.add_run()
        r2.text = d
        r2.font.size = Pt(12)
        r2.font.color.rgb = COLOR_SLATE
        p.space_after = Pt(8)

    m2 = add_clean_card(s15, Inches(6.8), Inches(1.75), Inches(5.7), Inches(5.1), "MODULE 2: 70-BYTE PROTOCOL & DTN VAULT")
    mt2 = m2.text_frame
    m2_items = [
        ("Binary Wire Frame Serializer", "Rigid 70-byte header: Magic ('ANIT'), Version, Packet Type, Hop Count, TTL, UUIDs, and Payload Length."),
        ("Room SQLite Store-and-Forward", "Persistent DTN vault stores unsent messages on flash storage, surviving power failures and reboots."),
        ("Rolling Ring-Buffer Cache", "Tracks last 1,000 seen UUIDs in memory; drops duplicate packets in O(1) time to eliminate storms."),
        ("7-Hop Dynamic TTL Relay", "Decrements TTL at each student repeater; safely drops expired packets to protect radio channels.")
    ]
    for h, d in m2_items:
        p = mt2.add_paragraph()
        r1 = p.add_run()
        r1.text = "• " + h + ": "
        r1.font.bold = True
        r1.font.size = Pt(12.5)
        r1.font.color.rgb = COLOR_DARK
        r2 = p.add_run()
        r2.text = d
        r2.font.size = Pt(12)
        r2.font.color.rgb = COLOR_SLATE
        p.space_after = Pt(8)

    # =========================================================================
    # SLIDE 16: MODULES (PART 2) — Cryptography & UI Suite
    # =========================================================================
    s16 = create_clean_slide(13, "SYSTEM MODULE ARCHITECTURE", "CORE SUBSYSTEMS: CRYPTOGRAPHIC TRUST & UI SUITE", "16 of 24")

    m3 = add_clean_card(s16, Inches(0.8), Inches(1.75), Inches(5.6), Inches(5.1), "MODULE 3: CRYPTOGRAPHIC TRUST ENGINE")
    mt3 = m3.text_frame
    m3_items = [
        ("Ed25519 Digital Signatures", "Signs all authority directives with sub-5ms asymmetric cryptographic verification to prevent panic spoofing."),
        ("End-to-End Encrypted Peer Chat", "Derives shared ECDH secret keys for AES-GCM encryption; intermediate relay nodes cannot inspect payloads."),
        ("Pre-Crisis Silent Cloud Sync", "Synchronizes student public keys and campus directory records silently with Supabase Cloud during peacetime."),
        ("Local Disk Cache Autonomy", "Stores serialized directory records locally, enabling 100% full directory lookups when cell towers fail.")
    ]
    for h, d in m3_items:
        p = mt3.add_paragraph()
        r1 = p.add_run()
        r1.text = "• " + h + ": "
        r1.font.bold = True
        r1.font.size = Pt(12.5)
        r1.font.color.rgb = COLOR_DARK
        r2 = p.add_run()
        r2.text = d
        r2.font.size = Pt(12)
        r2.font.color.rgb = COLOR_SLATE
        p.space_after = Pt(8)

    m4 = add_clean_card(s16, Inches(6.8), Inches(1.75), Inches(5.7), Inches(5.1), "MODULE 4: ERGONOMIC UI & SETTINGS SUITE")
    mt4 = m4.text_frame
    m4_items = [
        ("Preemptive SOS Dispatcher", "One-touch 3-second countdown dialog broadcasting emergency coordinates with immediate channel preemption."),
        ("Campus Radio Directives", "Authority broadcast portal allowing HODs and Wardens to transmit verified evacuation orders with hop counts."),
        ("Multi-Hop Departmental Chat", "Interactive peer chat with department-based recipient selection, delivery receipts, and chat deletion."),
        ("Unified Header & Settings Tab", "Matte UI with live 'X Nodes Online' indicator, 5th Settings tab, and preset password ('1234').")
    ]
    for h, d in m4_items:
        p = mt4.add_paragraph()
        r1 = p.add_run()
        r1.text = "• " + h + ": "
        r1.font.bold = True
        r1.font.size = Pt(12.5)
        r1.font.color.rgb = COLOR_DARK
        r2 = p.add_run()
        r2.text = d
        r2.font.size = Pt(12)
        r2.font.color.rgb = COLOR_SLATE
        p.space_after = Pt(8)

    # =========================================================================
    # SLIDE 17: FIG. 5 — UML USE CASE DIAGRAM (MAX VISIBILITY)
    # =========================================================================
    s17 = create_clean_slide(14, "FIG. 5: SYSTEM ACTORS & USE CASE SPECIFICATION", "UML USE CASE DIAGRAM: CAMPUS OCCUPANT ROLES & DISASTER ACTIONS", "17 of 24")
    
    uc_img = r'H:\Final dance\ui_ux\uml_usecase.png'
    if os.path.exists(uc_img):
        s17.shapes.add_picture(uc_img, Inches(0.8), Inches(1.75), width=Inches(7.3))

    uc_card = add_clean_card(s17, Inches(8.3), Inches(1.75), Inches(4.233), Inches(5.1), "KEY USE CASES")
    uct = uc_card.text_frame
    uc_specs = [
        ("UC1: Emergency SOS Beacon", "Triggered by disaster victims. Floods casualty coordinates with Tier-1 priority preemption."),
        ("UC2: Radio Directives", "Broadcast by HODs & Principal. Displays Gold Shield cryptographic verification badge."),
        ("UC3: P2P Encrypted Chat", "End-to-end encrypted private messaging across departmental student nodes."),
        ("UC4: Offline Campus Map", "Vector navigation guiding students to ANITS assembly grounds during blackouts.")
    ]
    for h, d in uc_specs:
        p = uct.add_paragraph()
        r1 = p.add_run()
        r1.text = h + "\n"
        r1.font.bold = True
        r1.font.size = Pt(12)
        r1.font.color.rgb = COLOR_DARK
        r2 = p.add_run()
        r2.text = d
        r2.font.size = Pt(11)
        r2.font.color.rgb = COLOR_SLATE
        p.space_after = Pt(10)

    # =========================================================================
    # SLIDE 18: FIG. 6 — UML CLASS DIAGRAM (MAX VISIBILITY)
    # =========================================================================
    s18 = create_clean_slide(15, "FIG. 6: STATIC OBJECT STRUCTURE & RELATIONSHIPS", "UML CLASS DIAGRAM: DATA ACCESS OBJECTS & RADIO DAEMONS", "18 of 24")

    cls_img = r'H:\Final dance\ui_ux\uml_class.png'
    if os.path.exists(cls_img):
        s18.shapes.add_picture(cls_img, Inches(0.8), Inches(1.75), width=Inches(7.3))

    cls_card = add_clean_card(s18, Inches(8.3), Inches(1.75), Inches(4.233), Inches(5.1), "CORE CLASSES")
    clst = cls_card.text_frame
    cls_specs = [
        ("RadioCoordinator", "Central hub controlling BLE scanning, advertising, and UDP socket relays."),
        ("BleScannerDaemon", "Continuous scanner with 12s active cycling and hardware manufacturer filter."),
        ("MeshDatabase (Room)", "Local SQLite store-and-forward persistence (MessageDao, PeerDao, CacheDao)."),
        ("CryptoEngine", "Bouncy Castle Ed25519 signature generator and AES-GCM session cipher.")
    ]
    for h, d in cls_specs:
        p = clst.add_paragraph()
        r1 = p.add_run()
        r1.text = h + "\n"
        r1.font.bold = True
        r1.font.size = Pt(12)
        r1.font.color.rgb = COLOR_DARK
        r2 = p.add_run()
        r2.text = d
        r2.font.size = Pt(11)
        r2.font.color.rgb = COLOR_SLATE
        p.space_after = Pt(10)

    # =========================================================================
    # SLIDE 19: FIG. 7 — UML SEQUENCE DIAGRAM (MAX VISIBILITY)
    # =========================================================================
    s19 = create_clean_slide(16, "FIG. 7: DYNAMIC MULTI-HOP INTERACTION FLOW", "UML SEQUENCE DIAGRAM: AUTHORITY DIRECTIVE BROADCAST & RELAY", "19 of 24")

    seq_img = r'H:\Final dance\ui_ux\uml_sequence.png'
    if os.path.exists(seq_img):
        s19.shapes.add_picture(seq_img, Inches(0.8), Inches(1.75), width=Inches(7.3))

    seq_card = add_clean_card(s19, Inches(8.3), Inches(1.75), Inches(4.233), Inches(5.1), "EXECUTION TRACE")
    seqt = seq_card.text_frame
    seq_specs = [
        ("1. Broadcast Directive", "HOD transmits signed directive with Initial TTL = 7 over peer BLE radios."),
        ("2. Store & Deduplicate", "Intermediate nodes verify Ed25519 signature and cache UUID in SQLite."),
        ("3. Hop Relay (TTL - 1)", "Packets decrement TTL (7 -> 6) and re-broadcast to adjacent corridor nodes."),
        ("4. Display & ACK", "Target node displays Gold Shield Card and routes an ACK back to the HOD.")
    ]
    for h, d in seq_specs:
        p = seqt.add_paragraph()
        r1 = p.add_run()
        r1.text = h + "\n"
        r1.font.bold = True
        r1.font.size = Pt(12)
        r1.font.color.rgb = COLOR_DARK
        r2 = p.add_run()
        r2.text = d
        r2.font.size = Pt(11)
        r2.font.color.rgb = COLOR_SLATE
        p.space_after = Pt(10)

    # =========================================================================
    # SLIDE 20: FIG. 8 — UML ACTIVITY DIAGRAM (MAX VISIBILITY)
    # =========================================================================
    s20 = create_clean_slide(17, "FIG. 8: PACKET PROCESSING & STORM SUPPRESSION FLOW", "UML ACTIVITY DIAGRAM: 3-TIER PACKET VALIDATION LIFECYCLE", "20 of 24")

    act_img = r'H:\Final dance\ui_ux\uml_activity.png'
    if os.path.exists(act_img):
        s20.shapes.add_picture(act_img, Inches(0.8), Inches(1.75), width=Inches(7.3))

    act_card = add_clean_card(s20, Inches(8.3), Inches(1.75), Inches(4.233), Inches(5.1), "DECISION PIPELINE")
    actt = act_card.text_frame
    act_specs = [
        ("Barrier 1: CRC-32 Check", "Frame integrity validation; corrupted radio packets are instantly discarded."),
        ("Barrier 2: Cache Lookup", "UUID lookup against rolling ring-buffer; duplicate frames dropped in O(1)."),
        ("Barrier 3: TTL Decrement", "TTL decremented by 1; frames reaching TTL = 0 terminate to prevent infinite loops."),
        ("Barrier 4: Room DB Dispatch", "Valid packets are saved into SQLite and scheduled for immediate forwarding.")
    ]
    for h, d in act_specs:
        p = actt.add_paragraph()
        r1 = p.add_run()
        r1.text = h + "\n"
        r1.font.bold = True
        r1.font.size = Pt(12)
        r1.font.color.rgb = COLOR_DARK
        r2 = p.add_run()
        r2.text = d
        r2.font.size = Pt(11)
        r2.font.color.rgb = COLOR_SLATE
        p.space_after = Pt(10)

    # =========================================================================
    # SLIDE 21: FIELD EXPERIMENTAL RESULTS & BENCHMARKS
    # =========================================================================
    s21 = create_clean_slide(18, "FIELD EXPERIMENTAL RESULTS & BENCHMARKS", "PHYSICAL HARDWARE TEST MATRIX: VIVO + ONEPLUS + SAMSUNG", "21 of 24")

    tb1 = add_clean_card(s21, Inches(0.8), Inches(1.75), Inches(5.6), Inches(5.1), "BLACKOUT FIELD TESTS")
    tbt1 = tb1.text_frame
    tests_left = [
        ("Authority Directive Relay", "HOD on Vivo broadcasts directive with zero internet. OnePlus student node receives & displays Gold Shield card in 340ms.", "100% SUCCESS"),
        ("Range Recovery Test", "Nodes separated 60m until BLE link drops, then returned. Watchdog automatically re-binds link in 1.4s with zero manual refresh.", "< 2.0s LATENCY"),
        ("Bluetooth Toggle Test", "Bluetooth turned OFF in quick settings. App presents prompt; state receiver re-awakens GATT & scanner upon toggle.", "ZERO DEADLOCK")
    ]
    for h, d, r in tests_left:
        p = tbt1.add_paragraph()
        r1 = p.add_run()
        r1.text = "✔ " + h + "  ➔  "
        r1.font.bold = True
        r1.font.size = Pt(12)
        r1.font.color.rgb = COLOR_DARK
        r_res = p.add_run()
        r_res.text = r + "\n"
        r_res.font.bold = True
        r_res.font.size = Pt(11)
        r_res.font.color.rgb = COLOR_ORANGE
        r2 = p.add_run()
        r2.text = d
        r2.font.size = Pt(11)
        r2.font.color.rgb = COLOR_SLATE
        p.space_after = Pt(8)

    tb2 = add_clean_card(s21, Inches(6.8), Inches(1.75), Inches(5.7), Inches(5.1), "PERFORMANCE BENCHMARKS")
    tbt2 = tb2.text_frame
    tests_right = [
        ("Radio Collision Suppression", "Ring buffer deduplication dropped 100% of redundant packet echoes, keeping channel collision under 3% in dense corridors.", "< 3% COLLISIONS"),
        ("Emergency SOS Preemption", "Active chat transfer immediately paused when SOS beacon was fired. Emergency packet seized radio in < 45ms.", "< 45ms LATENCY"),
        ("Battery Drain Profile", "BLE 5.0 Low-Duty Cycle mode consumed only 3.2% battery per hour, ensuring 24+ hour smartphone survival during extended outages.", "3.2% BATT / HR")
    ]
    for h, d, r in tests_right:
        p = tbt2.add_paragraph()
        r1 = p.add_run()
        r1.text = "✔ " + h + "  ➔  "
        r1.font.bold = True
        r1.font.size = Pt(12)
        r1.font.color.rgb = COLOR_DARK
        r_res = p.add_run()
        r_res.text = r + "\n"
        r_res.font.bold = True
        r_res.font.size = Pt(11)
        r_res.font.color.rgb = COLOR_ORANGE
        r2 = p.add_run()
        r2.text = d
        r2.font.size = Pt(11)
        r2.font.color.rgb = COLOR_SLATE
        p.space_after = Pt(8)

    # =========================================================================
    # SLIDE 22: DEPLOYMENT ARTIFACTS & ACCESS LINKS
    # =========================================================================
    s22 = create_clean_slide(19, "CONTINUOUS DEPLOYMENT & ARTIFACT DISTRIBUTION", "PRODUCTION RELEASES (v1.0.6), GITHUB PAGES & NETLIFY MIRRORS", "22 of 24")

    d1 = add_clean_card(s22, Inches(0.8), Inches(1.75), Inches(5.6), Inches(5.1), "DISTRIBUTION INFRASTRUCTURE")
    dt1 = d1.text_frame
    d_items = [
        ("Official GitHub Release v1.0.6", "Published directly to GitHub Releases with automated binary packaging (`CampusMesh-v1.0.6.apk` and `CampusMesh-v1.0.apk`)."),
        ("Permanent GitHub Pages Portal", "Live at: `https://047harry.github.io/anits-campus-mesh/`\nHosted from repository `/docs` branch with zero server costs."),
        ("Dynamic QR Code Generator", "Built-in dynamic QR generator allows students and faculty to point their smartphone cameras at the screen and download the APK directly."),
        ("Redundant Netlify Cloud Mirror", "Secondary live deployment on Netlify (`https://fluffy-valkyrie-5c0a68.netlify.app`), ensuring zero-downtime campus distribution.")
    ]
    for h, d in d_items:
        p = dt1.add_paragraph()
        r1 = p.add_run()
        r1.text = "• " + h + ": "
        r1.font.bold = True
        r1.font.size = Pt(12.5)
        r1.font.color.rgb = COLOR_DARK
        r2 = p.add_run()
        r2.text = d
        r2.font.size = Pt(11.5)
        r2.font.color.rgb = COLOR_SLATE
        p.space_after = Pt(8)

    d2 = add_clean_card(s22, Inches(6.8), Inches(1.75), Inches(5.7), Inches(5.1), "ACCESS ARTIFACTS & LINKS")
    dt2 = d2.text_frame
    d_links = [
        ("Web Distribution Portal", "https://047harry.github.io/anits-campus-mesh/"),
        ("Netlify Mirror URL", "https://fluffy-valkyrie-5c0a68.netlify.app"),
        ("Official GitHub Release", "https://github.com/047harry/anits-campus-mesh/releases/tag/v1.0.6"),
        ("Direct APK Download", "https://github.com/047harry/anits-campus-mesh/releases/download/v1.0.6/CampusMesh-v1.0.6.apk"),
        ("Target Architecture", "Android 10.0+ (API Level 29 to 34)"),
        ("Binary Package Size", "61.1 MB (Self-contained Room DB + Crypto Engine)")
    ]
    for h, d in d_links:
        p = dt2.add_paragraph()
        r1 = p.add_run()
        r1.text = h + ": "
        r1.font.bold = True
        r1.font.size = Pt(12)
        r1.font.color.rgb = COLOR_DARK
        r2 = p.add_run()
        r2.text = d
        r2.font.size = Pt(11)
        r2.font.color.rgb = COLOR_ORANGE if "http" in d else COLOR_SLATE
        p.space_after = Pt(8)

    # =========================================================================
    # SLIDE 23: CONCLUSION & FUTURE SCOPE
    # =========================================================================
    s23 = create_clean_slide(20, "SUMMARY, SOCIETAL IMPACT & FUTURE ENHANCEMENTS", "CONCLUSION & POST-CAPSTONE SYSTEM ROADMAP", "23 of 24")

    c1 = add_clean_card(s23, Inches(0.8), Inches(1.75), Inches(5.6), Inches(5.1), "KEY CAPSTONE ACHIEVEMENTS")
    ct1 = c1.text_frame
    achieve = [
        ("Zero-Infrastructure Survival", "Commercial smartphones in student pockets form a resilient, self-healing communication grid during catastrophic power grid and cellular blackouts."),
        ("Institutional Role Trust", "First decentralized mesh designed specifically for university campus roles (Principal, HOD, Warden, Clinic, Student)."),
        ("Cryptographic Authenticity", "Ed25519 digital signatures and local SQLite certificate caching eliminate disaster misinformation and panic spoofing."),
        ("100% Production Implementation", "A fully coded, debugged, and field-tested native Android APK deployed across real student smartphones.")
    ]
    for h, d in achieve:
        p = ct1.add_paragraph()
        r1 = p.add_run()
        r1.text = "✔ " + h + ": "
        r1.font.bold = True
        r1.font.size = Pt(12.5)
        r1.font.color.rgb = COLOR_DARK
        r2 = p.add_run()
        r2.text = d
        r2.font.size = Pt(11.5)
        r2.font.color.rgb = COLOR_SLATE
        p.space_after = Pt(8)

    c2 = add_clean_card(s23, Inches(6.8), Inches(1.75), Inches(5.7), Inches(5.1), "RESEARCH ROADMAP")
    ct2 = c2.text_frame
    roadmap = [
        ("LoRa Long-Range Gateways", "Deploying low-cost ESP32 LoRa transceivers on campus water towers to bridge 5km inter-campus gaps across Tagarapuvalasa and Bheemili."),
        ("Wi-Fi Aware NAN Cluster Sockets", "Scaling from BLE 5.0 to high-throughput Wi-Fi Aware NAN data clusters for high-speed offline map tile sharing."),
        ("On-Device AI Triage Bot", "Integrating quantized on-device LLMs (e.g. Gemini Nano) for offline emergency first-aid advice and casualty triaging."),
        ("National Disaster CAP Integration", "Interfacing with the National Disaster Management Authority (NDMA) Common Alerting Protocol.")
    ]
    for h, d in roadmap:
        p = ct2.add_paragraph()
        r1 = p.add_run()
        r1.text = "➔ " + h + ": "
        r1.font.bold = True
        r1.font.size = Pt(12.5)
        r1.font.color.rgb = COLOR_ORANGE
        r2 = p.add_run()
        r2.text = d
        r2.font.size = Pt(11.5)
        r2.font.color.rgb = COLOR_SLATE
        p.space_after = Pt(8)

    # =========================================================================
    # SLIDE 24: THANK YOU (MATCHING REVIEW 2 SLIDE 15 STYLE)
    # =========================================================================
    s24 = prs.slides.add_slide(prs.slide_layouts[6])
    bg24 = s24.shapes.add_shape(MSO_SHAPE.RECTANGLE, 0, 0, slide_width, slide_height)
    bg24.fill.solid()
    bg24.fill.fore_color.rgb = COLOR_WHITE
    bg24.line.fill.background()

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
    p_t3.font.size = Pt(13)
    p_t3.font.color.rgb = COLOR_SLATE
    p_t3.space_after = Pt(18)

    p_t4 = th_tf.add_paragraph()
    p_t4.alignment = PP_ALIGN.CENTER
    p_t4.text = "Presented By: Team 12 (CSD-B)  •  Project Lead: P. Darakesu Hemanth (A23126551113)"
    p_t4.font.name = 'Arial'
    p_t4.font.bold = True
    p_t4.font.size = Pt(13)
    p_t4.font.color.rgb = COLOR_DARK

    p_t5 = th_tf.add_paragraph()
    p_t5.alignment = PP_ALIGN.CENTER
    p_t5.text = "Under the Guidance of: Mrs. A. Bhagya Lakshmi, Asst. Professor, Dept. of CSD"
    p_t5.font.name = 'Arial'
    p_t5.font.size = Pt(12)
    p_t5.font.color.rgb = COLOR_ORANGE
    p_t5.space_after = Pt(14)

    p_t6 = th_tf.add_paragraph()
    p_t6.alignment = PP_ALIGN.CENTER
    p_t6.text = "Live Web Portal: https://047harry.github.io/anits-campus-mesh/  |  Open for Live Demonstration"
    p_t6.font.name = 'Arial'
    p_t6.font.size = Pt(11.5)
    p_t6.font.color.rgb = COLOR_MUTED

    # Update Slide 2 (Agenda)
    slide2 = prs.slides[1]
    for shape in slide2.shapes:
        if shape.name == 'Rounded Rectangle 7':
            tf7 = shape.text_frame
            tf7.clear()
            p = tf7.paragraphs[0]
            p.text = "PHASE 2 & FINAL MODULES"
            p.font.name = 'Arial'
            p.font.bold = True
            p.font.size = Pt(13)
            p.font.color.rgb = COLOR_ORANGE
            p.space_after = Pt(6)

            agenda_items = [
                ("12. SYSTEM MODULES: HARDWARE & DATA LINK", "Dual-radio BLE & 70-byte protocol."),
                ("13. SYSTEM MODULES: CRYPTOGRAPHY & UI", "Ed25519 PKI & Jetpack Compose suite."),
                ("14. FIG. 5: UML USE CASE DIAGRAM", "Occupant roles & emergency actions."),
                ("15. FIG. 6: UML CLASS DIAGRAM", "RadioCoordinator, DAOs & serializers."),
                ("16. FIG. 7: UML SEQUENCE DIAGRAM", "Hop-by-hop message relay execution trace."),
                ("17. FIG. 8: UML ACTIVITY DIAGRAM", "3-tier packet validation & storm suppression."),
                ("18. EXPERIMENTAL VALIDATION & METRICS", "Multi-device test matrix across Vivo & OnePlus."),
                ("19. DEPLOYMENT & LIVE QR WEB PORTAL", "GitHub Release v1.0.6 & Netlify mirrors."),
                ("20. CONCLUSION & FUTURE ROADMAP", "Societal impact & LoRa gateways.")
            ]
            for title, desc in agenda_items:
                p_i = tf7.add_paragraph()
                p_i.text = title
                p_i.font.name = 'Arial'
                p_i.font.bold = True
                p_i.font.size = Pt(10)
                p_i.font.color.rgb = COLOR_DARK
                p_d = tf7.add_paragraph()
                p_d.text = "       " + desc
                p_d.font.name = 'Arial'
                p_d.font.size = Pt(9)
                p_d.font.color.rgb = COLOR_MUTED
                p_d.space_after = Pt(2)

    output_path = r'H:\Final dance\ppts\final-2.pptx'
    prs.save(output_path)
    print(f"Successfully generated {output_path} with {len(prs.slides)} slides!")

if __name__ == '__main__':
    build_presentation()
