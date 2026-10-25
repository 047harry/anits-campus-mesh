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
import `in`.anits.mesh.ui.theme.AuthorityGold
import `in`.anits.mesh.ui.theme.SurfaceBorder
import `in`.anits.mesh.ui.theme.SurfaceDark
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
                    .background(Color.Black, shape = RoundedCornerShape(16.dp))
                    .border(2.dp, AuthorityGold, shape = RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Campaign, contentDescription = null, tint = AuthorityGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Broadcast Evacuation Directive", color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Notice will be cryptographically signed with your Ed25519 Authority Key and flooded across all student devices.", color = Color(0xFF94A3B8), fontSize = 11.sp, lineHeight = 14.sp)

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = directiveText,
                        onValueChange = { directiveText = it },
                        placeholder = { Text("e.g. Earthquake aftershock: North Stairwell clear. Evacuate to Cricket Ground triage tent.", color = Color(0xFF64748B), fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AuthorityGold,
                            unfocusedBorderColor = SurfaceBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showBroadcastDialog = false }) {
                            Text("CANCEL", color = Color(0xFF94A3B8))
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
                            colors = ButtonDefaults.buttonColors(containerColor = AuthorityGold)
                        ) {
                            Text("SIGN & BROADCAST", color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("OFFICIAL BULLETINS", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text("CRYPTOGRAPHICALLY VERIFIED OFFLINE", color = AuthorityGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            if (isAuthority) {
                Button(
                    onClick = { showBroadcastDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = AuthorityGold),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("📢 Broadcast", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.5.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
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
