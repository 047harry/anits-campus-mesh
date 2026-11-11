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
    object Sos : Screen("sos", "SOS", "🚨", RoseAlert)
    object Bulletins : Screen("bulletins", "Radio", "📡", BananaGold)
    object Chat : Screen("chat", "Chat", "💬", MintGreen)
    object Map : Screen("map", "Map", "🗺️", CyberCyan)
    object Profile : Screen("profile", "Settings", "⚙️", TextSecondary)
}

val NavScreens = listOf(Screen.Sos, Screen.Bulletins, Screen.Chat, Screen.Map, Screen.Profile)

@Composable
fun MeshBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        color = MeshSurfaceElevated,
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MeshBorder),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavScreens.forEach { screen ->
                val isSelected = (currentRoute == screen.route)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp)
                        .background(
                            if (isSelected) screen.accentColor.copy(alpha = 0.15f) else Color.Transparent,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { onNavigate(screen.route) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = screen.icon,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = screen.title,
                            color = if (isSelected) screen.accentColor else TextTertiary,
                            fontSize = 10.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
