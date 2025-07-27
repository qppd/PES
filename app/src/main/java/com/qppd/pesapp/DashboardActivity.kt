package com.qppd.pesapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

import com.qppd.pesapp.auth.AuthManager
import com.qppd.pesapp.models.User
import com.qppd.pesapp.models.UserRole
import com.qppd.pesapp.screens.BulkRegistrationScreen
import com.qppd.pesapp.ui.theme.PESAppTheme
import com.qppd.pesapp.screens.AnnouncementListScreen
import com.qppd.pesapp.screens.FinancialReportListScreen
import com.qppd.pesapp.screens.UserManagementScreen
import com.qppd.pesapp.models.Announcement
import com.qppd.pesapp.auth.EventManager
import com.qppd.pesapp.models.Event

class DashboardActivity : ComponentActivity() {
    private val authManager = AuthManager.getInstance()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if user is logged in, if not redirect to login
        if (!authManager.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
        
        setContent {
            PESAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DashboardScreen(
                        onLogout = {
                            authManager.signOut()
                            startActivity(Intent(this@DashboardActivity, MainActivity::class.java))
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(onLogout: () -> Unit) {
    var selectedIndex by remember { mutableStateOf(0) }
    var currentUser by remember { mutableStateOf<User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showAnnouncementDetails by remember { mutableStateOf<Announcement?>(null) }

    val authManager = AuthManager.getInstance()

    // Get current user data
    LaunchedEffect(Unit) {
        authManager.getCurrentUser()?.let { firebaseUser ->
            currentUser = User(
                uid = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                displayName = firebaseUser.displayName ?: "User",
                role = if (firebaseUser.email?.contains("admin") == true) UserRole.ADMIN
                       else if (firebaseUser.email?.contains("teacher") == true) UserRole.TEACHER
                       else if (firebaseUser.email?.contains("parent") == true) UserRole.PARENT
                       else UserRole.GUEST
            )
        }
        isLoading = false
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Define navigation items based on user role
    val navigationItems = when (currentUser?.role) {
        UserRole.ADMIN -> listOf(
            BottomNavItem("Home", Icons.Default.Home),
            BottomNavItem("Announcements", Icons.Default.Campaign),
            BottomNavItem("Financial Reports", Icons.Default.Assessment),
            BottomNavItem("Admin Panel", Icons.Default.AdminPanelSettings),
            BottomNavItem("Profile", Icons.Default.Person)
        )
        UserRole.TEACHER -> listOf(
            BottomNavItem("Home", Icons.Default.Home),
            BottomNavItem("Announcements", Icons.Default.Campaign),
            BottomNavItem("Financial Reports", Icons.Default.Assessment),
            BottomNavItem("Profile", Icons.Default.Person)
        )
        else -> listOf(
        BottomNavItem("Home", Icons.Default.Home),
            BottomNavItem("Announcements", Icons.Default.Campaign),
        BottomNavItem("Financial Reports", Icons.Default.Assessment),
            BottomNavItem("Profile", Icons.Default.Person)
    )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Panaon ES",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                actions = {
                    currentUser?.role?.let { role ->
                        Text(
                            text = role.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                navigationItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            when (selectedIndex) {
                0 -> HomeScreen(currentUserRole = currentUser?.role)
                1 -> AnnouncementListScreen(currentUserRole = currentUser?.role ?: UserRole.GUEST)
                2 -> FinancialReportListScreen(currentUserRole = currentUser?.role ?: UserRole.GUEST)
                3 -> when (currentUser?.role) {
                    UserRole.ADMIN -> AdminPanelScreen()
                    else -> ParentProfileScreen(onLogout = onLogout)
                }
                4 -> ParentProfileScreen(onLogout = onLogout)
            }
        }
    }
}

@Composable
fun AdminPanelScreen() {
    var showBulkRegistration by remember { mutableStateOf(false) }
    var showUserManagement by remember { mutableStateOf(false) }
    
    if (showBulkRegistration) {
        BulkRegistrationScreen()
    } else if (showUserManagement) {
        UserManagementScreen()
    } else {
        Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Admin Panel",
                style = MaterialTheme.typography.headlineMedium
            )
            
            // Admin actions
            AdminActionCard(
                title = "Bulk User Registration",
                description = "Upload CSV file to register multiple parents",
                icon = Icons.Default.UploadFile,
                onClick = { showBulkRegistration = true }
            )
            
            AdminActionCard(
                title = "Manage Users",
                description = "Add, edit, or remove user accounts",
                icon = Icons.Default.People,
                onClick = { showUserManagement = true }
            )
            
            AdminActionCard(
                title = "Add Announcement",
                description = "Create new school announcements",
                icon = Icons.Default.Add,
                onClick = { /* TODO: Navigate to add announcement */ }
            )
            
            AdminActionCard(
                title = "Add Financial Report",
                description = "Create new financial reports",
                icon = Icons.Default.Assessment,
                onClick = { /* TODO: Navigate to add financial report */ }
            )
            
            AdminActionCard(
                title = "System Settings",
                description = "Configure app settings and permissions",
                icon = Icons.Default.Settings,
                onClick = { /* TODO: Navigate to settings */ }
            )
        }
    }
}

@Composable
fun AdminActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .padding(end = 16.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(text = description, style = MaterialTheme.typography.bodyMedium)
            }
            
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}






@Composable
fun ParentProfileScreen(onLogout: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingField by remember { mutableStateOf("") }

    val authManager = AuthManager.getInstance()
    val currentUser = authManager.getCurrentUser()
    
    // Initialize with current user data
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            email = user.email ?: ""
            displayName = user.displayName ?: "User"
            // In a real app, you'd fetch contact from Firestore
            contact = "+63 963 490 5586"
        }
    }

    val children = listOf("Yrel Munda Mendoza", "Aiyeen Munda Mendoza")

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 🔹 Profile Image with Upload Button
            Box(modifier = Modifier.size(130.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.profile),
                    contentDescription = "Profile Photo",
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .align(Alignment.Center)
                )
                IconButton(
                    onClick = { /* Upload logic */ },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Upload",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = displayName, style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(24.dp))

            // 🔹 Email
            InfoCard(
                title = "Email",
                value = email,
                onEditClick = {
                    editingField = "email"
                    showEditDialog = true
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 🔹 Contact
            InfoCard(
                title = "Contact",
                value = contact,
                onEditClick = {
                    editingField = "contact"
                    showEditDialog = true
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 🔹 Children Card
            ChildrenCard(children = children)
        }

        // 🔻 Logout Button
        Button(
            onClick = {
                onLogout()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text("Logout")
        }
    }
    
    // Edit Profile Dialog
    if (showEditDialog) {
        EditProfileDialog(
            field = editingField,
            currentValue = when (editingField) {
                "email" -> email
                "contact" -> contact
                else -> ""
            },
            onDismiss = { showEditDialog = false },
            onSave = { newValue ->
                when (editingField) {
                    "email" -> email = newValue
                    "contact" -> contact = newValue
                }
                showEditDialog = false
            }
        )
    }
}

@Composable
fun InfoCard(title: String, value: String, onEditClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.labelMedium)
                Text(text = value, style = MaterialTheme.typography.bodyLarge)
            }
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = "Edit $title")
            }
        }
    }
}


@Composable
fun ChildrenCard(children: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Children in Panaon Elementary School",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            children.forEachIndexed { index, name ->
                Text(
                    text = "• $name",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                if (index != children.lastIndex) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}




data class Parent(
    val name: String,
    val email: String,
    val contact: String,
    val profilePic: Int
)

@Composable
fun HomeScreen(currentUserRole: UserRole? = null) {
    var events by remember { mutableStateOf<List<Event>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var showAddEventDialog by remember { mutableStateOf(false) }
    var pendingEventToAdd by remember { mutableStateOf<Event?>(null) }
    
    val eventManager = EventManager.getInstance()
    
    // Load events from Firestore
    LaunchedEffect(Unit) {
        try {
            isLoading = true
            val allEvents = eventManager.getAllEvents()
            
            // If no events exist, add sample data for testing
            if (allEvents.isEmpty()) {
                val sampleEvents = listOf(
            Event(
                        id = "1",
                        title = "Coco Lilay Festival 2025",
                        description = "Annual school festival with cultural performances and food stalls. Students showcase traditional dances and local products.",
                        date = System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000L), // 30 days from now
                        location = "School Grounds",
                        category = com.qppd.pesapp.models.EventCategory.CULTURAL,
                        authorName = "School Admin",
                        tags = listOf("festival", "cultural", "celebration")
            ),
            Event(
                        id = "2",
                title = "Rape Prevention Lecture",
                description = "PMSg Mary Ann A Limbo conducted a lecture about rape prevention tips and other gender-based cases among the teachers and students.",
                        date = System.currentTimeMillis() + (15 * 24 * 60 * 60 * 1000L), // 15 days from now
                        location = "School Auditorium",
                        category = com.qppd.pesapp.models.EventCategory.WORKSHOP,
                        authorName = "School Admin",
                        tags = listOf("safety", "education", "awareness")
                    ),
                    Event(
                        id = "3",
                        title = "Parent-Teacher Meeting",
                        description = "Quarterly meeting to discuss student progress and school activities.",
                        date = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L), // 7 days from now
                        location = "Classrooms",
                        category = com.qppd.pesapp.models.EventCategory.MEETING,
                        authorName = "School Admin",
                        tags = listOf("meeting", "parent", "teacher")
                    )
                )
                
                // Add sample events to Firestore
                sampleEvents.forEach { event ->
                    eventManager.addEvent(event)
                }
                events = sampleEvents
            } else {
                events = allEvents
            }
            
            isLoading = false
        } catch (e: Exception) {
            errorMessage = "Failed to load events: ${e.message}"
            isLoading = false
        }
    }
    
    // Handle add event
    if (pendingEventToAdd != null) {
        LaunchedEffect(pendingEventToAdd) {
            val result = eventManager.addEvent(pendingEventToAdd!!)
            result.fold(
                onSuccess = {
                    events = listOf(pendingEventToAdd!!) + events
                    showAddEventDialog = false
                    pendingEventToAdd = null
                },
                onFailure = { exception ->
                    errorMessage = "Failed to add event: ${exception.message}"
                    pendingEventToAdd = null
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
                text = "School Events",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Add button (Admin/Teacher only)
            if (currentUserRole == UserRole.ADMIN || currentUserRole == UserRole.TEACHER) {
                IconButton(onClick = { showAddEventDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Event")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Events List
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (events.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No events found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(events) { event ->
                    EventCard(event = event)
                }
            }
        }
    }
    
    // Add Event Dialog (placeholder for now)
    if (showAddEventDialog) {
        // TODO: Implement AddEventDialog
        AlertDialog(
            onDismissRequest = { showAddEventDialog = false },
            title = { Text("Add Event") },
            text = { Text("Event creation dialog will be implemented here.") },
            confirmButton = {
                TextButton(onClick = { showAddEventDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun EventCard(event: Event) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
                modifier = Modifier
                    .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Event header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dateFormat.format(Date(event.date)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Category badge
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when (event.category) {
                            com.qppd.pesapp.models.EventCategory.CULTURAL -> MaterialTheme.colorScheme.secondary
                            com.qppd.pesapp.models.EventCategory.WORKSHOP -> MaterialTheme.colorScheme.primary
                            com.qppd.pesapp.models.EventCategory.MEETING -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.surface
                        }
                    )
                ) {
                    Text(
                        text = event.category.name,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Event description
            Text(
                text = event.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Event details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (event.location.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = event.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                if (event.attendees.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.People,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${event.attendees.size} attending",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Tags
            if (event.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(event.tags) { tag ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                        ) {
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}










@Composable
fun EditProfileDialog(
    field: String,
    currentValue: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var newValue by remember { mutableStateOf(currentValue) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit ${field.capitalize()}") },
        text = {
            OutlinedTextField(
                value = newValue,
                onValueChange = { newValue = it },
                label = { Text(field.capitalize()) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(newValue) },
                enabled = newValue.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

data class BottomNavItem(val label: String, val icon: ImageVector)
