package `in`.anits.mesh.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.anits.mesh.data.UserManager
import `in`.anits.mesh.model.UserRole
import `in`.anits.mesh.ui.theme.*
import kotlinx.coroutines.launch

enum class AuthMode {
    SIGN_IN,
    SIGN_UP
}

@Composable
fun RegistrationScreen(
    onRegistrationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var authMode by remember { mutableStateOf(AuthMode.SIGN_IN) }

    // Sign In Fields
    var loginRollNumber by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginPasswordVisible by remember { mutableStateOf(false) }

    // Sign Up Fields
    var fullName by remember { mutableStateOf("") }
    var rollNumber by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("CSD") }
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT_OCCUPANT) }
    var signUpPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var signUpPasswordVisible by remember { mutableStateOf(false) }
    var securityPin by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val departments = listOf("CSD", "CSE", "CSM", "CIC", "IT", "ECE", "EEE", "Mechanical", "Civil", "Chemical")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Top Badge
        Box(
            modifier = Modifier
                .background(MeshGreen.copy(alpha = 0.15f), shape = RoundedCornerShape(999.dp))
                .border(1.dp, MeshGreen, shape = RoundedCornerShape(999.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = "ANITS CAMPUS MESH • PEACETIME AUTH",
                color = MeshGreen,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = if (authMode == AuthMode.SIGN_IN) "Campus Mesh Login" else "New Member Registration",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = if (authMode == AuthMode.SIGN_IN)
                "Sign in with your registered Roll Number & Password."
            else "Register your identity to sync public keys before disasters.",
            color = TextMuted,
            fontSize = 11.5.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Mode Switcher Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark, shape = RoundedCornerShape(12.dp))
                .border(1.dp, SurfaceBorder, shape = RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            TabButton(
                title = "🔑 Sign In",
                isSelected = (authMode == AuthMode.SIGN_IN),
                onClick = {
                    authMode = AuthMode.SIGN_IN
                    errorMessage = null
                    successMessage = null
                },
                modifier = Modifier.weight(1f)
            )
            TabButton(
                title = "📝 Sign Up",
                isSelected = (authMode == AuthMode.SIGN_UP),
                onClick = {
                    authMode = AuthMode.SIGN_UP
                    errorMessage = null
                    successMessage = null
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Preset Buttons for Instant Multi-Device Testing / Evaluation Viva
        Text(
            text = "FAST TEST PRESETS (ONE-TAP DEMO):",
            color = Color(0xFF64748B),
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    authMode = AuthMode.SIGN_IN
                    loginRollNumber = "A23126551000"
                    loginPassword = "anits123"
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 6.dp)
            ) {
                Text("⚡ Student", fontSize = 11.sp, color = MeshGreen, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    authMode = AuthMode.SIGN_IN
                    loginRollNumber = "FAC-CSD-01"
                    loginPassword = "anits123"
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 6.dp)
            ) {
                Text("🏛️ HOD (CSD)", fontSize = 11.sp, color = AuthorityGold, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    authMode = AuthMode.SIGN_IN
                    loginRollNumber = "FAC-ADMIN-01"
                    loginPassword = "anits123"
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 6.dp)
            ) {
                Text("🎓 Principal", fontSize = 11.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Error / Success Banners
        errorMessage?.let { msg ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EmergencyRed.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
                    .border(1.dp, EmergencyRed, shape = RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(text = "⚠️ $msg", color = Color(0xFFFCA5A5), fontSize = 11.5.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        successMessage?.let { msg ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MeshGreen.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
                    .border(1.dp, MeshGreen, shape = RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(text = "✓ $msg", color = Color(0xFF86EFAC), fontSize = 11.5.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // ======================= SIGN IN VIEW =======================
        if (authMode == AuthMode.SIGN_IN) {
            OutlinedTextField(
                value = loginRollNumber,
                onValueChange = { loginRollNumber = it },
                label = { Text("Roll Number / Faculty ID", color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = DirectBlue) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = DirectBlue,
                    unfocusedBorderColor = SurfaceBorder
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = loginPassword,
                onValueChange = { loginPassword = it },
                label = { Text("Password", color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = DirectBlue) },
                trailingIcon = {
                    IconButton(onClick = { loginPasswordVisible = !loginPasswordVisible }) {
                        Icon(
                            if (loginPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = TextMuted
                        )
                    }
                },
                visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = DirectBlue,
                    unfocusedBorderColor = SurfaceBorder
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (loginRollNumber.isBlank() || loginPassword.isBlank()) {
                        errorMessage = "Please enter both Roll Number and Password."
                        return@Button
                    }
                    isLoading = true
                    errorMessage = null
                    coroutineScope.launch {
                        val result = UserManager.loginUser(context, loginRollNumber, loginPassword)
                        isLoading = false
                        if (result.isSuccess) {
                            successMessage = "Authentication successful! Entering mesh..."
                            onRegistrationComplete()
                        } else {
                            errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Login failed."
                        }
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = DirectBlue),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text("SIGN IN TO CAMPUS MESH", fontWeight = FontWeight.Black, fontSize = 13.sp)
                }
            }
        }

        // ======================= SIGN UP VIEW =======================
        if (authMode == AuthMode.SIGN_UP) {
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name (e.g. Dr. K. Venkata Lakshmi)", color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MeshGreen) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = MeshGreen,
                    unfocusedBorderColor = SurfaceBorder
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = rollNumber,
                onValueChange = { rollNumber = it },
                label = { Text("Roll Number / Faculty ID (e.g. A23126551000)", color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = AuthorityGold) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = AuthorityGold,
                    unfocusedBorderColor = SurfaceBorder
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Department Chips
            Text(
                text = "DEPARTMENT / BRANCH:",
                color = TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                departments.chunked(4).forEach { rowDepts ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        rowDepts.forEach { dept ->
                            DeptChip(
                                title = dept,
                                isSelected = (department == dept),
                                onClick = { department = dept },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Campus Roles (All 6 Roles from Architecture Blueprint)
            Text(
                text = "SELECT CAMPUS STAKEHOLDER ROLE (BLUEPRINT HIERARCHY):",
                color = TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                RoleSelectionCard(
                    tier = "TIER 3",
                    title = "Student / Occupant",
                    description = "Standard emergency node. Transmits SOS, peer chat, and relays packets.",
                    isSelected = (selectedRole == UserRole.STUDENT_OCCUPANT),
                    badgeColor = MeshGreen,
                    onClick = { selectedRole = UserRole.STUDENT_OCCUPANT }
                )

                RoleSelectionCard(
                    tier = "TIER 2",
                    title = "Campus Health Center / Security",
                    description = "Emergency medical triage & rescue response. Receives high-priority SOS alerts.",
                    isSelected = (selectedRole == UserRole.CLINIC_SECURITY),
                    badgeColor = Color(0xFFEF4444),
                    onClick = { selectedRole = UserRole.CLINIC_SECURITY }
                )

                RoleSelectionCard(
                    tier = "TIER 1",
                    title = "Faculty / Hostel Warden",
                    description = "Staff authority node. Coordinates floor evacuations and safe muster.",
                    isSelected = (selectedRole == UserRole.FACULTY_WARDEN),
                    badgeColor = Color(0xFF38BDF8),
                    onClick = { selectedRole = UserRole.FACULTY_WARDEN }
                )

                RoleSelectionCard(
                    tier = "TIER 1",
                    title = "Head of Department (HOD)",
                    description = "Department executive authority. Can issue signed evacuation directives.",
                    isSelected = (selectedRole == UserRole.HOD),
                    badgeColor = AuthorityGold,
                    onClick = { selectedRole = UserRole.HOD }
                )

                RoleSelectionCard(
                    tier = "TIER 1",
                    title = "Principal / Dean",
                    description = "Highest campus executive authority. Broadcasts campus-wide lockdown/evac orders.",
                    isSelected = (selectedRole == UserRole.PRINCIPAL_DEAN),
                    badgeColor = Color(0xFFF59E0B),
                    onClick = { selectedRole = UserRole.PRINCIPAL_DEAN }
                )

                RoleSelectionCard(
                    tier = "TIER 0",
                    title = "Campus Root Administrator",
                    description = "Root authority. Manages cryptographic key directory and network config.",
                    isSelected = (selectedRole == UserRole.SYSTEM_ADMIN),
                    badgeColor = Color(0xFFA855F7),
                    onClick = { selectedRole = UserRole.SYSTEM_ADMIN }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Password Field
            OutlinedTextField(
                value = signUpPassword,
                onValueChange = { signUpPassword = it },
                label = { Text("Create Account Password (min 4 chars)", color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MeshGreen) },
                trailingIcon = {
                    IconButton(onClick = { signUpPasswordVisible = !signUpPasswordVisible }) {
                        Icon(
                            if (signUpPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = TextMuted
                        )
                    }
                },
                visualTransformation = if (signUpPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = MeshGreen,
                    unfocusedBorderColor = SurfaceBorder
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password", color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Check, contentDescription = null, tint = MeshGreen) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = MeshGreen,
                    unfocusedBorderColor = SurfaceBorder
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Security PIN field for privileged roles
            if (selectedRole != UserRole.STUDENT_OCCUPANT) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = securityPin,
                    onValueChange = { securityPin = it },
                    label = { Text("Institutional Verification PIN (Default: 1234)", color = AuthorityGold) },
                    leadingIcon = { Icon(Icons.Default.Security, contentDescription = null, tint = AuthorityGold) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AuthorityGold,
                        unfocusedBorderColor = SurfaceBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    if (fullName.isBlank() || rollNumber.isBlank() || signUpPassword.isBlank()) {
                        errorMessage = "Please fill in all required fields."
                        return@Button
                    }
                    if (signUpPassword.length < 4) {
                        errorMessage = "Password must be at least 4 characters."
                        return@Button
                    }
                    if (signUpPassword != confirmPassword) {
                        errorMessage = "Passwords do not match."
                        return@Button
                    }
                    if (selectedRole != UserRole.STUDENT_OCCUPANT && securityPin != "1234") {
                        errorMessage = "Invalid verification PIN for ${selectedRole.title}. Use PIN 1234."
                        return@Button
                    }

                    isLoading = true
                    errorMessage = null
                    coroutineScope.launch {
                        val result = UserManager.signUpUser(
                            context = context,
                            fullName = fullName,
                            rollNumber = rollNumber,
                            department = department,
                            role = selectedRole,
                            password = signUpPassword
                        )
                        isLoading = false
                        if (result.isSuccess) {
                            successMessage = "Account registered & synced to Supabase Cloud! Entering mesh..."
                            onRegistrationComplete()
                        } else {
                            errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Registration failed."
                        }
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = MeshGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp))
                } else {
                    Text("REGISTER & SYNC IDENTITY TO CLOUD", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.5.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun TabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                if (isSelected) Color(0xFF2563EB) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) Color.White else TextMuted,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DeptChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                if (isSelected) DirectBlue else SurfaceDark,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                1.dp,
                if (isSelected) DirectBlue else SurfaceBorder,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) Color.White else TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun RoleSelectionCard(
    tier: String,
    title: String,
    description: String,
    isSelected: Boolean,
    badgeColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) badgeColor.copy(alpha = 0.12f) else SurfaceDark,
                shape = RoundedCornerShape(10.dp)
            )
            .border(
                if (isSelected) 1.5.dp else 1.dp,
                if (isSelected) badgeColor else SurfaceBorder,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(if (isSelected) badgeColor else Color.Transparent, shape = CircleShape)
                .border(2.dp, if (isSelected) badgeColor else TextMuted, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(badgeColor.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(tier, color = badgeColor, fontSize = 8.5.sp, fontWeight = FontWeight.Black)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    color = if (isSelected) Color.White else Color(0xFFE2E8F0),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                color = TextMuted,
                fontSize = 10.sp,
                lineHeight = 13.sp
            )
        }
    }
}
