package `in`.anits.mesh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.anits.mesh.data.UserManager
import `in`.anits.mesh.model.UserRole
import `in`.anits.mesh.ui.theme.*

@Composable
fun ProfileSettingsScreen(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfile by UserManager.currentUser.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MeshBackground)
            .verticalScroll(scrollState)
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Profile Avatar Card
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(MintGreen.copy(alpha = 0.15f))
                .border(2.dp, MintGreen, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userProfile?.fullName?.split(" ")?.mapNotNull { it.firstOrNull() }?.take(2)?.joinToString("") ?: "AN",
                color = MintGreen,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = userProfile?.fullName ?: "Campus User",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(2.dp))

        Surface(
            color = MintGreenSubtle,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MintGreen.copy(alpha = 0.3f))
        ) {
            Text(
                text = "${userProfile?.role?.title ?: "Student"} • ${userProfile?.department ?: "CSD"}",
                color = MintGreen,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Identity & Mesh Credentials Card
        Text(
            text = "CAMPUS CREDENTIALS",
            color = TextTertiary,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MeshSurface,
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MeshBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileInfoRow(
                    icon = Icons.Default.Badge,
                    title = "Roll / Identification No.",
                    value = userProfile?.rollNumber ?: "Not Assigned"
                )
                HorizontalDivider(color = MeshBorder.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 12.dp))
                ProfileInfoRow(
                    icon = Icons.Default.Apartment,
                    title = "Department / Faculty",
                    value = userProfile?.department ?: "Computer Science & Engineering"
                )
                HorizontalDivider(color = MeshBorder.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 12.dp))
                ProfileInfoRow(
                    icon = Icons.Default.Security,
                    title = "Authority Level",
                    value = if (userProfile?.role in listOf(UserRole.HOD, UserRole.PRINCIPAL_DEAN)) "Verified Authority (Broadcast Access)" else "Standard Occupant"
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Hardware & Mesh Node Card
        Text(
            text = "HARDWARE & ENCRYPTION",
            color = TextTertiary,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MeshSurface,
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MeshBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileInfoRow(
                    icon = Icons.Default.Sensors,
                    title = "Mesh Node Identifier",
                    value = userProfile?.nodeId ?: "offline"
                )
                HorizontalDivider(color = MeshBorder.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 12.dp))
                ProfileInfoRow(
                    icon = Icons.Default.Key,
                    title = "Ed25519 Public Key",
                    value = userProfile?.publicKeyHex?.take(24)?.let { "$it..." } ?: "Generated On-Device"
                )
                HorizontalDivider(color = MeshBorder.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 12.dp))
                ProfileInfoRow(
                    icon = Icons.Default.CellTower,
                    title = "Radios Active",
                    value = "BLE 5.0 + Wi-Fi Direct (Hop TTL: 7)"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Logout / Switch User Action
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MeshSurfaceElevated),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, RoseAlert.copy(alpha = 0.6f))
        ) {
            Icon(
                Icons.Default.Logout,
                contentDescription = null,
                tint = RoseAlert,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Switch Account / Log Out",
                color = RoseAlert,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(MeshSurfaceElevated, CircleShape)
                .border(1.dp, MeshBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MintGreen,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                color = TextTertiary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

