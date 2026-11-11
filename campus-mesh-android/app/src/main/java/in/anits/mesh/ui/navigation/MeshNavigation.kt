package `in`.anits.mesh.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.anits.mesh.ui.theme.*

sealed class Screen(val route: String, val title: String, val icon: String, val accentColor: Color) {
    object Sos : Screen("sos", "SOS", "🚨", MineRedstone)
    object Bulletins : Screen("bulletins", "Radio", "📡", MineGold)
    object Chat : Screen("chat", "Chat", "💬", MineEmerald)
    object Map : Screen("map", "Map", "🗺️", MineDiamondBlue)
}

val NavScreens = listOf(Screen.Sos, Screen.Bulletins, Screen.Chat, Screen.Map)

@Composable
fun MeshBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Minecraft Hotbar Style Navigation
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MineDirtDark)
            .border(width = 2.dp, color = MineStoneBorder)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavScreens.forEach { screen ->
            val isSelected = (currentRoute == screen.route)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
                    .background(
                        if (isSelected) MineStoneDark else Color.Transparent,
                        RoundedCornerShape(6.dp)
                    )
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) screen.accentColor else MineStoneBorder.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { onNavigate(screen.route) }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = screen.icon,
                        fontSize = 17.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = screen.title,
                        color = if (isSelected) screen.accentColor else TextMuted,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                    )
                }
            }
        }
    }
}
