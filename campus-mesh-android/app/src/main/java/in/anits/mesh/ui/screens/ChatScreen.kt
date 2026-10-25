package `in`.anits.mesh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.anits.mesh.data.UserManager
import `in`.anits.mesh.model.UserRole
import `in`.anits.mesh.ui.theme.*
import `in`.anits.mesh.viewmodel.ChatMessage
import `in`.anits.mesh.viewmodel.ChatViewModel

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.messages.collectAsState()
    val activePeers by viewModel.activePeers.collectAsState()
    val selectedPeer by viewModel.selectedPeer.collectAsState()
    var inputText by remember { mutableStateOf("") }

    val activePeerSummary = selectedPeer?.let {
        UserManager.resolvePeer(it.rollNumber ?: it.nodeId, it.userRole)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(14.dp)
    ) {
        // Discovered Peer Selector Strip (if peers detected)
        if (activePeers.isNotEmpty()) {
            Text(
                text = "AVAILABLE PEERS IN MESH RANGE (${activePeers.size}):",
                color = Color(0xFF94A3B8),
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(activePeers) { peer ->
                    val summary = UserManager.resolvePeer(peer.rollNumber ?: peer.nodeId, peer.userRole)
                    val isSelected = (selectedPeer?.nodeId == peer.nodeId)
                    val chipColor = when (summary.role) {
                        UserRole.HOD, UserRole.PRINCIPAL_DEAN -> AuthorityGold
                        UserRole.CLINIC_SECURITY -> EmergencyRed
                        UserRole.FACULTY_WARDEN -> Color(0xFF38BDF8)
                        else -> MeshGreen
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected) chipColor.copy(alpha = 0.2f) else SurfaceDark,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                1.dp,
                                if (isSelected) chipColor else SurfaceBorder,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { viewModel.selectPeer(peer) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(chipColor, shape = CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = summary.fullName.split(" ").take(2).joinToString(" "),
                                color = if (isSelected) Color.White else TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Active Chat Header Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark, shape = RoundedCornerShape(12.dp))
                .border(1.dp, SurfaceBorder, shape = RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val roleColor = when (activePeerSummary?.role) {
                UserRole.HOD, UserRole.PRINCIPAL_DEAN -> AuthorityGold
                UserRole.CLINIC_SECURITY -> EmergencyRed
                UserRole.FACULTY_WARDEN -> Color(0xFF38BDF8)
                else -> DirectBlue
            }

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(roleColor.copy(alpha = 0.2f), shape = CircleShape)
                    .border(1.5.dp, roleColor, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = activePeerSummary?.fullName?.split(" ")?.mapNotNull { it.firstOrNull() }?.take(2)?.joinToString("") ?: "P",
                    color = roleColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activePeerSummary?.fullName ?: "Scanning for nearby peers...",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = DirectBlue,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "E2EE Encrypted • ${activePeerSummary?.role?.title ?: "BLE Offline Channel"}",
                        color = DirectBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            selectedPeer?.let {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF065F46), shape = RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${it.rssi} dBm",
                        color = Color(0xFF6EE7B7),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Message List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                DynamicChatBubble(msg)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark, shape = RoundedCornerShape(24.dp))
                .border(1.dp, SurfaceBorder, shape = RoundedCornerShape(24.dp))
                .padding(horizontal = 14.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Type encrypted mesh message...", color = TextMuted, fontSize = 12.5.sp) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    }
                }
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Send Message",
                    tint = DirectBlue
                )
            }
        }
    }
}

@Composable
fun DynamicChatBubble(msg: ChatMessage) {
    val roleColor = when (msg.senderRole) {
        UserRole.HOD, UserRole.PRINCIPAL_DEAN -> AuthorityGold
        UserRole.CLINIC_SECURITY -> EmergencyRed
        UserRole.FACULTY_WARDEN -> Color(0xFF38BDF8)
        else -> MeshGreen
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (msg.isOutgoing) Alignment.End else Alignment.Start
    ) {
        // Sender Name Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(roleColor.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = msg.senderRole.title,
                    color = roleColor,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = msg.senderName,
                color = Color(0xFFCBD5E1),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    if (msg.isOutgoing) Color(0xFF1E3A8A) else SurfaceDark,
                    shape = RoundedCornerShape(
                        topStart = 14.dp,
                        topEnd = 14.dp,
                        bottomStart = if (msg.isOutgoing) 14.dp else 2.dp,
                        bottomEnd = if (msg.isOutgoing) 2.dp else 14.dp
                    )
                )
                .border(
                    1.dp,
                    if (msg.isOutgoing) Color(0xFF3B82F6) else SurfaceBorder,
                    shape = RoundedCornerShape(
                        topStart = 14.dp,
                        topEnd = 14.dp,
                        bottomStart = if (msg.isOutgoing) 14.dp else 2.dp,
                        bottomEnd = if (msg.isOutgoing) 2.dp else 14.dp
                    )
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Column {
                Text(
                    text = msg.text,
                    color = Color.White,
                    fontSize = 12.5.sp,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = msg.timestamp,
                        color = Color(0xFF94A3B8),
                        fontSize = 9.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "• ${msg.deliveryStatus}",
                        color = if (msg.deliveryStatus == "DELIVERED") Color(0xFF6EE7B7) else Color(0xFFFBBF24),
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
