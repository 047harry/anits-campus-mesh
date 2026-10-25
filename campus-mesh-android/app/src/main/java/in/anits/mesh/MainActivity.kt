package `in`.anits.mesh

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import `in`.anits.mesh.radio.MeshRadioService
import `in`.anits.mesh.ui.components.MeshStatusBar
import `in`.anits.mesh.ui.navigation.MeshBottomNavigation
import `in`.anits.mesh.ui.navigation.Screen
import `in`.anits.mesh.ui.screens.*
import `in`.anits.mesh.ui.theme.CampusMeshTheme
import `in`.anits.mesh.viewmodel.*

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private val sosViewModel: SosViewModel by viewModels()
    private val bulletinViewModel: BulletinViewModel by viewModels()
    private val chatViewModel: ChatViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        MeshRadioService.startService(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestPermissions()

        setContent {
            CampusMeshTheme {
                MainScreen(
                    mainViewModel = mainViewModel,
                    sosViewModel = sosViewModel,
                    bulletinViewModel = bulletinViewModel,
                    chatViewModel = chatViewModel
                )
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)

        val needed = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (needed.isNotEmpty()) {
            requestPermissionLauncher.launch(needed.toTypedArray())
        } else {
            MeshRadioService.startService(this)
        }
    }
}

@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    sosViewModel: SosViewModel,
    bulletinViewModel: BulletinViewModel,
    chatViewModel: ChatViewModel
) {
    var currentRoute by remember { mutableStateOf(Screen.Sos.route) }
    val peerCount by mainViewModel.peerCount.collectAsState()
    val isBlackout by mainViewModel.isBlackoutActive.collectAsState()
    val battery by mainViewModel.batteryLevel.collectAsState()

    Scaffold(
        topBar = {
            Column {
                MeshStatusBar(
                    peerCount = peerCount,
                    batteryPercent = battery,
                    isBlackoutActive = isBlackout
                )
                // Blackout alert banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF7F1D1D))
                        .padding(vertical = 4.dp, horizontal = 16.dp)
                ) {
                    Text(
                        text = "● OFFLINE MESH ACTIVE • ZERO INTERNET REQUIRED",
                        color = Color.White,
                        fontSize = 10.5.sp,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        },
        bottomBar = {
            MeshBottomNavigation(
                currentRoute = currentRoute,
                onNavigate = { route -> currentRoute = route }
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentRoute) {
                Screen.Sos.route -> SosScreen(viewModel = sosViewModel, mainViewModel = mainViewModel)
                Screen.Bulletins.route -> BulletinsScreen(viewModel = bulletinViewModel)
                Screen.Chat.route -> ChatScreen(viewModel = chatViewModel)
                Screen.Map.route -> MusterMapScreen()
            }
        }
    }
}
