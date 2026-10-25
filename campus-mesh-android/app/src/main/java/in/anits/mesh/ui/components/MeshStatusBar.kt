package `in`.anits.mesh.ui.components

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
import `in`.anits.mesh.ui.theme.*

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
