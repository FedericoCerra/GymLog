package com.example.learningkotlin.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fireplace
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.learningkotlin.viewmodel.AuthViewModel

@Composable
fun AuthScreen(viewModel: AuthViewModel, onAuthSuccess: () -> Unit) {
    var isLogin by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = Color(0xFF1C1C1E)

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // 1. BACKGROUND DECORATION
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(size.width * 0.9f, size.height * 0.1f),
                    radius = size.width * 0.8f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.1f), Color.Transparent),
                    center = Offset(size.width * 0.1f, size.height * 0.9f),
                    radius = size.width * 0.7f
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 2. LOGO
            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(24.dp),
                color = primaryColor.copy(alpha = 0.1f),
                border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Fireplace,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isLogin) "Welcome Back" else "Join the Pack",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = (-1).sp
            )
            
            Text(
                text = if (isLogin) "Login to continue your journey" else "Start tracking your progress today",
                fontSize = 15.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // 3. INPUT CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = surfaceColor.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    AnimatedVisibility(
                        visible = !isLogin,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it },
                                label = { Text("Username") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = primaryColor,
                                    unfocusedBorderColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Black.copy(alpha = 0.3f),
                                    focusedContainerColor = Color.Black.copy(alpha = 0.3f)
                                ),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color.Transparent,
                            unfocusedContainerColor = Color.Black.copy(alpha = 0.3f),
                            focusedContainerColor = Color.Black.copy(alpha = 0.3f)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = null, tint = Color.Gray)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color.Transparent,
                            unfocusedContainerColor = Color.Black.copy(alpha = 0.3f),
                            focusedContainerColor = Color.Black.copy(alpha = 0.3f)
                        ),
                        singleLine = true
                    )

                    AnimatedVisibility(
                        visible = viewModel.authError != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Text(
                            text = viewModel.authError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 12.dp, start = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 4. ACTION BUTTON
            Button(
                onClick = {
                    val trimmedEmail = email.trim()
                    val trimmedPassword = password.trim()
                    val trimmedUsername = username.trim()
                    if (isLogin) {
                        viewModel.signIn(trimmedEmail, trimmedPassword) { if (it) onAuthSuccess() }
                    } else {
                        viewModel.signUp(trimmedEmail, trimmedUsername, trimmedPassword) { if (it) onAuthSuccess() }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    disabledContainerColor = primaryColor.copy(alpha = 0.3f)
                ),
                enabled = !viewModel.isLoading && email.isNotBlank() && password.isNotBlank() && (isLogin || username.isNotBlank())
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(
                        text = if (isLogin) "Sign In" else "Get Started",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { isLogin = !isLogin; viewModel.clearError() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isLogin) "New here?" else "Already a member?",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isLogin) "Create Account" else "Sign In",
                        color = primaryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
