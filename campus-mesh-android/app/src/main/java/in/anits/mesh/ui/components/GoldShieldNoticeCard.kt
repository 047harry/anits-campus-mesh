package `in`.anits.mesh.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.anits.mesh.ui.theme.*

@Composable
fun GoldShieldNoticeCard(
    senderName: String,
    senderRole: String,
    timestampText: String,
    hopCount: Int,
    content: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = SurfaceDark, shape = RoundedCornerShape(12.dp))
            .border(width = 1.5.dp, color = AuthorityGold, shape = RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "Gold Shield Authority",
                tint = AuthorityGold,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = senderName,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$senderRole • $timestampText",
                    color = AuthorityGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Box(
                modifier = Modifier
                    .background(color = Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$hopCount Hops",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = content,
            color = TextPrimary,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(color = AuthorityGoldDark.copy(alpha = 0.25f), shape = RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Verified",
                tint = MeshGreen,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Cryptographically Verified Offline via Root PKI Ring",
                color = TextMuted,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
