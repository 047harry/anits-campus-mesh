package `in`.anits.mesh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.anits.mesh.ui.theme.*
import `in`.anits.mesh.viewmodel.ChatMessage
import `in`.anits.mesh.viewmodel.ChatViewModel

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.messages.collectAsState()
    var inputText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        // Peer Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark, shape = RoundedCornerShape(12.dp))
                .border(1.dp, SurfaceBorder, shape = RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF1E293B), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("RS", color = DirectBlue, fontWeight = FontWeight.Black, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text("Rahul Sharma (A23126551124)", color = TextPrimary, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                Text("🔒 E2EE Encrypted • 1 Hop Distance", color = DirectBlue, fontSize = 10.5.sp, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Message List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(msg)
            }
        }

        // Input bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark, shape = RoundedCornerShape(24.dp))
                .border(1.dp, SurfaceBorder, shape = RoundedCornerShape(24.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Type offline mesh message...", color = Color(0xFF6B7280), fontSize = 12.sp) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    }
                },
                modifier = Modifier
                    .size(36.dp)
                    .background(DirectBlue, shape = CircleShape)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun ChatBubble(msg: ChatMessage) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (msg.isOutgoing) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    if (msg.isOutgoing) Color(0xFF1D4ED8) else Color(0xFF1E293B),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(10.dp)
        ) {
            Column {
                if (!msg.isOutgoing) {
                    Text(msg.senderName, color = Color(0xFF93C5FD), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                }
                Text(msg.text, color = Color.White, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${msg.timestamp} • ${msg.deliveryStatus}",
                    color = if (msg.deliveryStatus == "DELIVERED") Color(0xFF86EFAC) else Color.White.copy(alpha = 0.6f),
                    fontSize = 9.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
