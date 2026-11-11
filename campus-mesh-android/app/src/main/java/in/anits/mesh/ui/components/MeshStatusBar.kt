package `in`.anits.mesh.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.anits.mesh.ui.theme.*

@Composable
fun MeshStatusBar(
    peerCount: Int,
    batteryPercent: Int,
    isBlackoutActive: Boolean,
    modifier: Modifier = Modifier
) {
    // Gamified Minecraft HUD Status Strip
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .background(MineDirtDark)
            .border(width = 1.5.dp, color = MineStoneBorder)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🍌", fontSize = 16.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color = if (peerCount > 0) MineEmerald else MineRedstone, shape = RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (peerCount > 0) "$peerCount Nodes in Chunk" else "Searching Chunks...",
                color = if (peerCount > 0) MineEmerald else TextMuted,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Black
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isBlackoutActive) {
                Box(
                    modifier = Modifier
                        .background(color = MineRedstone.copy(alpha = 0.25f), shape = RoundedCornerShape(4.dp))
                        .border(1.dp, MineRedstone, shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "⚔️ BLACKOUT",
                        color = MineRedstone,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Box(
                modifier = Modifier
                    .background(MineStoneDark, RoundedCornerShape(4.dp))
                    .border(1.dp, MineStoneBorder, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "🔋 $batteryPercent%",
                    color = if (batteryPercent > 20) MineBananaYellow else MineRedstone,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}
