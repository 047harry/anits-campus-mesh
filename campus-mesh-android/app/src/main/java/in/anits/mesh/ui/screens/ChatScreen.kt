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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import `in`.anits.mesh.util.ImageCompressor
import `in`.anits.mesh.data.UserManager
import `in`.anits.mesh.model.UserRole
import `in`.anits.mesh.viewmodel.ChatConversation
import `in`.anits.mesh.viewmodel.ChatViewModel
import `in`.anits.mesh.viewmodel.MeshChatMessage

// =========================================================================
// WHATSAPP COLOR PALETTE (True OLED Dark Theme)
// =========================================================================
val WhatsAppDarkBackground = Color(0xFF0C1317)
val WhatsAppHeader = Color(0xFF1F2C34)
val WhatsAppIncoming = Color(0xFF202C33)
val WhatsAppOutgoing = Color(0xFF005C4B)
val WhatsAppGreen = Color(0xFF25D366)
val WhatsAppTickCyan = Color(0xFF53BDEB)
val WhatsAppTextMuted = Color(0xFF8696A0)
val WhatsAppBorder = Color(0xFF222D34)
val RelayCyan = Color(0xFF67E8F9)

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = viewModel(),
    onSwitchUser: (() -> Unit)? = null
) {
    val activeConversation by viewModel.activeConversation.collectAsState()
    val conversations by viewModel.conversations.collectAsState()
    val messages by viewModel.messages.collectAsState()

    var showNewChatDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WhatsAppDarkBackground)
    ) {
        if (activeConversation == null) {
            // Screen 1: WhatsApp Inbox (Conversations List)
            WhatsAppInboxView(
                conversations = conversations,
                onSelectConversation = { viewModel.openConversation(it) },
                onOpenNewChat = { showNewChatDialog = true },
                onSwitchUser = onSwitchUser
            )
        } else {
            // Screen 2: WhatsApp Chat Room (Inside a conversation)
            val currentConv = activeConversation!!
            val convMessages = messages.filter { it.conversationId == currentConv.conversationId }

            WhatsAppChatRoomView(
                conversation = currentConv,
                messages = convMessages,
                onBack = { viewModel.closeConversation() },
                onSendMessage = { text -> viewModel.sendMessage(currentConv, text) },
                onSendPhoto = { uri, caption -> viewModel.sendPhotoMessage(currentConv, uri, caption) }
            )
        }

        // Dialog: Start Private Multi-Hop Chat by Department & Roll Number
        if (showNewChatDialog) {
            NewPrivateChatDialog(
                onDismiss = { showNewChatDialog = false },
                onStartChat = { dept, roll, name ->
                    showNewChatDialog = false
                    viewModel.startNewPrivateChat(dept, roll, name)
                }
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
    onSelectConversation: (ChatConversation) -> Unit,
    onOpenNewChat: () -> Unit,
    onSwitchUser: (() -> Unit)? = null
) {
    var searchQuery by remember { mutableStateOf("") }

    val generalBroadcast = conversations.firstOrNull { it.isBroadcast }
    val directChats = conversations.filter { !it.isBroadcast }
    val onlineDirectChats = directChats.filter { it.isOnlineInRange }
    val multiHopChats = directChats.filter { !it.isOnlineInRange }

    val filteredDirectChats = if (searchQuery.isBlank()) {
        onlineDirectChats
    } else {
        onlineDirectChats.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.rollNumber.contains(searchQuery, ignoreCase = true) ||
            it.department.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredMultiHopChats = if (searchQuery.isBlank()) {
        multiHopChats
    } else {
        multiHopChats.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.rollNumber.contains(searchQuery, ignoreCase = true) ||
            it.department.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenNewChat,
                containerColor = WhatsAppGreen,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 60.dp, end = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Private Chat")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "New Chat",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        },
        containerColor = WhatsAppDarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Clean WhatsApp Top App Bar (Clutter-free, modern)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WhatsAppHeader)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "ANITS Mesh Chat",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                if (onSwitchUser != null) {
                    val userProfile = UserManager.getProfile(LocalContext.current)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(0xFF1E293B), RoundedCornerShape(20.dp))
                            .clickable { onSwitchUser() }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .background(WhatsAppGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userProfile?.fullName?.take(1)?.uppercase() ?: "U",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${userProfile?.fullName ?: "User"} (${userProfile?.rollNumber ?: ""})",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.SwapHoriz,
                            contentDescription = "Switch User",
                            tint = WhatsAppTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // WhatsApp Real-Time Peer Search Bar (Name or Roll Number)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WhatsAppHeader)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Search peer by name or roll number...",
                            color = WhatsAppTextMuted,
                            fontSize = 12.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = if (searchQuery.isNotBlank()) WhatsAppGreen else WhatsAppTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = WhatsAppTextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = WhatsAppIncoming,
                        unfocusedContainerColor = WhatsAppIncoming,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = WhatsAppGreen,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                )
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
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Medium,
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
                        text = "DISCOVERED DIRECT PEERS IN RANGE (${onlineDirectChats.size})",
                        color = WhatsAppTextMuted,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                if (filteredDirectChats.isEmpty()) {
                    item {
                        EmptyStateHint(
                            title = "Scanning direct physical radios...",
                            subtitle = "Nearby Bluetooth LE / Wi-Fi peers will appear here automatically."
                        )
                    }
                } else {
                    items(filteredDirectChats) { conv ->
                        WhatsAppConversationRow(
                            conversation = conv,
                            onClick = { onSelectConversation(conv) }
                        )
                    }
                }

                // 3. MULTI-HOP PRIVATE CHATS (Across campus blocks)
                if (multiHopChats.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "MULTI-HOP PRIVATE CHATS (${multiHopChats.size})",
                            color = RelayCyan,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    items(filteredMultiHopChats) { conv ->
                        WhatsAppConversationRow(
                            conversation = conv,
                            onClick = { onSelectConversation(conv) }
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// NEW PRIVATE CHAT DIALOG: DEPARTMENT & ROLL NUMBER SELECTOR
// =========================================================================
@Composable
fun NewPrivateChatDialog(
    onDismiss: () -> Unit,
    onStartChat: (dept: String, roll: String, name: String) -> Unit
) {
    val departments = listOf("MECH", "CSE", "CSD", "ECE", "CIVIL", "IT", "AIDS", "EEE")
    var selectedDept by remember { mutableStateOf("MECH") }
    var rollInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }

    val campusDir by UserManager.campusDirectory.collectAsState()
    val registeredInDept = remember(selectedDept, campusDir) {
        campusDir.values.filter { it.department.equals(selectedDept, ignoreCase = true) }
    }

    val defaultDepartmentPeers = remember(selectedDept) {
        when (selectedDept) {
            "MECH" -> listOf("5124" to "Friend (Mechanical Block)", "5102" to "Lab Partner (Mech)", "MECH-01" to "Mechanical Faculty / HOD")
            "CSE" -> listOf("4012" to "Rahul (CSE Block B)", "4055" to "Classmate (Lab 3)", "CSE-01" to "CSE Department Lead")
            "CSD" -> listOf("1" to "Student a (CSD Lab)", "2" to "Student b (CSD Lab)", "CSD-HOD" to "Dr. K. Venkata Lakshmi (HOD)")
            "ECE" -> listOf("3015" to "Friend (ECE Core)", "3022" to "Student (ECE Lab)")
            "CIVIL" -> listOf("2001" to "Civil Quad Student", "2010" to "Survey Team")
            "IT" -> listOf("6004" to "IT Block Student", "6018" to "IT Lab 1")
            "AIDS" -> listOf("7001" to "AIDS Lead", "7014" to "AI Lab Peer")
            else -> listOf("1001" to "Campus Student", "1002" to "Hostel Occupant")
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = WhatsAppHeader,
            border = androidx.compose.foundation.BorderStroke(1.dp, WhatsAppBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(RelayCyan.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = RelayCyan, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Start Private Mesh Chat",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Multi-Hop Hopping across Campus Blocks",
                            color = RelayCyan,
                            fontSize = 10.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 1. Select Department
                Text(
                    text = "1. SELECT RECIPIENT'S DEPARTMENT:",
                    color = WhatsAppTextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(departments) { dept ->
                        val isSel = dept == selectedDept
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSel) WhatsAppGreen else WhatsAppIncoming,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSel) WhatsAppGreen else WhatsAppBorder,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { selectedDept = dept }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = dept,
                                color = if (isSel) Color.Black else Color.White,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Black else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Quick Suggestions in selected Department (1-Tap to Chat)
                Text(
                    text = "2. PEERS IN $selectedDept (TAP TO START):",
                    color = RelayCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Registered users from cloud directory
                    items(registeredInDept) { st ->
                        Box(
                            modifier = Modifier
                                .background(WhatsAppIncoming, shape = RoundedCornerShape(8.dp))
                                .border(1.dp, RelayCyan.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp))
                                .clickable {
                                    onStartChat(selectedDept, st.rollNumber, st.fullName)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Column {
                                Text(
                                    text = st.fullName,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Roll: ${st.rollNumber} ➔",
                                    color = RelayCyan,
                                    fontSize = 9.5.sp
                                )
                            }
                        }
                    }

                    // Predefined offline campus peers
                    items(defaultDepartmentPeers) { (r, name) ->
                        Box(
                            modifier = Modifier
                                .background(WhatsAppIncoming, shape = RoundedCornerShape(8.dp))
                                .border(1.dp, WhatsAppBorder, shape = RoundedCornerShape(8.dp))
                                .clickable {
                                    onStartChat(selectedDept, r, name)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Column {
                                Text(
                                    text = name,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Roll: $r ➔",
                                    color = WhatsAppGreen,
                                    fontSize = 9.5.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Or Enter Any Custom Roll Number
                Text(
                    text = "3. OR ENTER SPECIFIC ROLL NUMBER:",
                    color = WhatsAppTextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = rollInput,
                    onValueChange = { rollInput = it },
                    placeholder = {
                        Text(
                            text = "e.g. 5124, 3, A23126552012",
                            color = WhatsAppTextMuted,
                            fontSize = 12.5.sp
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = WhatsAppIncoming,
                        unfocusedContainerColor = WhatsAppIncoming,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = WhatsAppGreen,
                        unfocusedBorderColor = WhatsAppBorder
                    ),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    placeholder = {
                        Text(
                            text = "Friend's Name (Optional)",
                            color = WhatsAppTextMuted,
                            fontSize = 12.sp
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = WhatsAppIncoming,
                        unfocusedContainerColor = WhatsAppIncoming,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = WhatsAppGreen,
                        unfocusedBorderColor = WhatsAppBorder
                    ),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = WhatsAppTextMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val targetRoll = rollInput.trim()
                            if (targetRoll.isNotBlank()) {
                                onStartChat(selectedDept, targetRoll, nameInput.trim())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                        shape = RoundedCornerShape(10.dp),
                        enabled = rollInput.isNotBlank()
                    ) {
                        Text("Start Encrypted Chat", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun WhatsAppChatRoomView(
    conversation: ChatConversation,
    messages: List<MeshChatMessage>,
    onBack: () -> Unit,
    onSendMessage: (String) -> Unit,
    onSendPhoto: (Uri, String) -> Unit = { _, _ -> }
) {
    var textInput by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var fullScreenImageBase64 by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WhatsAppDarkBackground)
    ) {
        // WhatsApp Top Bar inside chat
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(WhatsAppHeader)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            // Avatar
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        if (conversation.isBroadcast) Color(0xFF1E3A8A) else Color(0xFF1E293B)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (conversation.isBroadcast) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(20.dp))
                } else {
                    val initials = conversation.rollNumber.take(2).uppercase()
                    Text(
                        text = initials.ifBlank { "ST" },
                        color = if (conversation.isOnlineInRange) WhatsAppGreen else RelayCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = conversation.title,
                        color = Color.White,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${conversation.rollNumber})",
                        color = WhatsAppTextMuted,
                        fontSize = 10.5.sp
                    )
                }
                Text(
                    text = if (conversation.isBroadcast) "📢 Public Mesh Channel • All nodes listening"
                    else if (conversation.isOnlineInRange) "🟢 In Direct Radio Range (~3m) • E2EE Encrypted"
                    else "🔀 Multi-Hop Mesh (${conversation.department}) • E2EE Encrypted",
                    color = if (conversation.isBroadcast || conversation.isOnlineInRange) WhatsAppGreen else RelayCyan,
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
                    isBroadcastRoom = conversation.isBroadcast,
                    onImageClick = { base64 -> fullScreenImageBase64 = base64 }
                )
            }
        }

        // Selected Photo Preview Banner
        if (selectedImageUri != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WhatsAppIncoming)
                    .border(1.dp, WhatsAppGreen.copy(alpha = 0.5f), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = WhatsAppGreen, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Photo Selected",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Will be compressed (~20KB) & encrypted for mesh",
                        color = WhatsAppGreen,
                        fontSize = 10.sp
                    )
                }
                IconButton(onClick = { selectedImageUri = null }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = WhatsAppTextMuted, modifier = Modifier.size(16.dp))
                }
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
            // Photo Attachment Button
            IconButton(
                onClick = { photoPickerLauncher.launch("image/*") },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.AddPhotoAlternate,
                    contentDescription = "Attach Photo",
                    tint = if (selectedImageUri != null) WhatsAppGreen else WhatsAppTextMuted,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = {
                    Text(
                        text = if (conversation.isBroadcast) "Broadcast to all mesh nodes..." else "Message ${conversation.rollNumber} (Hopped)...",
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
                    if (selectedImageUri != null) {
                        onSendPhoto(selectedImageUri!!, textInput.trim())
                        selectedImageUri = null
                        textInput = ""
                    } else if (textInput.isNotBlank()) {
                        onSendMessage(textInput.trim())
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

        // Full Screen Image Preview Dialog
        if (fullScreenImageBase64 != null) {
            Dialog(onDismissRequest = { fullScreenImageBase64 = null }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A), shape = RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val fullBmp = remember(fullScreenImageBase64) {
                            ImageCompressor.base64ToComposeImageBitmap(fullScreenImageBase64!!)
                        }
                        if (fullBmp != null) {
                            Image(
                                bitmap = fullBmp,
                                contentDescription = "Enlarged Mesh Photo",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 440.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { fullScreenImageBase64 = null },
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Close Preview", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// WHATSAPP CHAT BUBBLE COMPONENT WITH HOP TELEMETRY
// =========================================================================
@Composable
fun WhatsAppBubble(
    message: MeshChatMessage,
    isBroadcastRoom: Boolean,
    onImageClick: (String) -> Unit = {}
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

                // Mesh Photo Render
                if (!message.imageBase64.isNullOrBlank()) {
                    val composeBmp = remember(message.imageBase64) {
                        ImageCompressor.base64ToComposeImageBitmap(message.imageBase64)
                    }
                    if (composeBmp != null) {
                        Image(
                            bitmap = composeBmp,
                            contentDescription = "Mesh Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp, max = 220.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onImageClick(message.imageBase64) }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                // Message Text / Caption
                if (message.text.isNotBlank()) {
                    Text(
                        text = message.text,
                        color = Color.White,
                        fontSize = 12.5.sp,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Hop Telemetry for Incoming Messages
                if (!isOut && !isBroadcastRoom) {
                    val hopLabel = if (message.hopCount <= 1) {
                        "⚡ Direct (1 Hop)"
                    } else if (message.relayedBy.isNotBlank()) {
                        "🔀 Relayed (${message.hopCount} Hops via ${message.relayedBy})"
                    } else {
                        "🔀 Relayed (${message.hopCount} Hops)"
                    }

                    Text(
                        text = hopLabel,
                        color = RelayCyan,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

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

// =========================================================================
// CONVERSATION ROW ITEMS
// =========================================================================
@Composable
fun GeneralBroadcastRow(
    conversation: ChatConversation,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E3A8A)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Campaign, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(24.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Campus General Broadcast",
                    color = Color.White,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = conversation.lastMessageTimestamp,
                    color = WhatsAppGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = conversation.lastMessageText,
                color = WhatsAppTextMuted,
                fontSize = 12.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
fun WhatsAppConversationRow(
    conversation: ChatConversation,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E293B)),
            contentAlignment = Alignment.Center
        ) {
            val initials = conversation.rollNumber.take(2).uppercase()
            Text(
                text = initials.ifBlank { "ST" },
                color = if (conversation.isOnlineInRange) WhatsAppGreen else RelayCyan,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = conversation.title,
                        color = Color.White,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (conversation.title != conversation.rollNumber && conversation.rollNumber.isNotBlank() && !conversation.isBroadcast) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(${conversation.rollNumber})",
                            color = WhatsAppTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
                Text(
                    text = conversation.lastMessageTimestamp,
                    color = WhatsAppTextMuted,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (conversation.isOnlineInRange) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(WhatsAppGreen, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "In range • ${conversation.rssi} dBm",
                        color = WhatsAppGreen,
                        fontSize = 10.5.sp,
                        maxLines = 1
                    )
                } else {
                    Text(
                        text = "🔀 Multi-Hop • ${conversation.department}",
                        color = RelayCyan,
                        fontSize = 10.5.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateHint(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            color = WhatsAppTextMuted,
            fontSize = 10.5.sp
        )
    }
}
