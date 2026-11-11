package `in`.anits.mesh.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
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

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val departments = listOf("CSD", "CSE", "CSM", "CIC", "IT", "ECE", "EEE", "Mechanical", "Civil", "Chemical")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MineStoneDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Minecraft Blocky Banana Logo Badge
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(MineDirtDark, RoundedCornerShape(12.dp))
                .border(3.dp, MineBananaYellow, RoundedCornerShape(12.dp))
                .shadow(6.dp, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🍌",
                fontSize = 38.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Pixel Title Header
        Text(
            text = "ANITS MESH",
            color = MineBananaYellow,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp
        )
        Text(
            text = "DECENTRALIZED OFFLINE NETWORK",
            color = MineDiamondBlue,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Mode Switcher (Minecraft Inventory Tab Style)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MineDirtDark, RoundedCornerShape(8.dp))
                .border(2.dp, MineStoneBorder, RoundedCornerShape(8.dp))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (authMode == AuthMode.SIGN_IN) MineGrassGreen else Color.Transparent,
                        RoundedCornerShape(6.dp)
                    )
                    .clickable {
                        authMode = AuthMode.SIGN_IN
                        errorMessage = null
                        successMessage = null
                    }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🔑 SIGN IN",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (authMode == AuthMode.SIGN_UP) MineGrassGreen else Color.Transparent,
                        RoundedCornerShape(6.dp)
                    )
                    .clickable {
                        authMode = AuthMode.SIGN_UP
                        errorMessage = null
                        successMessage = null
                    }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⛏️ SIGN UP",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Preset Selector for Easy Testing
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    authMode = AuthMode.SIGN_IN
                    loginRollNumber = "1"
                    loginPassword = "anits"
                },
                colors = ButtonDefaults.buttonColors(containerColor = MineDirtDark),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, MineEmerald),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Text("⚡ User a (1)", fontSize = 11.sp, color = MineEmerald, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    authMode = AuthMode.SIGN_IN
                    loginRollNumber = "2"
                    loginPassword = "anits"
                },
                colors = ButtonDefaults.buttonColors(containerColor = MineDirtDark),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, MineDiamondBlue),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Text("⚡ User b (2)", fontSize = 11.sp, color = MineDiamondBlue, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    authMode = AuthMode.SIGN_IN
                    loginRollNumber = "3"
                    loginPassword = "anits"
                },
                colors = ButtonDefaults.buttonColors(containerColor = MineDirtDark),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, MineGold),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Text("⚡ User c (3)", fontSize = 11.sp, color = MineGold, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Error & Success Feedback Boxes
        errorMessage?.let { err ->
            Surface(
                color = MineRedstone.copy(alpha = 0.2f),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, MineRedstone),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Text(
                    text = "⚠️ $err",
                    color = MineRedstone,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        successMessage?.let { msg ->
            Surface(
                color = MineEmerald.copy(alpha = 0.2f),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, MineEmerald),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Text(
                    text = "✅ $msg",
                    color = MineEmerald,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        // Form Container (Minecraft Crafting GUI Style)
        Card(
            colors = CardDefaults.cardColors(containerColor = MineDirtDark),
            border = androidx.compose.foundation.BorderStroke(2.dp, MineStoneBorder),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (authMode == AuthMode.SIGN_IN) {
                    // SIGN IN FIELDS
                    Text("ROLL NUMBER / USER ID", color = MineDiamondBlue, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = loginRollNumber,
                        onValueChange = { loginRollNumber = it },
                        placeholder = { Text("e.g. 1, 2, or Roll No", color = TextMuted, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MineBananaYellow,
                            unfocusedBorderColor = MineStoneBorder,
                            focusedContainerColor = MineStoneDark,
                            unfocusedContainerColor = MineStoneDark
                        ),
                        shape = RoundedCornerShape(6.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("PASSWORD", color = MineDiamondBlue, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = loginPassword,
                        onValueChange = { loginPassword = it },
                        placeholder = { Text("Password", color = TextMuted, fontSize = 12.sp) },
                        visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { loginPasswordVisible = !loginPasswordVisible }) {
                                Icon(
                                    if (loginPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = TextMuted
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MineBananaYellow,
                            unfocusedBorderColor = MineStoneBorder,
                            focusedContainerColor = MineStoneDark,
                            unfocusedContainerColor = MineStoneDark
                        ),
                        shape = RoundedCornerShape(6.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            val cleanRoll = loginRollNumber.trim()
                            val cleanPass = loginPassword.trim()
                            if (cleanRoll.isBlank() || cleanPass.isBlank()) {
                                errorMessage = "Please enter roll number and password"
                                return@Button
                            }
                            isLoading = true
                            errorMessage = null
                            coroutineScope.launch {
                                val result = UserManager.loginUser(context, cleanRoll, cleanPass)
                                isLoading = false
                                if (result.isSuccess) {
                                    onRegistrationComplete()
                                } else {
                                    errorMessage = result.exceptionOrNull()?.message ?: "Login failed"
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MineGrassGreen),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(2.dp, MineEmerald),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("ENTER THE MESH ➔", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
                        }
                    }
                } else {
                    // SIGN UP FIELDS
                    Text("FULL NAME", color = MineDiamondBlue, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        placeholder = { Text("e.g. Alex", color = TextMuted, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MineBananaYellow,
                            unfocusedBorderColor = MineStoneBorder,
                            focusedContainerColor = MineStoneDark,
                            unfocusedContainerColor = MineStoneDark
                        ),
                        shape = RoundedCornerShape(6.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("ROLL NUMBER / USER ID", color = MineDiamondBlue, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = rollNumber,
                        onValueChange = { rollNumber = it },
                        placeholder = { Text("e.g. 4 or 5", color = TextMuted, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MineBananaYellow,
                            unfocusedBorderColor = MineStoneBorder,
                            focusedContainerColor = MineStoneDark,
                            unfocusedContainerColor = MineStoneDark
                        ),
                        shape = RoundedCornerShape(6.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("DEPARTMENT", color = MineDiamondBlue, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("CSD", "CSE", "IT", "ECE").forEach { dept ->
                            val isSel = (department == dept)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isSel) MineEmerald else MineStoneDark, RoundedCornerShape(6.dp))
                                    .border(1.5.dp, if (isSel) MineBananaYellow else MineStoneBorder, RoundedCornerShape(6.dp))
                                    .clickable { department = dept }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(dept, color = if (isSel) Color.Black else Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // ROLE / CATEGORY SELECTOR (HOD, Warden, Principal, Student, etc.)
                    Text("ROLE / CAMPUS DESIGNATION", color = MineDiamondBlue, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(
                            UserRole.STUDENT_OCCUPANT to "🎓 Student / Occupant",
                            UserRole.HOD to "⭐ Head of Department (HOD)",
                            UserRole.FACULTY_WARDEN to "🛡️ Faculty / Hostel Warden",
                            UserRole.CLINIC_SECURITY to "🚨 Health Clinic / Security",
                            UserRole.PRINCIPAL_DEAN to "🏛️ Principal / Dean"
                        ).forEach { (role, label) ->
                            val isSel = (selectedRole == role)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isSel) (if (role == UserRole.HOD || role == UserRole.PRINCIPAL_DEAN) MineGold.copy(alpha = 0.2f) else MineEmerald.copy(alpha = 0.2f))
                                        else MineStoneDark,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .border(
                                        1.5.dp,
                                        if (isSel) (if (role == UserRole.HOD || role == UserRole.PRINCIPAL_DEAN) MineBananaYellow else MineEmerald)
                                        else MineStoneBorder,
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable { selectedRole = role }
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSel) Color.White else TextMuted,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSel) FontWeight.Black else FontWeight.Medium
                                )
                                if (isSel) {
                                    Text(
                                        text = if (role == UserRole.HOD || role == UserRole.PRINCIPAL_DEAN) "AUTHORITY" else "SELECTED",
                                        color = if (role == UserRole.HOD || role == UserRole.PRINCIPAL_DEAN) MineBananaYellow else MineEmerald,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("PASSWORD", color = MineDiamondBlue, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = signUpPassword,
                        onValueChange = { signUpPassword = it },
                        placeholder = { Text("Set a password", color = TextMuted, fontSize = 12.sp) },
                        visualTransformation = if (signUpPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = MineBananaYellow,
                            unfocusedBorderColor = MineStoneBorder,
                            focusedContainerColor = MineStoneDark,
                            unfocusedContainerColor = MineStoneDark
                        ),
                        shape = RoundedCornerShape(6.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val cleanName = fullName.trim()
                            val cleanRoll = rollNumber.trim()
                            val cleanPass = signUpPassword.trim()
                            if (cleanName.isBlank() || cleanRoll.isBlank() || cleanPass.isBlank()) {
                                errorMessage = "Please fill in all fields"
                                return@Button
                            }
                            isLoading = true
                            errorMessage = null
                            coroutineScope.launch {
                                val result = UserManager.signUpUser(
                                    context = context,
                                    fullName = cleanName,
                                    rollNumber = cleanRoll,
                                    department = department,
                                    role = selectedRole,
                                    password = cleanPass
                                )
                                isLoading = false
                                if (result.isSuccess) {
                                    onRegistrationComplete()
                                } else {
                                    errorMessage = result.exceptionOrNull()?.message ?: "Sign up failed"
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MineGrassGreen),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(2.dp, MineEmerald),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("CREATE IDENTITY & JOIN ➔", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
