package com.federicocerra.gymlog.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutAppScreen(onBack: () -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About", fontWeight = FontWeight.ExtraBold) },
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item { Spacer(modifier = Modifier.height(40.dp)) }

            // App Logo / Icon
            item {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(primaryColor.copy(alpha = 0.1f))
                        .border(1.dp, primaryColor.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(50.dp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Hevy Clone",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Version 1.0.0 (Stable)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            item { Spacer(modifier = Modifier.height(48.dp)) }

            item {
                GlassSettingContainer( "THE PROJECT", "") {
                    Text(
                        text = "This app is built with Kotlin and Jetpack Compose, using firebase to administer accounts and workouts.",
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }

            item {
                GlassSettingContainer(title = "LEGAL", subtitle = "Terms & Privacy") {
                    TextButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                        Text("Privacy Policy", color = primaryColor)
                    }
                    TextButton(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                        Text("Terms of Service", color = primaryColor)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = "Made with ❤️ by Federico",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
