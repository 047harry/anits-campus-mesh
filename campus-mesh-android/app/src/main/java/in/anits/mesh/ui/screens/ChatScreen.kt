package `in`.anits.mesh.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.anits.mesh.model.UserRole
import `in`.anits.mesh.viewmodel.ChatConversation
import `in`.anits.mesh.viewmodel.ChatViewModel
import `in`.anits.mesh.viewmodel.MeshChatMessage

// WhatsApp Dark Theme Palette
private val WhatsAppBg = Color(0xFF0B141A)
private val WhatsAppHeader = Color(0xFF1F2C34)
private val WhatsAppGreen = Color(0xFF25D366)
private val WhatsAppTeal = Color(0xFF00A884)
private val WhatsAppOutgoing = Color(0xFF005C4B)
private val WhatsAppIncoming = Color(0xFF202C33)
private val WhatsAppTickCyan = Color(0xFF53BDEB)
private val WhatsAppTextMuted = Color(0xFF8696A0)
private val WhatsAppBorder = Color(0xFF2A3942)

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val activeConversation by viewModel.activeConversation.collectAsState()
    val conversations by viewModel.conversations.collectAsState()
    val messages by viewModel.messages.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WhatsAppBg)
    ) {
        if (activeConversation == null) {
            // Screen 1: WhatsApp Inbox (List of chats + General Broadcast)
            WhatsAppInboxView(
                conversations = conversations,
                onSelectConversation = { viewModel.openConversation(it) }
            )
        } else {
            // Screen 2: WhatsApp Chat Room (Inside a conversation)
            val currentConv = activeConversation!!
            val convMessages = messages.filter { it.conversationId == currentConv.conversationId }

            WhatsAppChatRoomView(
                conversation = currentConv,
                messages = convMessages,
                onBack = { viewModel.closeConversation() },
                onSendMessage = { text -> viewModel.sendMessage(currentConv, text) }
            )
        }
    }
}

// =========================================================================
// SCREEN 1: WHATSAPP INBOX (Conversations List)
// =========================================================================
@Composable
fun WhatsAppInboxView(
    conversations: List<ChatConversation>,
    onSelectConversation: (ChatConversation) -> Unit
) {
    val generalBroadcast = conversations.firstOrNull { it.isBroadcast }
    val directChats = conversations.filter { !it.isBroadcast }
    val onlineDirectChats = directChats.filter { it.isOnlineInRange }
    val directoryDirectChats = directChats.filter { !it.isOnlineInRange }

    Column(modifier = Modifier.fillMaxSize()) {
        // WhatsApp Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(WhatsAppHeader)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "ANITS Mesh Chat",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "● Offline WhatsApp P2P • Zero Internet Required",
                    color = WhatsAppGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Box(
                modifier = Modifier
                    .background(WhatsAppGreen.copy(alpha = 0.15f), shape = RoundedCornerShape(999.dp))
                    .border(1.dp, WhatsAppGreen.copy(alpha = 0.4f), shape = RoundedCornerShape(999.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${onlineDirectChats.size} PEERS IN RANGE",
                    color = WhatsAppGreen,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 4.dp)
        ) {
            // 1. PINNED SPECIAL: CAMPUS GENERAL BROADCAST (SENDER -> ALL)
            if (generalBroadcast != null) {
                item {
                    Text(
                        text = "PINNED BROADCAST CHANNEL",
                        color = WhatsAppTextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    GeneralBroadcastRow(
                        conversation = generalBroadcast,
                        onClick = { onSelectConversation(generalBroadcast) }
                    )
                    HorizontalDivider(color = WhatsAppBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 6.dp))
                }
            }

            // 2. DISCOVERED PEERS (BLE DIRECT 1-ON-1)
            item {
                Text(
                    text = "DISCOVERED PEERS IN MESH RANGE (${onlineDirectChats.size})",
                    color = WhatsAppTextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (onlineDirectChats.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .background(WhatsAppHeader, shape = RoundedCornerShape(10.dp))
                            .border(1.dp, WhatsAppBorder, shape = RoundedCornerShape(10.dp))
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    color = WhatsAppTeal,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Scanning for nearby peer nodes...",
                                    color = Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Bring 2nd device within 10-15m with Bluetooth ON to connect.",
                                color = WhatsAppTextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            } else {
                items(onlineDirectChats) { conv ->
                    WhatsAppChatRow(
                        conversation = conv,
                        onClick = { onSelectConversation(conv) }
                    )
                }
            }

            // 3. CAMPUS ROSTER (STORE & FORWARD DTN)
            if (directoryDirectChats.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "CAMPUS DIRECTORY (STORE-AND-FORWARD)",
                        color = WhatsAppTextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(directoryDirectChats) { conv ->
                    WhatsAppChatRow(
                        conversation = conv,
                        onClick = { onSelectConversation(conv) }
                    )
                }
            }
        }
    }
}

@Composable
fun GeneralBroadcastRow(
    conversation: ChatConversation,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(Color(0xFF0F1E19))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Loudspeaker / Group broadcast icon
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(WhatsAppGreen.copy(alpha = 0.2f), shape = CircleShape)
                .border(1.5.dp, WhatsAppGreen, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Campaign,
                contentDescription = null,
                tint = WhatsAppGreen,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📢 Campus General Broadcast",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = conversation.lastMessageTimestamp,
                    color = WhatsAppGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(WhatsAppGreen.copy(alpha = 0.25f), shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "SENDER → ALL NODES",
                        color = WhatsAppGreen,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = conversation.lastMessageText,
                    color = WhatsAppTextMuted,
                    fontSize = 11.5.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun WhatsAppChatRow(
    conversation: ChatConversation,
    onClick: () -> Unit
) {
    val roleColor = when (conversation.role) {
        UserRole.HOD, UserRole.PRINCIPAL_DEAN -> Color(0xFFF59E0B) // Gold
        UserRole.CLINIC_SECURITY -> Color(0xFFEF4444) // Red
        UserRole.FACULTY_WARDEN -> Color(0xFF38BDF8) // Sky Blue
        else -> WhatsAppGreen // Emerald
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar circle with Initials + Role indicator
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(roleColor.copy(alpha = 0.15f), shape = CircleShape)
                .border(1.5.dp, if (conversation.isOnlineInRange) roleColor else WhatsAppBorder, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = conversation.title.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString(""),
                color = roleColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black
            )
            // Online green badge
            if (conversation.isOnlineInRange) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(WhatsAppGreen, shape = CircleShape)
                        .border(1.5.dp, WhatsAppBg, shape = CircleShape)
                        .align(Alignment.BottomEnd)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Name + Roll Number in WhatsApp header format!
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = conversation.title,
                        color = Color.White,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${conversation.rollNumber})",
                        color = WhatsAppTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = conversation.lastMessageTimestamp,
                    color = WhatsAppTextMuted,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .background(roleColor.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = conversation.role.title.take(12),
                            color = roleColor,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = conversation.lastMessageText,
                        color = WhatsAppTextMuted,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }

                if (conversation.isOnlineInRange) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF065F46), shape = RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${conversation.rssi} dBm",
                            color = Color(0xFF6EE7B7),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// SCREEN 2: WHATSAPP CHAT ROOM (Inside Conversation)
// =========================================================================
@Composable
fun WhatsAppChatRoomView(
    conversation: ChatConversation,
    messages: List<MeshChatMessage>,
    onBack: () -> Unit,
    onSendMessage: (String) -> Unit
) {
    var textInput by remember { mutableStateOf("") }

    val roleColor = when (conversation.role) {
        UserRole.HOD, UserRole.PRINCIPAL_DEAN -> Color(0xFFF59E0B)
        UserRole.CLINIC_SECURITY -> Color(0xFFEF4444)
        UserRole.FACULTY_WARDEN -> Color(0xFF38BDF8)
        else -> WhatsAppGreen
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // WhatsApp Chat Room Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(WhatsAppHeader)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Inbox",
                    tint = Color.White
                )
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(roleColor.copy(alpha = 0.2f), shape = CircleShape)
                    .border(1.dp, roleColor, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (conversation.isBroadcast) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = WhatsAppGreen, modifier = Modifier.size(20.dp))
                } else {
                    Text(
                        text = conversation.title.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString(""),
                        color = roleColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = conversation.title,
                        color = Color.White,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(${conversation.rollNumber})",
                        color = WhatsAppTextMuted,
                        fontSize = 10.5.sp
                    )
                }
                Text(
                    text = if (conversation.isBroadcast) "📢 Public Mesh Channel • All nodes listening"
                    else if (conversation.isOnlineInRange) "🟢 In Mesh Range (~3m) • E2EE Encrypted"
                    else "📡 Store & Forward DTN • Offline Channel",
                    color = if (conversation.isBroadcast || conversation.isOnlineInRange) WhatsAppGreen else WhatsAppTextMuted,
                    fontSize = 9.5.sp
                )
            }
        }

        // Message List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                WhatsAppBubble(
                    message = msg,
                    isBroadcastRoom = conversation.isBroadcast
                )
            }
        }

        // WhatsApp Bottom Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(WhatsAppHeader)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = {
                    Text(
                        text = if (conversation.isBroadcast) "Broadcast to all mesh nodes..." else "Message ${conversation.rollNumber}...",
                        color = WhatsAppTextMuted,
                        fontSize = 12.5.sp
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = WhatsAppIncoming,
                    unfocusedContainerColor = WhatsAppIncoming,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (textInput.isNotBlank()) {
                        onSendMessage(textInput)
                        textInput = ""
                    }
                },
                modifier = Modifier
                    .size(46.dp)
                    .background(WhatsAppGreen, shape = CircleShape)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// =========================================================================
// WHATSAPP CHAT BUBBLE COMPONENT
// =========================================================================
@Composable
fun WhatsAppBubble(
    message: MeshChatMessage,
    isBroadcastRoom: Boolean
) {
    val isOut = message.isOutgoing
    val bubbleColor = if (isOut) WhatsAppOutgoing else WhatsAppIncoming

    val senderRoleColor = when (message.senderRole) {
        UserRole.HOD, UserRole.PRINCIPAL_DEAN -> Color(0xFFF59E0B)
        UserRole.CLINIC_SECURITY -> Color(0xFFEF4444)
        UserRole.FACULTY_WARDEN -> Color(0xFF38BDF8)
        else -> WhatsAppGreen
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isOut) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 290.dp)
                .background(
                    color = bubbleColor,
                    shape = RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isOut) 12.dp else 2.dp,
                        bottomEnd = if (isOut) 2.dp else 12.dp
                    )
                )
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Column {
                // If in General Broadcast room and incoming: Show Name + Roll Number + Role Badge!
                if (!isOut && isBroadcastRoom) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(senderRoleColor.copy(alpha = 0.25f), shape = RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = message.senderRole.title,
                                color = senderRoleColor,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${message.senderName} (${message.senderRoll})",
                            color = senderRoleColor,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                }

                // Message Text
                Text(
                    text = message.text,
                    color = Color.White,
                    fontSize = 12.5.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Time + WhatsApp Double Checkmark
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message.timestamp,
                        color = WhatsAppTextMuted,
                        fontSize = 9.sp
                    )
                    if (isOut) {
                        Spacer(modifier = Modifier.width(4.dp))
                        // Double Cyan checkmark for Delivered, single checkmark for relaying
                        Text(
                            text = if (message.deliveryStatus == "DELIVERED") "✓✓" else "✓",
                            color = if (message.deliveryStatus == "DELIVERED") WhatsAppTickCyan else WhatsAppTextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
