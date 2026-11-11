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
    var loginPassword by remember { mutableStateOf("1234") }
    var loginPasswordVisible by remember { mutableStateOf(false) }

    // Sign Up Fields
    var fullName by remember { mutableStateOf("") }
    var rollNumber by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("CSD") }
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT_OCCUPANT) }
    var signUpPassword by remember { mutableStateOf("1234") }
    var confirmPassword by remember { mutableStateOf("1234") }
    var signUpPasswordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val departments = listOf("CSD", "CSE", "CSM", "CIC", "IT", "ECE", "EEE", "Mechanical", "Civil", "Chemical")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MeshBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Modern Rounded Banana Logo Badge
        Box(
            modifier = Modifier
                .size(76.dp)
                .background(MeshSurfaceElevated, RoundedCornerShape(22.dp))
                .border(1.5.dp, BananaGold.copy(alpha = 0.4f), RoundedCornerShape(22.dp))
                .shadow(8.dp, RoundedCornerShape(22.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🍌",
                fontSize = 40.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "ANITS MESH",
            color = BananaGold,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Autonomous Campus Disaster Grid",
            color = TextSecondary,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(22.dp))

        // Mode Switcher (Clean Pill Toggle)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MeshSurface, RoundedCornerShape(16.dp))
                .border(1.dp, MeshBorder, RoundedCornerShape(16.dp))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (authMode == AuthMode.SIGN_IN) MintGreen.copy(alpha = 0.2f) else Color.Transparent,
                        RoundedCornerShape(12.dp)
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
                    text = "Sign In",
                    color = if (authMode == AuthMode.SIGN_IN) MintGreen else TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (authMode == AuthMode.SIGN_UP) MintGreen.copy(alpha = 0.2f) else Color.Transparent,
                        RoundedCornerShape(12.dp)
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
                    text = "Create Account",
                    color = if (authMode == AuthMode.SIGN_UP) MintGreen else TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
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
                    loginPassword = "1234"
                },
                colors = ButtonDefaults.buttonColors(containerColor = MeshSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MintGreen.copy(alpha = 0.5f)),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                Text("⚡ User a (1)", fontSize = 11.5.sp, color = MintGreen, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    authMode = AuthMode.SIGN_IN
                    loginRollNumber = "2"
                    loginPassword = "1234"
                },
                colors = ButtonDefaults.buttonColors(containerColor = MeshSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f)),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                Text("⚡ User b (2)", fontSize = 11.5.sp, color = CyberCyan, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    authMode = AuthMode.SIGN_IN
                    loginRollNumber = "3"
                    loginPassword = "1234"
                },
                colors = ButtonDefaults.buttonColors(containerColor = MeshSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BananaGold.copy(alpha = 0.5f)),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                Text("⚡ User c (3)", fontSize = 11.5.sp, color = BananaGold, fontWeight = FontWeight.Bold)
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

        // Form Container (Smooth Rounded Card)
        Card(
            colors = CardDefaults.cardColors(containerColor = MeshSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MeshBorder),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                if (authMode == AuthMode.SIGN_IN) {
                    // SIGN IN FIELDS
                    Text("ROLL NUMBER / USER ID", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = loginRollNumber,
                        onValueChange = { loginRollNumber = it },
                        placeholder = { Text("e.g. 1, 2, or Roll No", color = TextTertiary, fontSize = 13.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = MintGreen,
                            unfocusedBorderColor = MeshBorder,
                            focusedContainerColor = MeshSurfaceElevated,
                            unfocusedContainerColor = MeshSurfaceElevated
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("PASSWORD", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = loginPassword,
                        onValueChange = { loginPassword = it },
                        placeholder = { Text("Password", color = TextTertiary, fontSize = 13.sp) },
                        visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { loginPasswordVisible = !loginPasswordVisible }) {
                                Icon(
                                    if (loginPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = TextSecondary
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = MintGreen,
                            unfocusedBorderColor = MeshBorder,
                            focusedContainerColor = MeshSurfaceElevated,
                            unfocusedContainerColor = MeshSurfaceElevated
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

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
                        colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Sign In to Mesh ➔", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                } else {
                    // SIGN UP FIELDS
                    Text("FULL NAME", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        placeholder = { Text("e.g. Alex", color = TextTertiary, fontSize = 13.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = MintGreen,
                            unfocusedBorderColor = MeshBorder,
                            focusedContainerColor = MeshSurfaceElevated,
                            unfocusedContainerColor = MeshSurfaceElevated
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("ROLL NUMBER / USER ID", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = rollNumber,
                        onValueChange = { rollNumber = it },
                        placeholder = { Text("e.g. 4 or 5", color = TextTertiary, fontSize = 13.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = MintGreen,
                            unfocusedBorderColor = MeshBorder,
                            focusedContainerColor = MeshSurfaceElevated,
                            unfocusedContainerColor = MeshSurfaceElevated
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("DEPARTMENT", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("CSD", "CSE", "IT", "ECE").forEach { dept ->
                            val isSel = (department == dept)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isSel) MintGreen.copy(alpha = 0.15f) else MeshSurfaceElevated, RoundedCornerShape(10.dp))
                                    .border(1.dp, if (isSel) MintGreen else MeshBorder, RoundedCornerShape(10.dp))
                                    .clickable { department = dept }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(dept, color = if (isSel) MintGreen else TextSecondary, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ROLE / CATEGORY SELECTOR (HOD, Warden, Principal, Student, etc.)
                    Text("CAMPUS ROLE", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.height(6.dp))
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
                                        if (isSel) (if (role == UserRole.HOD || role == UserRole.PRINCIPAL_DEAN) BananaGold.copy(alpha = 0.15f) else MintGreen.copy(alpha = 0.15f))
                                        else MeshSurfaceElevated,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSel) (if (role == UserRole.HOD || role == UserRole.PRINCIPAL_DEAN) BananaGold else MintGreen)
                                        else MeshBorder,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedRole = role }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSel) TextPrimary else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                                )
                                if (isSel) {
                                    Text(
                                        text = if (role == UserRole.HOD || role == UserRole.PRINCIPAL_DEAN) "AUTHORITY" else "SELECTED",
                                        color = if (role == UserRole.HOD || role == UserRole.PRINCIPAL_DEAN) BananaGold else MintGreen,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("PASSWORD", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = signUpPassword,
                        onValueChange = { signUpPassword = it },
                        placeholder = { Text("Set a password (e.g. 1234)", color = TextTertiary, fontSize = 13.sp) },
                        visualTransformation = if (signUpPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = MintGreen,
                            unfocusedBorderColor = MeshBorder,
                            focusedContainerColor = MeshSurfaceElevated,
                            unfocusedContainerColor = MeshSurfaceElevated
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

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
                        colors = ButtonDefaults.buttonColors(containerColor = MintGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Create Identity & Join ➔", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                        }
                    }
                }
            }
        }
    }
}
