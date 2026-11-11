package `in`.anits.mesh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import `in`.anits.mesh.data.UserManager
import `in`.anits.mesh.model.UserRole
import `in`.anits.mesh.ui.components.GoldShieldNoticeCard
import `in`.anits.mesh.ui.theme.*
import `in`.anits.mesh.viewmodel.BulletinViewModel

@Composable
fun BulletinsScreen(
    viewModel: BulletinViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bulletins by viewModel.bulletins.collectAsState()
    val userProfile = remember { UserManager.getProfile(context) }
    val isAuthority = userProfile?.role in listOf(UserRole.HOD, UserRole.PRINCIPAL_DEAN, UserRole.FACULTY_WARDEN)

    var showBroadcastDialog by remember { mutableStateOf(false) }
    var directiveText by remember { mutableStateOf("") }

    if (showBroadcastDialog) {
        Dialog(onDismissRequest = { showBroadcastDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF141210), shape = RoundedCornerShape(12.dp))
                    .border(2.dp, Color(0xFFFFE600), shape = RoundedCornerShape(12.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📢", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Broadcast Authority Bulletin",
                            color = Color(0xFFFFE600),
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "This directive will flood across all nearby peer devices in the mesh with priority delivery.",
                        color = Color(0xFF8696A0),
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = directiveText,
                        onValueChange = { directiveText = it },
                        placeholder = {
                            Text(
                                "e.g. Earthquake aftershock: North stairwell clear. Evacuate to Central Ground.",
                                color = Color(0xFF8696A0),
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFFFE600),
                            unfocusedBorderColor = Color(0xFF222D34),
                            focusedContainerColor = Color(0xFF202C33),
                            unfocusedContainerColor = Color(0xFF202C33)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showBroadcastDialog = false }) {
                            Text("CANCEL", color = Color(0xFF8696A0), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (directiveText.isNotBlank()) {
                                    viewModel.publishDirective(directiveText)
                                    directiveText = ""
                                    showBroadcastDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("BROADCAST ➔", color = Color(0xFF0B141A), fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B141A))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CAMPUS RADIO BULLETINS",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "AUTHORITY EMERGENCY BROADCASTS",
                    color = Color(0xFF25D366),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isAuthority) {
                Button(
                    onClick = { showBroadcastDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFE600)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("📢 Broadcast", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.5.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(bulletins) { notice ->
                GoldShieldNoticeCard(
                    senderName = notice.senderName,
                    senderRole = notice.senderRole,
                    timestampText = notice.timestampText,
                    hopCount = notice.hopCount,
                    content = notice.directiveText
                )
            }
        }
    }
}
