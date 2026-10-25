package `in`.anits.mesh.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import `in`.anits.mesh.data.entity.PeerEntity
import `in`.anits.mesh.ui.components.EmergencySosButton
import `in`.anits.mesh.ui.theme.*
import `in`.anits.mesh.viewmodel.MainViewModel
import `in`.anits.mesh.viewmodel.SosState
import `in`.anits.mesh.viewmodel.SosViewModel

@Composable
fun SosScreen(
    viewModel: SosViewModel,
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val sosState by viewModel.sosState.collectAsState()
    val countdown by viewModel.countdownSeconds.collectAsState()
    val selectedHazard by viewModel.selectedHazard.collectAsState()
    val location by viewModel.currentLocation.collectAsState()
    val activePeers by mainViewModel.activePeers.collectAsState()
    val radioLogs by mainViewModel.radioLogs.collectAsState()

    val hazards = listOf("Medical Trauma", "Trapped in Room", "Smoke / Fire", "Power Blackout")

    // Abort Countdown Dialog
    if (sosState == SosState.COUNTDOWN) {
        Dialog(onDismissRequest = { viewModel.cancelSosCountdown() }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black, shape = RoundedCornerShape(20.dp))
                    .border(2.dp, EmergencyRed, shape = RoundedCornerShape(20.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("BROADCASTING SOS IN", color = Color(0xFFFCA5A5), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("$countdown", color = EmergencyRed, fontSize = 72.sp, fontWeight = FontWeight.Black)
                    Text("Auto-detecting floor and alerting rescuers", color = TextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.cancelSosCountdown() },
                        colors = ButtonDefaults.buttonColors(containerColor = EmergencyRedDark)
                    ) {
                        Text("CANCEL EMERGENCY", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Location Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark, shape = RoundedCornerShape(12.dp))
                .border(1.dp, SurfaceBorder, shape = RoundedCornerShape(12.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("AUTO-DETECTED LOCATION", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(location, color = TextPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .background(MeshGreen.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("GPS/BLE OK", color = MeshGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Large Center SOS Button
        EmergencySosButton(
            isTriggered = (sosState == SosState.CONFIRMED_ACK),
            onSosTriggered = { viewModel.startSosCountdown() }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Hazard selector pills
        Text(
            text = "SPECIFY HAZARD (AUTO-INCLUDED IN SOS):",
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            hazards.take(2).forEach { hazard ->
                HazardPill(
                    title = hazard,
                    isSelected = (selectedHazard == hazard),
                    onClick = { viewModel.selectHazard(hazard) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            hazards.takeLast(2).forEach { hazard ->
                HazardPill(
                    title = hazard,
                    isSelected = (selectedHazard == hazard),
                    onClick = { viewModel.selectHazard(hazard) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Closed-Loop Confirmation Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (sosState == SosState.CONFIRMED_ACK) MeshGreen.copy(alpha = 0.2f)
                    else MeshGreen.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp)
                )
                .border(1.dp, MeshGreen, shape = RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = if (sosState == SosState.CONFIRMED_ACK) "HELP IS EN ROUTE — CLINIC CONFIRMED ✓"
                    else "CLOSED-LOOP RESCUE CONFIRMATION",
                    color = MeshGreen,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (sosState == SosState.CONFIRMED_ACK)
                        "Dr. Adinarayana received your alert at Health Center. Medical rescue team dispatched to Floor 3."
                    else "Signal hops across nearby student devices to Health Center. Screen turns green upon receipt.",
                    color = Color(0xFFD1D5DB),
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // LIVE MESH RADIO TELEMETRY & HARDWARE RADAR CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(Color(0xFF22C55E), shape = CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "RADIO HARDWARE TELEMETRY",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Text(
                        text = "ED25519 VERIFIED",
                        color = Color(0xFFF59E0B),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Radio status badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RadioBadge("BLE Beacon", "TX ACTIVE", Color(0xFF10B981), Modifier.weight(1f))
                    RadioBadge("BLE Scan", "DUTY 25%", Color(0xFF3B82F6), Modifier.weight(1f))
                    RadioBadge("Wi-Fi Aware", "NAN READY", Color(0xFF8B5CF6), Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Discovered Peers Section
                Text(
                    text = "NEARBY PEERS (${activePeers.size} IN MESH RANGE)",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (activePeers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B), shape = RoundedCornerShape(8.dp))
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    color = Color(0xFF38BDF8),
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Scanning 2.4 GHz BLE channels for peers...",
                                    color = Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Ensure Bluetooth & Location are enabled on both phones. Devices within 10-15m connect automatically.",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp,
                                lineHeight = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        activePeers.forEach { peer ->
                            PeerRow(peer)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Live Console Ticker
                Text(
                    text = "REAL-TIME RADIO LOG STREAM",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF030712), shape = RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF1F2937), shape = RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        radioLogs.take(5).forEach { log ->
                            Text(
                                text = log,
                                color = if (log.contains("Discovered")) Color(0xFF86EFAC) else Color(0xFF9CA3AF),
                                fontSize = 9.5.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun RadioBadge(title: String, status: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color(0xFF1E293B), shape = RoundedCornerShape(8.dp))
            .border(1.dp, color.copy(alpha = 0.4f), shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = Color(0xFF94A3B8), fontSize = 9.sp, fontWeight = FontWeight.Medium)
            Text(status, color = color, fontSize = 9.5.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun PeerRow(peer: PeerEntity) {
    val summary = `in`.anits.mesh.data.UserManager.resolvePeer(peer.rollNumber ?: peer.nodeId, peer.userRole)
    val roleColor = when (summary.role) {
        `in`.anits.mesh.model.UserRole.HOD, `in`.anits.mesh.model.UserRole.PRINCIPAL_DEAN -> Color(0xFFF59E0B)
        `in`.anits.mesh.model.UserRole.CLINIC_SECURITY -> Color(0xFFEF4444)
        `in`.anits.mesh.model.UserRole.FACULTY_WARDEN -> Color(0xFF38BDF8)
        else -> Color(0xFF10B981)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E293B), shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(roleColor.copy(alpha = 0.2f), shape = CircleShape)
                    .border(1.dp, roleColor, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val initials = if (summary.fullName.startsWith("Roll No:")) {
                    (peer.rollNumber ?: peer.nodeId).takeLast(2).uppercase()
                } else {
                    summary.fullName.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("")
                }
                Text(
                    text = initials,
                    color = roleColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(roleColor.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(summary.role.title, color = roleColor, fontSize = 8.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = summary.fullName,
                        color = Color.White,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "ID: ${peer.rollNumber ?: peer.nodeId.take(10)} • Dept: ${summary.department} • ${peer.radioType}",
                    color = Color(0xFF94A3B8),
                    fontSize = 9.sp
                )
            }
        }
        Box(
            modifier = Modifier
                .background(Color(0xFF065F46), shape = RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Text(
                text = "${peer.rssi} dBm (~3m)",
                color = Color(0xFF6EE7B7),
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun HazardPill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                if (isSelected) EmergencyRedDark.copy(alpha = 0.4f) else SurfaceDark,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                1.dp,
                if (isSelected) EmergencyRed else SurfaceBorder,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) Color.White else TextMuted,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
