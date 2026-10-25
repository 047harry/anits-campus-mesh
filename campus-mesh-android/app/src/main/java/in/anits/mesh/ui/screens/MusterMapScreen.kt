package `in`.anits.mesh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun MusterMapScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Text("OFFLINE MUSTER MAP", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
        Text("ANITS CAMPUS VECTOR NAVIGATION", color = MeshGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(14.dp))

        // Vector map container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
                .background(Color(0xFF0B132B), shape = RoundedCornerShape(16.dp))
                .border(2.dp, Color(0xFF1E293B), shape = RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            // CSE Block (Top Left)
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(width = 130.dp, height = 70.dp)
                    .background(Color(0xFF1F2937), shape = RoundedCornerShape(8.dp))
                    .border(2.dp, EmergencyRed, shape = RoundedCornerShape(8.dp))
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("CSE BLOCK", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
                    Text("YOU ARE HERE (FL 3)", color = EmergencyRed, fontWeight = FontWeight.Bold, fontSize = 8.5.sp)
                }
            }

            // ECE Block (Top Right)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(width = 130.dp, height = 70.dp)
                    .background(Color(0xFF1F2937), shape = RoundedCornerShape(8.dp))
                    .border(1.dp, SurfaceBorder, shape = RoundedCornerShape(8.dp))
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ECE BLOCK", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Text("Cleared ✓", color = MeshGreen, fontSize = 8.5.sp)
                }
            }

            // Central Cricket Ground (Center Safe Zone)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(horizontal = 8.dp)
                    .background(Color(0xFF064E3B).copy(alpha = 0.7f), shape = RoundedCornerShape(12.dp))
                    .border(2.dp, MeshGreen, shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("★", color = MeshGreen, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Text("CENTRAL CRICKET GROUND", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    Text("PRIMARY SAFE MUSTER ZONE", color = Color(0xFFA7F3D0), fontWeight = FontWeight.Bold, fontSize = 9.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("TRIAGE TENT 1 ACTIVE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 8.5.sp)
                }
            }

            // Mech Block (Bottom Left)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .size(width = 130.dp, height = 70.dp)
                    .background(Color(0xFF1F2937), shape = RoundedCornerShape(8.dp))
                    .border(1.dp, SurfaceBorder, shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("MECH BLOCK", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }

            // Admin Block (Bottom Right)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(width = 130.dp, height = 70.dp)
                    .background(Color(0xFF1F2937), shape = RoundedCornerShape(8.dp))
                    .border(1.dp, SurfaceBorder, shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("ADMIN BLOCK", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Evacuation instructions
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MeshGreen.copy(alpha = 0.08f), shape = RoundedCornerShape(12.dp))
                .border(1.dp, MeshGreen, shape = RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column {
                Text("RECOMMENDED EVACUATION PATHWAY:", color = MeshGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "Take North Fire Stairwell down to Ground Floor. Exit through courtyard and proceed 90m East into Cricket Ground.",
                    color = Color(0xFFE5E7EB),
                    fontSize = 11.sp
                )
            }
        }
    }
}
