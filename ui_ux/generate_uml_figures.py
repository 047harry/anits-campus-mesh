import matplotlib.pyplot as plt
import matplotlib.patches as patches

# Color palette matching Review 2
BG_COLOR = '#FFFFFF'
CARD_BG = '#F5ECE3'
CARD_BORDER = '#E2D4C4'
TEXT_DARK = '#0F172A'
TEXT_MUTED = '#334155'
ORANGE = '#D96B43'
GREEN = '#0D9488'
BLUE = '#2563EB'
RED = '#DC2626'

def draw_usecase_diagram(out_path):
    # Widescreen canvas
    fig, ax = plt.subplots(figsize=(11, 7.0), dpi=300)
    fig.patch.set_facecolor(BG_COLOR)
    ax.set_facecolor(BG_COLOR)
    ax.set_xlim(0, 11)
    ax.set_ylim(0, 7.0)
    ax.axis('off')

    # Boundary Box
    rect = patches.FancyBboxPatch((2.4, 0.4), 6.2, 6.2, boxstyle="round,pad=0.12", 
                                  facecolor='#FDFBF9', edgecolor=ORANGE, linewidth=3.0)
    ax.add_patch(rect)
    ax.text(5.5, 6.25, "ANITS Campus Mesh System Boundary", ha='center', va='center', 
            fontsize=16, fontweight='black', color=ORANGE)

    # Actors with prominent cards & large text
    actors = [
        ("Student / Occupant", 1.2, 5.0),
        ("Disaster Victim", 1.2, 3.2),
        ("Faculty / Warden", 1.2, 1.4),
        ("Authority (HOD/Dean)", 9.8, 4.6),
        ("Clinic / Security", 9.8, 2.0),
    ]

    for name, x, y in actors:
        box = patches.FancyBboxPatch((x-1.05, y-0.45), 2.1, 0.9, boxstyle="round,pad=0.08",
                                     facecolor='#F1F5F9', edgecolor='#64748B', linewidth=2.4)
        ax.add_patch(box)
        ax.text(x, y, name, ha='center', va='center', fontsize=12.5, fontweight='black', color=TEXT_DARK)

    # Use Cases (Enlarged ovals with 13-14pt bold text)
    usecases = [
        ("UC1: Emergency SOS Beacon", 5.5, 5.3, RED),
        ("UC2: Authority Radio Directive", 5.5, 4.1, ORANGE),
        ("UC3: P2P Encrypted Mesh Chat", 5.5, 2.9, BLUE),
        ("UC4: Offline Campus Muster Map", 5.5, 1.8, GREEN),
        ("UC5: Peer Discovery & Directory", 5.5, 0.8, '#475569'),
    ]

    for text, x, y, col in usecases:
        ellipse = patches.Ellipse((x, y), 4.6, 0.85, facecolor='#FFFFFF', edgecolor=col, linewidth=2.8)
        ax.add_patch(ellipse)
        ax.text(x, y, text, ha='center', va='center', fontsize=13.5, fontweight='black', color=TEXT_DARK)

    # Connecting arrows
    conns = [
        ((2.25, 3.2), (3.3, 5.3)), # Victim -> SOS
        ((2.25, 5.0), (3.3, 2.9)), # Student -> Chat
        ((2.25, 5.0), (3.3, 1.8)), # Student -> Map
        ((2.25, 5.0), (3.3, 0.8)), # Student -> Discovery
        ((2.25, 1.4), (3.3, 4.1)), # Faculty -> Directive
        ((8.75, 4.6), (7.7, 4.1)), # Authority -> Directive
        ((8.75, 2.0), (7.7, 5.3)), # Clinic -> SOS
    ]

    for (x1, y1), (x2, y2) in conns:
        ax.annotate("", xy=(x2, y2), xytext=(x1, y1),
                    arrowprops=dict(arrowstyle="-", color='#334155', lw=2.2))

    plt.tight_layout()
    plt.savefig(out_path, bbox_inches='tight', dpi=300)
    plt.close()
    print(f"Generated {out_path}")

def draw_class_diagram(out_path):
    fig, ax = plt.subplots(figsize=(11, 7.0), dpi=300)
    fig.patch.set_facecolor(BG_COLOR)
    ax.set_facecolor(BG_COLOR)
    ax.set_xlim(0, 11)
    ax.set_ylim(0, 7.0)
    ax.axis('off')

    classes = [
        ("RadioCoordinator", 4.1, 3.8, 3.1, 2.8, [
            "+ isMeshActive: Boolean",
            "+ startMeshServices()",
            "+ stopMeshServices()",
            "+ transmitChatMessage()"
        ], ORANGE),
        ("BleScannerDaemon", 0.4, 3.8, 3.2, 2.8, [
            "- isScanning: Boolean",
            "- scanFilter: 0x02E5",
            "+ start() / stop()",
            "+ handleScanResult()"
        ], BLUE),
        ("BleAdvertiserDaemon", 0.4, 0.5, 3.2, 2.8, [
            "- isAdvertising: Boolean",
            "- rollNumber: String",
            "+ start() / stop()",
            "+ updateIdentity()"
        ], BLUE),
        ("MeshDatabase (Room)", 4.1, 0.5, 3.1, 2.8, [
            "+ messageDao(): Dao",
            "+ peerDao(): Dao",
            "+ cacheDao(): Dao",
            "- table_messages (Room)"
        ], GREEN),
        ("CryptoEngine", 7.6, 3.8, 3.0, 2.8, [
            "+ generateKeyPair()",
            "+ signPacket(): byte[64]",
            "+ verifySignature(): Bool",
            "+ deriveAesKey(): byte[32]"
        ], '#7C3AED'),
        ("MeshPacket (70B Wire)", 7.6, 0.5, 3.0, 2.8, [
            "+ magic: 'ANIT'",
            "+ packetType: Byte",
            "+ ttl: Byte (Default 7)",
            "+ signature: byte[64]"
        ], '#B45309')
    ]

    for title, x, y, w, h, methods, col in classes:
        card = patches.FancyBboxPatch((x, y), w, h, boxstyle="round,pad=0.08",
                                      facecolor='#FFFFFF', edgecolor=col, linewidth=2.8)
        ax.add_patch(card)
        
        # Header banner
        header = patches.Rectangle((x, y + h - 0.65), w, 0.65, facecolor='#F8FAFC', edgecolor=col, linewidth=1.5)
        ax.add_patch(header)
        ax.text(x + w/2, y + h - 0.32, title, ha='center', va='center', fontsize=13, fontweight='black', color=TEXT_DARK)
        
        line_y = y + h - 1.05
        for m in methods:
            ax.text(x + 0.18, line_y, m, ha='left', va='center', fontsize=12, fontweight='black', color='#1E293B')
            line_y -= 0.42

    arrows = [
        ((4.1, 5.2), (3.6, 5.2)),
        ((4.1, 4.2), (3.6, 2.6)),
        ((5.65, 3.8), (5.65, 3.3)),
        ((7.2, 5.2), (7.6, 5.2)),
        ((5.65, 1.8), (7.6, 1.8)),
    ]
    for (x1, y1), (x2, y2) in arrows:
        ax.annotate("", xy=(x2, y2), xytext=(x1, y1),
                    arrowprops=dict(arrowstyle="->", color=ORANGE, lw=2.5))

    plt.tight_layout()
    plt.savefig(out_path, bbox_inches='tight', dpi=300)
    plt.close()
    print(f"Generated {out_path}")

def draw_sequence_diagram(out_path):
    fig, ax = plt.subplots(figsize=(11, 7.0), dpi=300)
    fig.patch.set_facecolor(BG_COLOR)
    ax.set_facecolor(BG_COLOR)
    ax.set_xlim(0, 11)
    ax.set_ylim(0, 7.0)
    ax.axis('off')

    lifelines = [
        ("Authority (HOD)\n[Vivo Node]", 2.0),
        ("Intermediate Repeater\n[OnePlus Peer]", 5.5),
        ("Student / Target Node\n[Receiving Node]", 9.0)
    ]

    for title, x in lifelines:
        box = patches.FancyBboxPatch((x - 1.5, 5.9), 3.0, 0.95, boxstyle="round,pad=0.08",
                                     facecolor='#F1F5F9', edgecolor=ORANGE, linewidth=2.5)
        ax.add_patch(box)
        ax.text(x, 6.38, title, ha='center', va='center', fontsize=12.5, fontweight='black', color=TEXT_DARK)
        ax.plot([x, x], [0.4, 5.9], linestyle='--', color='#94A3B8', lw=1.8)

    steps = [
        ("1. Publish Evacuation Directive (TTL = 7)", 2.0, 5.5, 5.1, True, ORANGE),
        ("2. Save in Room SQLite DTN Vault", 2.0, 2.0, 4.4, False, '#1E293B'),
        ("3. Verify Ed25519 Authority Signature", 5.5, 5.5, 3.7, False, '#1E293B'),
        ("4. Decrement TTL (7 -> 6) & Re-broadcast", 5.5, 9.0, 3.0, True, ORANGE),
        ("5. Display Verified Gold Shield Card", 9.0, 9.0, 2.3, False, GREEN),
        ("6. Return Delivery ACK Packet", 9.0, 5.5, 1.6, True, BLUE),
        ("7. Relay ACK to Originating HOD", 5.5, 2.0, 0.9, True, BLUE),
    ]

    for text, x1, x2, y, is_arrow, col in steps:
        if is_arrow:
            ax.annotate("", xy=(x2, y), xytext=(x1, y),
                        arrowprops=dict(arrowstyle="->", color=col, lw=2.5))
            mid_x = (x1 + x2) / 2
            ax.text(mid_x, y + 0.18, text, ha='center', va='bottom', fontsize=12, fontweight='black', color=TEXT_DARK)
        else:
            rect = patches.FancyBboxPatch((x1 - 1.6, y - 0.25), 3.2, 0.5, boxstyle="round,pad=0.06",
                                         facecolor='#FFFFFF', edgecolor='#CBD5E1', linewidth=2.0)
            ax.add_patch(rect)
            ax.text(x1, y, text, ha='center', va='center', fontsize=11, fontweight='black', color=col)

    plt.tight_layout()
    plt.savefig(out_path, bbox_inches='tight', dpi=300)
    plt.close()
    print(f"Generated {out_path}")

def draw_activity_diagram(out_path):
    fig, ax = plt.subplots(figsize=(11, 7.0), dpi=300)
    fig.patch.set_facecolor(BG_COLOR)
    ax.set_facecolor(BG_COLOR)
    ax.set_xlim(0, 11)
    ax.set_ylim(0, 7.0)
    ax.axis('off')

    nodes = [
        ("Start: Receive Radio Frame", 5.5, 6.4, 'start'),
        ("1. CRC-32 Frame Integrity Check", 5.5, 5.4, 'action'),
        ("CRC Valid?", 5.5, 4.3, 'decision'),
        ("2. SeenPacketCache UUID Lookup", 5.5, 3.1, 'action'),
        ("Seen Before?", 5.5, 2.0, 'decision'),
        ("3. Decrement TTL & Store in Room DB", 5.5, 0.8, 'action'),
        ("Discard Corrupt Frame", 9.1, 4.3, 'drop'),
        ("Drop Duplicate Frame", 9.1, 2.0, 'drop'),
        ("4. Re-broadcast (TTL - 1)", 1.7, 0.8, 'end')
    ]

    for text, x, y, ntype in nodes:
        if ntype == 'start':
            circle = patches.Circle((x, y), 0.35, facecolor=ORANGE, edgecolor='none')
            ax.add_patch(circle)
            ax.text(x, y + 0.45, text, ha='center', va='center', fontsize=13, fontweight='black', color=TEXT_DARK)
        elif ntype == 'action':
            box = patches.FancyBboxPatch((x - 2.0, y - 0.32), 4.0, 0.64, boxstyle="round,pad=0.08",
                                         facecolor='#F8FAFC', edgecolor=ORANGE, linewidth=2.5)
            ax.add_patch(box)
            ax.text(x, y, text, ha='center', va='center', fontsize=12.5, fontweight='black', color=TEXT_DARK)
        elif ntype == 'decision':
            diamond = patches.Polygon([[x, y+0.42], [x+1.1, y], [x, y-0.42], [x-1.1, y]], 
                                      closed=True, facecolor='#FEF3C7', edgecolor='#D97706', linewidth=2.5)
            ax.add_patch(diamond)
            ax.text(x, y, text, ha='center', va='center', fontsize=11.5, fontweight='black', color=TEXT_DARK)
        elif ntype == 'drop':
            box = patches.FancyBboxPatch((x - 1.5, y - 0.28), 3.0, 0.56, boxstyle="round,pad=0.08",
                                         facecolor='#FEE2E2', edgecolor='#EF4444', linewidth=2.2)
            ax.add_patch(box)
            ax.text(x, y, text, ha='center', va='center', fontsize=11.5, fontweight='black', color='#991B1B')
        elif ntype == 'end':
            box = patches.FancyBboxPatch((x - 1.5, y - 0.32), 3.0, 0.64, boxstyle="round,pad=0.08",
                                         facecolor='#DCFCE7', edgecolor='#16A34A', linewidth=2.5)
            ax.add_patch(box)
            ax.text(x, y, text, ha='center', va='center', fontsize=12, fontweight='black', color='#166534')

    flow_arrows = [
        ((5.5, 6.05), (5.5, 5.72)),
        ((5.5, 5.08), (5.5, 4.72)),
        ((5.5, 3.88), (5.5, 3.42)),
        ((5.5, 2.78), (5.5, 2.42)),
        ((5.5, 1.58), (5.5, 1.12)),
        ((6.6, 4.3), (7.6, 4.3)),
        ((6.6, 2.0), (7.6, 2.0)),
        ((3.5, 0.8), (3.2, 0.8))
    ]

    for (x1, y1), (x2, y2) in flow_arrows:
        ax.annotate("", xy=(x2, y2), xytext=(x1, y1),
                    arrowprops=dict(arrowstyle="->", color='#334155', lw=2.5))

    ax.text(5.75, 3.65, "Yes", fontsize=11.5, fontweight='black', color=GREEN)
    ax.text(7.0, 4.5, "No", fontsize=11.5, fontweight='black', color=RED)
    ax.text(5.75, 1.35, "No (Fresh)", fontsize=11.5, fontweight='black', color=GREEN)
    ax.text(7.0, 2.2, "Yes (Duplicate)", fontsize=11.5, fontweight='black', color=RED)

    plt.tight_layout()
    plt.savefig(out_path, bbox_inches='tight', dpi=300)
    plt.close()
    print(f"Generated {out_path}")

if __name__ == '__main__':
    draw_usecase_diagram(r'H:\Final dance\ui_ux\uml_usecase.png')
    draw_class_diagram(r'H:\Final dance\ui_ux\uml_class.png')
    draw_sequence_diagram(r'H:\Final dance\ui_ux\uml_sequence.png')
    draw_activity_diagram(r'H:\Final dance\ui_ux\uml_activity.png')
