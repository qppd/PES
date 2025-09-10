package com.qppd.pesapp.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AdminPanelScreen(
    modifier: Modifier = Modifier
) {
    var showBulkRegistration by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Admin Panel",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        Button(
            onClick = { showBulkRegistration = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Bulk User Registration")
        }
        
        // Add more admin features here
        
        if (showBulkRegistration) {
            BulkRegistrationScreen(

            )
        }
    }
}
