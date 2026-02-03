package com.federicocerra.gymlog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help & Support", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                GlassSettingContainer(title = "FAQ", subtitle = "Common Questions") {
                    FAQItem("How do I create a routine?", "Go to the Home tab and tap 'Create New Routine' at the bottom.")
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 12.dp))
                    FAQItem("How do I track weight PRs?", "PRs are automatically tracked based on your workout history for each exercise.")
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 12.dp))
                    FAQItem("Can I change my theme color?", "Yes, visit App Settings from your profile.")
                }
            }

            item {
                GlassSettingContainer(title = "CONTACT", subtitle = "Get in touch") {
                    ListItem(
                        headlineContent = { Text("Email Support", color = Color.White, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text("support@hevyclone.com", color = Color.Gray) },
                        leadingContent = { Icon(Icons.Default.Email, null, tint = MaterialTheme.colorScheme.primary) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 4.dp))
                    ListItem(
                        headlineContent = { Text("Community Forum", color = Color.White, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text("Join the discussion", color = Color.Gray) },
                        leadingContent = { Icon(Icons.Default.QuestionAnswer, null, tint = MaterialTheme.colorScheme.primary) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

@Composable
fun FAQItem(question: String, answer: String) {
    Column {
        Text(question, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(answer, color = Color.Gray, fontSize = 13.sp)
    }
}
