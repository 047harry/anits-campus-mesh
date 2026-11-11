package `in`.anits.mesh.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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

    val hazards = listOf("Medical Trauma", "Trapped in Room", "Smoke / Fire", "Power Blackout")

    // Abort Countdown Dialog
    if (sosState == SosState.COUNTDOWN) {
        Dialog(onDismissRequest = { viewModel.cancelSosCountdown() }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MineDirtDark, shape = RoundedCornerShape(12.dp))
                    .border(3.dp, MineRedstone, shape = RoundedCornerShape(12.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⚔️ BROADCASTING SOS IN", color = MineRedstone, fontSize = 13.sp, fontWeight = FontWeight.Black)
                    Text("$countdown", color = MineBananaYellow, fontSize = 64.sp, fontWeight = FontWeight.Black)
                    Text("Flooding emergency signal to nearby peers", color = TextMuted, fontSize = 11.5.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.cancelSosCountdown() },
                        colors = ButtonDefaults.buttonColors(containerColor = MineRedstone),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(2.dp, Color.White)
                    ) {
                        Text("CANCEL BEACON", color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MineStoneDark)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Location Card (Minecraft Block Style)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MineDirtDark, shape = RoundedCornerShape(8.dp))
                .border(2.dp, MineStoneBorder, shape = RoundedCornerShape(8.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("CURRENT CHUNK / COORDS", color = MineDiamondBlue, fontSize = 9.5.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(2.dp))
                Text(location, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .background(MineGrassGreen, shape = RoundedCornerShape(6.dp))
                    .border(1.dp, MineEmerald, shape = RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("BLE RADAR OK", color = Color.White, fontSize = 9.5.sp, fontWeight = FontWeight.Black)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Center SOS Button
        EmergencySosButton(
            isTriggered = (sosState == SosState.CONFIRMED_ACK),
            onSosTriggered = { viewModel.startSosCountdown() }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Hazard selector pills
        Text(
            text = "SELECT EVENT HAZARD:",
            color = MineBananaYellow,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            hazards.take(2).forEach { hazard ->
                MineHazardPill(
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
                MineHazardPill(
                    title = hazard,
                    isSelected = (selectedHazard == hazard),
                    onClick = { viewModel.selectHazard(hazard) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Gamified Status Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MineDirtDark),
            border = androidx.compose.foundation.BorderStroke(2.dp, MineStoneBorder),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🛡️", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "HARDWARE MESH DISASTER SHIELD",
                        color = MineEmerald,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "100% Offline peer hopping via BLE 5.0 and Wi-Fi LAN broadcast.",
                        color = TextMuted,
                        fontSize = 10.5.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MineHazardPill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(if (isSelected) MineRedstone.copy(alpha = 0.25f) else MineDirtDark, RoundedCornerShape(6.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MineRedstone else MineStoneBorder,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) MineRedstone else Color.White,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
        )
    }
}
