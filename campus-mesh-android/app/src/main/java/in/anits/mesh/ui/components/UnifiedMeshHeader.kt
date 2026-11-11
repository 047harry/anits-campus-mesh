package `in`.anits.mesh.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.anits.mesh.data.UserProfile
import `in`.anits.mesh.ui.theme.*

@Composable
fun UnifiedMeshHeader(
    title: String,
    peerCount: Int,
    userProfile: UserProfile?,
    onSwitchUser: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MeshSurface)
            .border(width = 0.5.dp, color = MeshBorder)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Banana App Brand + Title + Nodes in Chunk
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(MeshSurfaceElevated, CircleShape)
                    .border(1.dp, BananaGold.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🍌", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    color = BananaGold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                color = if (peerCount > 0) MintGreen else RoseAlert,
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (peerCount > 0) "$peerCount Nodes Online" else "Discovering Nodes...",
                        color = if (peerCount > 0) MintGreen else TextTertiary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // User Account Chip + Logout / Switch Icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(MeshSurfaceElevated, RoundedCornerShape(20.dp))
                .border(1.dp, MeshBorder, RoundedCornerShape(20.dp))
                .clickable { onSwitchUser() }
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(MintGreen.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userProfile?.fullName?.take(1)?.uppercase() ?: "U",
                    color = MintGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${userProfile?.fullName ?: "User"} (${userProfile?.rollNumber ?: ""})",
                color = TextPrimary,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                Icons.Default.SwapHoriz,
                contentDescription = "Switch Account / Logout",
                tint = TextSecondary,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}


