package com.qppd.pesapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

import com.qppd.pesapp.auth.AuthManager
import com.qppd.pesapp.ui.theme.PESAppTheme

class MainActivity : ComponentActivity() {
    private val authManager = AuthManager.getInstance()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Check if user is already logged in
        if (authManager.isLoggedIn()) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
            return
        }
        
        setContent {
            PESAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LoginScreen(
                        onLoginSuccess = {
                            startActivity(Intent(this@MainActivity, DashboardActivity::class.java))
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    // Pre-filled credentials for debugging
    var email by remember { mutableStateOf("admin@pesapp.com") }
    var password by remember { mutableStateOf("Admin1234") }
    var passwordVisible by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var showForgotPassword by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var showAdminSuccess by remember { mutableStateOf(false) }
    var showDebugInfo by remember { mutableStateOf(true) }
    var selectedRole by remember { mutableStateOf("ADMIN") }
    
    val context = LocalContext.current
    val authManager = AuthManager.getInstance()
    val userManager = com.qppd.pesapp.auth.UserManager.getInstance()
    
    // Main layout container
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // 🔹 Background Image
        Image(
            painter = painterResource(id = R.drawable.appbg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 🔹 Foreground content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App logo
            Image(
                painter = painterResource(id = R.drawable.applogo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(128.dp))

            // Card container with opacity and rounded corners
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Email input
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password input with toggle
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            val description = if (passwordVisible) "Hide password" else "Show password"
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = icon, contentDescription = description)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Forgot Password link
                    TextButton(
                        onClick = { showForgotPassword = true },
                        enabled = !isLoading
                    ) {
                        Text("Forgot Password?")
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Buttons side by side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                if (email.isNotBlank() && password.isNotBlank()) {
                                    isLoading = true
                                    errorMessage = ""
                                    showError = false
                                    
                                    // Launch coroutine for login
                                    (context as? MainActivity)?.lifecycleScope?.launch {
                                        val result = authManager.signIn(email, password)
                                        isLoading = false
                                        
                                        result.fold(
                                            onSuccess = { user ->
                                                onLoginSuccess()
                                            },
                                            onFailure = { exception ->
                                                errorMessage = exception.message ?: "Login failed"
                                                showError = true
                                            }
                                        )
                                    }
                                } else {
                                    errorMessage = "Please fill in all fields"
                                    showError = true
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Login")
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                // Navigate to GuestActivity
                                context.startActivity(Intent(context, DashboardActivity::class.java))
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading
                        ) {
                            Text("Guest")
                        }
                    }
                    
                    // Error message
                    if (showError && errorMessage.isNotBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    // Debug info with credentials
                    if (showDebugInfo) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Role selection
                        Text(
                            text = "Select role for debug login:",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall
                        )
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            val roles = listOf("ADMIN", "TEACHER", "PARENT", "GUEST")
                            roles.forEach { role ->
                                FilterChip(
                                    selected = selectedRole == role,
                                    onClick = {
                                        selectedRole = role
                                        when (role) {
                                            "ADMIN" -> {
                                                email = "admin@pesapp.com"
                                                password = "Admin1234"
                                            }
                                            "TEACHER" -> {
                                                email = "teacher@pesapp.com"
                                                password = "Teacher1"
                                            }
                                            "PARENT" -> {
                                                email = "parent@pesapp.com"
                                                password = "Parent1"
                                            }
                                            "GUEST" -> {
                                                email = "guest@pesapp.com"
                                                password = "Guest123"
                                            }
                                        }
                                    },
                                    label = { Text(role, style = MaterialTheme.typography.labelSmall) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        
                        // Show current credentials
                        Text(
                            text = "Debug credentials: $email / $password",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall
                        )
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { 
                                showDebugInfo = false 
                                email = ""
                                password = ""
                                passwordVisible = false
                            }) {
                                Text("Clear & Hide", style = MaterialTheme.typography.labelSmall)
                            }
                            TextButton(onClick = { showDebugInfo = false }) {
                                Text("Hide Only", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { 
                            showDebugInfo = true 
                            when (selectedRole) {
                                "ADMIN" -> {
                                    email = "admin@pesapp.com"
                                    password = "Admin1234"
                                }
                                "TEACHER" -> {
                                    email = "teacher@pesapp.com"
                                    password = "Teacher1"
                                }
                                "PARENT" -> {
                                    email = "parent@pesapp.com"
                                    password = "Parent1"
                                }
                                "GUEST" -> {
                                    email = "guest@pesapp.com"
                                    password = "Guest123"
                                }
                                else -> {
                                    email = "admin@pesapp.com"
                                    password = "Admin1234"
                                }
                            }
                            passwordVisible = true
                        }) {
                            Text("Show Debug Credentials", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    // TEMPORARY: Register Test Accounts Button
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            isLoading = true
                            errorMessage = ""
                            showError = false
                            (context as? MainActivity)?.lifecycleScope?.launch {
                                // Create accounts for all roles
                                val accounts = listOf(
                                    Triple("admin@pesapp.com", "Admin1234", com.qppd.pesapp.models.UserRole.ADMIN),
                                    Triple("teacher@pesapp.com", "Teacher1", com.qppd.pesapp.models.UserRole.TEACHER),
                                    Triple("parent@pesapp.com", "Parent1", com.qppd.pesapp.models.UserRole.PARENT),
                                    Triple("guest@pesapp.com", "Guest123", com.qppd.pesapp.models.UserRole.GUEST)
                                )
                                
                                var successCount = 0
                                var failureCount = 0
                                
                                for ((email, password, role) in accounts) {
                                    val displayName = "${role.name.lowercase().capitalize()} User"
                                    val result = authManager.createUser(
                                        email = email,
                                        password = password,
                                        displayName = displayName,
                                        role = role
                                    )
                                    
                                    result.fold(
                                        onSuccess = { user ->
                                            // Set role in Firestore (redundant, but safe)
                                            userManager.updateUserRole(user.uid, role)
                                            successCount++
                                        },
                                        onFailure = { exception ->
                                            failureCount++
                                        }
                                    )
                                }
                                
                                isLoading = false
                                if (successCount > 0) {
                                    showAdminSuccess = true
                                }
                                if (failureCount > 0) {
                                    errorMessage = "Failed to create $failureCount accounts. $successCount accounts created successfully."
                                    showError = true
                                }
                            }
                        },
                        enabled = !isLoading
                    ) {
                        Text("Register Test Accounts (DEV)")
                    }
                    if (showAdminSuccess) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Test accounts registered! Use the debug credentials above to log in.",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
    
    // Forgot Password Dialog
    if (showForgotPassword) {
        ForgotPasswordDialog(
            onDismiss = { showForgotPassword = false },
            onSendEmail = { emailAddress ->
                (context as? MainActivity)?.lifecycleScope?.launch {
                    val result = authManager.sendPasswordResetEmail(emailAddress)
                    showForgotPassword = false
                    
                    result.fold(
                        onSuccess = {
                            // Show success message
                        },
                        onFailure = { exception ->
                            errorMessage = exception.message ?: "Failed to send reset email"
                            showError = true
                        }
                    )
                }
            }
        )
    }
}

@Composable
fun ForgotPasswordDialog(
    onDismiss: () -> Unit,
    onSendEmail: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Forgot Password") },
        text = {
            Column {
                Text("Enter your email address to receive a password reset link.")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSendEmail(email) },
                enabled = email.isNotBlank()
            ) {
                Text("Send Reset Link")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    PESAppTheme {
        LoginScreen(onLoginSuccess = {})
    }
}
