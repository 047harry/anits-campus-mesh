package `in`.anits.mesh.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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

sealed class Screen(val route: String, val title: String, val icon: ImageVector, val accentColor: Color) {
    object Sos : Screen("sos", "SOS", Icons.Default.Warning, EmergencyRed)
    object Bulletins : Screen("bulletins", "Bulletins", Icons.Default.Campaign, AuthorityGold)
    object Chat : Screen("chat", "Mesh Chat", Icons.Default.Chat, DirectBlue)
    object Map : Screen("map", "Map", Icons.Default.LocationOn, MeshGreen)
}

val NavScreens = listOf(Screen.Sos, Screen.Bulletins, Screen.Chat, Screen.Map)

@Composable
fun MeshBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = Color.Black,
        modifier = modifier.height(72.dp)
    ) {
        NavScreens.forEach { screen ->
            val isSelected = (currentRoute == screen.route)
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(screen.route) },
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.title,
                        tint = if (isSelected) screen.accentColor else TextMuted
                    )
                },
                label = {
                    Text(
                        text = screen.title,
                        color = if (isSelected) screen.accentColor else TextMuted,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
