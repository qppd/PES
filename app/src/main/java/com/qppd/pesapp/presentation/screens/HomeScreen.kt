package com.qppd.pesapp.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qppd.pesapp.models.UserRole

@Composable
fun HomeScreen(
    currentUserRole: UserRole?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Welcome to PES App",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = when (currentUserRole) {
                UserRole.ADMIN -> "You are logged in as an Administrator"
                UserRole.TEACHER -> "You are logged in as a Teacher"
                UserRole.PARENT -> "You are logged in as a Parent"
                UserRole.GUEST -> "You are logged in as a Guest"
                null -> "Welcome"
            },
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Add more content specific to each role here
    }
}
