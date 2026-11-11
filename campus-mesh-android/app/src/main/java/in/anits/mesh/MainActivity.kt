package `in`.anits.mesh

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import `in`.anits.mesh.data.UserManager
import `in`.anits.mesh.data.UserProfile
import `in`.anits.mesh.model.UserRole
import `in`.anits.mesh.radio.MeshRadioService
import `in`.anits.mesh.radio.RadioCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import `in`.anits.mesh.ui.components.MeshStatusBar
import `in`.anits.mesh.ui.components.UnifiedMeshHeader
import `in`.anits.mesh.ui.navigation.MeshBottomNavigation
import `in`.anits.mesh.ui.navigation.Screen
import `in`.anits.mesh.ui.screens.*
import `in`.anits.mesh.ui.theme.*
import `in`.anits.mesh.viewmodel.*

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private val sosViewModel: SosViewModel by viewModels()
    private val bulletinViewModel: BulletinViewModel by viewModels()
    private val chatViewModel: ChatViewModel by viewModels()

    private val bluetoothEnableLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            RadioCoordinator.instance?.startMeshServices()
        }
    }

    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                when (state) {
                    BluetoothAdapter.STATE_ON -> {
                        RadioCoordinator.instance?.startMeshServices()
                    }
                    BluetoothAdapter.STATE_TURNING_OFF, BluetoothAdapter.STATE_OFF -> {
                        RadioCoordinator.instance?.stopMeshServices()
                    }
                }
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        checkAndPromptBluetooth()
        MeshRadioService.startService(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerReceiver(bluetoothStateReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
        checkAndRequestPermissions()

        setContent {
            CampusMeshTheme {
                var isRegistered by remember { mutableStateOf(UserManager.isRegistered(this)) }

                if (!isRegistered) {
                    RegistrationScreen(
                        onRegistrationComplete = {
                            isRegistered = true
                            MeshRadioService.startService(this)
                        }
                    )
                } else {
                    MainScreen(
                        mainViewModel = mainViewModel,
                        sosViewModel = sosViewModel,
                        bulletinViewModel = bulletinViewModel,
                        chatViewModel = chatViewModel,
                        onSwitchUser = {
                            MeshRadioService.stopService(this)
                            RadioCoordinator.instance?.stopMeshServices()
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val app = application as CampusMeshApp
                                    app.database.peerDao().clearAllPeers()
                                } catch (e: Exception) {}
                            }
                            UserManager.logout(this)
                            isRegistered = false
                        }
                    )
                }
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
            checkAndPromptBluetooth()
            MeshRadioService.startService(this)
        }
    }

    private fun checkAndPromptBluetooth() {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val adapter = bluetoothManager?.adapter
        if (adapter != null && !adapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            bluetoothEnableLauncher.launch(enableBtIntent)
        }
    }

    override fun onResume() {
        super.onResume()
        checkAndPromptBluetooth()
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(bluetoothStateReceiver)
        } catch (e: Exception) {}
        super.onDestroy()
    }
}

@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    sosViewModel: SosViewModel,
    bulletinViewModel: BulletinViewModel,
    chatViewModel: ChatViewModel,
    onSwitchUser: () -> Unit
) {
    val context = LocalContext.current
    var currentRoute by remember { mutableStateOf(Screen.Sos.route) }
    val peerCount by mainViewModel.peerCount.collectAsState()
    val isBlackout by mainViewModel.isBlackoutActive.collectAsState()
    val battery by mainViewModel.batteryLevel.collectAsState()
    val userProfile by UserManager.currentUser.collectAsState()

    val screenTitle = when (currentRoute) {
        Screen.Sos.route -> "CAMPUS SOS"
        Screen.Bulletins.route -> "CAMPUS RADIO"
        Screen.Chat.route -> "MESH CHAT"
        Screen.Map.route -> "MUSTER MAP"
        Screen.Profile.route -> "SETTINGS & IDENTITY"
        else -> "CAMPUS MESH"
    }

    Scaffold(
        topBar = {
            UnifiedMeshHeader(
                title = screenTitle,
                peerCount = peerCount,
                userProfile = userProfile,
                onSwitchUser = onSwitchUser
            )
        },
        bottomBar = {
            MeshBottomNavigation(
                currentRoute = currentRoute,
                onNavigate = { route -> currentRoute = route }
            )
        },
        containerColor = MeshBackground
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentRoute) {
                Screen.Sos.route -> SosScreen(viewModel = sosViewModel, mainViewModel = mainViewModel)
                Screen.Bulletins.route -> BulletinsScreen(viewModel = bulletinViewModel)
                Screen.Chat.route -> ChatScreen(viewModel = chatViewModel, onSwitchUser = onSwitchUser)
                Screen.Map.route -> MusterMapScreen()
                Screen.Profile.route -> ProfileSettingsScreen(onLogout = onSwitchUser)
            }
        }
    }
}
