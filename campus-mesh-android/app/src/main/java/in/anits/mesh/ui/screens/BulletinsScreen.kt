package `in`.anits.mesh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.anits.mesh.ui.components.GoldShieldNoticeCard
import `in`.anits.mesh.ui.theme.AuthorityGold
import `in`.anits.mesh.viewmodel.BulletinViewModel

@Composable
fun BulletinsScreen(
    viewModel: BulletinViewModel,
    modifier: Modifier = Modifier
) {
    val bulletins by viewModel.bulletins.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Text("OFFICIAL BULLETINS", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
        Text("CRYPTOGRAPHICALLY VERIFIED OFFLINE", color = AuthorityGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)

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
