package com.qppd.pesapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

import com.qppd.pesapp.auth.UserManager
import com.qppd.pesapp.auth.AuthManager
import com.qppd.pesapp.models.User
import com.qppd.pesapp.models.UserRole
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen() {
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddUserDialog by remember { mutableStateOf(false) }
    var showEditUserDialog by remember { mutableStateOf<User?>(null) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    var pendingUserToAdd by remember { mutableStateOf<User?>(null) }
    var pendingUserToEdit by remember { mutableStateOf<User?>(null) }
    var pendingUserToDelete by remember { mutableStateOf<User?>(null) }
    
    val userManager = UserManager.getInstance()
    val authManager = AuthManager.getInstance()
    
    // Load users
    LaunchedEffect(Unit) {
        try {
            isLoading = true
            val result = userManager.getAllUsers()
            result.fold(
                onSuccess = { userList ->
                    users = userList
                    isLoading = false
                },
                onFailure = { exception ->
                    errorMessage = exception.message ?: "Failed to load users"
                    isLoading = false
                }
            )
        } catch (e: Exception) {
            errorMessage = "Failed to load users: ${e.message}"
            isLoading = false
        }
    }
    
    // Handle add user
    if (pendingUserToAdd != null) {
        LaunchedEffect(pendingUserToAdd) {
            val result = userManager.createUser(pendingUserToAdd!!)
            result.fold(
                onSuccess = {
                    users = listOf(pendingUserToAdd!!) + users
                    showAddUserDialog = false
                    successMessage = "User added successfully"
                    pendingUserToAdd = null
                },
                onFailure = { exception ->
                    errorMessage = "Failed to add user: ${exception.message}"
                    pendingUserToAdd = null
                }
            )
        }
    }
    
    // Handle edit user
    if (pendingUserToEdit != null) {
        LaunchedEffect(pendingUserToEdit) {
            val result = userManager.updateUserProfile(
                mapOf(
                    "displayName" to pendingUserToEdit!!.displayName,
                    "role" to pendingUserToEdit!!.role,
                    "contactNumber" to pendingUserToEdit!!.contactNumber,
                    "children" to pendingUserToEdit!!.children
                )
            )
            result.fold(
                onSuccess = {
                    users = users.map { if (it.uid == pendingUserToEdit!!.uid) pendingUserToEdit!! else it }
                    showEditUserDialog = null
                    successMessage = "User updated successfully"
                    pendingUserToEdit = null
                },
                onFailure = { exception ->
                    errorMessage = "Failed to update user: ${exception.message}"
                    pendingUserToEdit = null
                }
            )
        }
    }
    
    // Handle delete user
    if (pendingUserToDelete != null) {
        LaunchedEffect(pendingUserToDelete) {
            val result = userManager.deleteUser(pendingUserToDelete!!.uid)
            result.fold(
                onSuccess = {
                    users = users.filter { it.uid != pendingUserToDelete!!.uid }
                    successMessage = "User deleted successfully"
                    pendingUserToDelete = null
                },
                onFailure = { exception ->
                    errorMessage = "Failed to delete user: ${exception.message}"
                    pendingUserToDelete = null
                }
            )
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "User Management",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = { showAddUserDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add User")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Users List
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (users.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No users found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(users) { user ->
                    UserCard(
                        user = user,
                        onEdit = { showEditUserDialog = user },
                        onDelete = { pendingUserToDelete = user }
                    )
                }
            }
        }
    }
    
    // Add User Dialog
    if (showAddUserDialog) {
        AddUserDialog(
            onDismiss = { showAddUserDialog = false },
            onAddUser = { email, password, displayName, role ->
                val newUser = User(
                    uid = "",
                    email = email,
                    displayName = displayName,
                    role = role,
                    contactNumber = "",
                    children = emptyList()
                )
                pendingUserToAdd = newUser
            }
        )
    }
    
    // Edit User Dialog
    val editDialogUser = showEditUserDialog
    if (editDialogUser != null) {
        EditUserDialog(
            user = editDialogUser,
            onDismiss = { showEditUserDialog = null },
            onEditUser = { updatedUser ->
                pendingUserToEdit = updatedUser
            }
        )
    }
    
    // Success/Error Messages
    if (successMessage.isNotEmpty()) {
        LaunchedEffect(successMessage) {
            kotlinx.coroutines.delay(3000)
            successMessage = ""
        }
    }
    
    if (errorMessage.isNotEmpty()) {
        LaunchedEffect(errorMessage) {
            kotlinx.coroutines.delay(3000)
            errorMessage = ""
        }
    }
}

@Composable
fun UserCard(
    user: User,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.displayName.ifEmpty { "No Name" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Role: ${user.role.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = when (user.role) {
                            UserRole.ADMIN -> MaterialTheme.colorScheme.error
                            UserRole.TEACHER -> MaterialTheme.colorScheme.primary
                            UserRole.PARENT -> MaterialTheme.colorScheme.secondary
                            UserRole.GUEST -> MaterialTheme.colorScheme.tertiary
                        }
                    )
                    if (user.contactNumber.isNotBlank()) {
                        Text(
                            text = "Contact: ${user.contactNumber}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (user.children.isNotEmpty()) {
                        Text(
                            text = "Children: ${user.children.joinToString(", ")}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(
                        text = "Created: ${dateFormat.format(Date(user.createdAt))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit User")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete User")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserDialog(
    onDismiss: () -> Unit,
    onAddUser: (String, String, String, UserRole) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.PARENT) }
    var showPassword by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New User") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showPassword) "Hide password" else "Show password"
                            )
                        }
                    }
                )
                
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Display Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Role Selection
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = { },
                ) {
                    OutlinedTextField(
                        value = selectedRole.name,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Role") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    DropdownMenu(
                        expanded = false,
                        onDismissRequest = { },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        for (role in UserRole.values()) {
                            DropdownMenuItem(
                                text = { Text(role.name) },
                                onClick = { selectedRole = role }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank() && displayName.isNotBlank()) {
                        onAddUser(email, password, displayName, selectedRole)
                    }
                },
                enabled = email.isNotBlank() && password.isNotBlank() && displayName.isNotBlank()
            ) {
                Text("Add User")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserDialog(
    user: User,
    onDismiss: () -> Unit,
    onEditUser: (User) -> Unit
) {
    var displayName by remember { mutableStateOf(user.displayName) }
    var selectedRole by remember { mutableStateOf(user.role) }
    var contactNumber by remember { mutableStateOf(user.contactNumber) }
    var children by remember { mutableStateOf(user.children.joinToString(", ")) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit User") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Display Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = contactNumber,
                    onValueChange = { contactNumber = it },
                    label = { Text("Contact Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = children,
                    onValueChange = { children = it },
                    label = { Text("Children (comma-separated)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Role Selection
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = { },
                ) {
                    OutlinedTextField(
                        value = selectedRole.name,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Role") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    DropdownMenu(
                        expanded = false,
                        onDismissRequest = { },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        for (role in UserRole.values()) {
                            DropdownMenuItem(
                                text = { Text(role.name) },
                                onClick = { selectedRole = role }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val updatedUser = user.copy(
                        displayName = displayName,
                        role = selectedRole,
                        contactNumber = contactNumber,
                        children = children.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    )
                    onEditUser(updatedUser)
                },
                enabled = displayName.isNotBlank()
            ) {
                Text("Update User")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 