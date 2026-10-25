package `in`.anits.mesh.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.anits.mesh.ui.theme.*

@Composable
fun EmergencySosButton(
    isTriggered: Boolean,
    onSosTriggered: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SosPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isTriggered) 1.15f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isTriggered) 600 else 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(220.dp)
            .scale(pulseScale)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = if (isTriggered) EmergencyRed.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f),
                    shape = CircleShape
                )
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(170.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isTriggered) listOf(MeshGreen, Color(0xFF047857))
                        else listOf(EmergencyRed, EmergencyRedDark)
                    ),
                    shape = CircleShape
                )
                .border(width = 4.dp, color = Color.White.copy(alpha = 0.8f), shape = CircleShape)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { onSosTriggered() },
                        onTap = { onSosTriggered() }
                    )
                }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isTriggered) "HELP\nEN ROUTE" else "EMERGENCY\nSOS",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isTriggered) "ACK RECEIVED" else "TAP OR HOLD",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
