package com.example.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.StudioViewModel
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import kotlinx.coroutines.launch

val StudioPrimary = Color(0xFF673AB7)
val StudioEmerald = Color(0xFF10B981)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(viewModel: StudioViewModel) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // Login Form State
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginPasswordVisible by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }

    // Register Form State
    var regName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }
    var regPasswordVisible by remember { mutableStateOf(false) }
    var regConfirmPasswordVisible by remember { mutableStateOf(false) }
    var regError by remember { mutableStateOf<String?>(null) }

    val emailFocusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Identity Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color(0xFF0D0D12), StudioPrimary.copy(alpha=0.3f))))
                .padding(vertical = 40.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(50.dp),
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Studio Account",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = "چوونەژوورەوە یان دروستکردنی هەژمار",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = StudioPrimary)
                .border(1.dp, Brush.linearGradient(listOf(StudioPrimary.copy(alpha = 0.5f), Color.Transparent)), RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0D12).copy(alpha=0.9f)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = StudioPrimary
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { 
                            selectedTabIndex = 0
                            loginError = null
                        },
                        text = { Text("چوونەژوورەوە", fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal) }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { 
                            selectedTabIndex = 1
                            regError = null
                        },
                        text = { Text("هەژماری نوێ", fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal) }
                    )
                }

                Box(modifier = Modifier.padding(20.dp)) {
                    if (selectedTabIndex == 0) {
                        // === چوونەژوورەوە (Login Form) ===
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "چوونەژوورەوە بە ئیمەیڵ",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            if (loginError != null) {
                                Surface(
                                    color = Color(0xFFFF5252).copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = loginError ?: "",
                                        color = Color(0xFFFF5252),
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Email Field
                            OutlinedTextField(
                                value = loginEmail,
                                onValueChange = { 
                                    loginEmail = it.trim()
                                    loginError = null
                                },
                                label = { Text("ئیمەیڵ (Email)") },
                                placeholder = { Text("user@example.com") },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = StudioPrimary)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = StudioPrimary, unfocusedBorderColor = Color.White.copy(alpha = 0.2f), focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedLabelColor = StudioPrimary, unfocusedLabelColor = Color.White.copy(alpha=0.6f), cursorColor = StudioPrimary, focusedContainerColor = Color.White.copy(alpha=0.05f), unfocusedContainerColor = Color.White.copy(alpha=0.02f)),
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Password Field
                            OutlinedTextField(
                                value = loginPassword,
                                onValueChange = { 
                                    loginPassword = it
                                    loginError = null
                                },
                                label = { Text("وشەی نهێنی (Password)") },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = StudioPrimary)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { loginPasswordVisible = !loginPasswordVisible }) {
                                        Icon(
                                            imageVector = if (loginPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = if (loginPasswordVisible) "شاردنەوە" else "پیشاندان"
                                        )
                                    }
                                },
                                visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = StudioPrimary, unfocusedBorderColor = Color.White.copy(alpha = 0.2f), focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedLabelColor = StudioPrimary, unfocusedLabelColor = Color.White.copy(alpha=0.6f), cursorColor = StudioPrimary, focusedContainerColor = Color.White.copy(alpha=0.05f), unfocusedContainerColor = Color.White.copy(alpha=0.02f)),
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    loginError = null
                                    val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
                                    if (loginEmail.isBlank() || !emailRegex.matches(loginEmail)) {
                                        loginError = "تکایە ئیمەیڵێکی دروست بنووسە."
                                        return@Button
                                    }
                                    if (loginPassword.isBlank()) {
                                        loginError = "تکایە وشەی نهێنی بنووسە."
                                        return@Button
                                    }
                                    
                                    // Check if email exists
                                    if (!viewModel.isEmailRegistered(loginEmail)) {
                                        loginError = "ئەم هەژمارە بوونی نییە"
                                        return@Button
                                    }
                                    
                                    // Check password match
                                    if (!viewModel.verifyPassword(loginEmail, loginPassword)) {
                                        loginError = "پاسۆرد هەڵەیە"
                                        return@Button
                                    }
                                    
                                    // Success
                                    viewModel.loginWithEmail(loginEmail, loginPassword)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                
                                shape = RoundedCornerShape(12.dp),
                                ) {
                                Text(
                                    text = "چوونەژوورەوە",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }
                        }
                    } else {
                        // === دروستکردنی هەژمار (Register Form) ===
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "دروستکردنی هەژماری نوێ",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            if (regError != null) {
                                Surface(
                                    color = Color(0xFFFF5252).copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = regError ?: "",
                                        color = Color(0xFFFF5252),
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Full Name
                            OutlinedTextField(
                                value = regName,
                                onValueChange = {
                                    regName = it
                                    regError = null
                                },
                                label = { Text("ناوی تەواو (Full Name)") },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = StudioPrimary)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = StudioPrimary, unfocusedBorderColor = Color.White.copy(alpha = 0.2f), focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedLabelColor = StudioPrimary, unfocusedLabelColor = Color.White.copy(alpha=0.6f), cursorColor = StudioPrimary, focusedContainerColor = Color.White.copy(alpha=0.05f), unfocusedContainerColor = Color.White.copy(alpha=0.02f)),
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Email Field
                            OutlinedTextField(
                                value = regEmail,
                                onValueChange = {
                                    regEmail = it.trim()
                                    regError = null
                                },
                                label = { Text("ئیمەیڵ (Email)") },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = StudioPrimary)
                                },
                                modifier = Modifier.fillMaxWidth().focusRequester(emailFocusRequester),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = StudioPrimary, unfocusedBorderColor = Color.White.copy(alpha = 0.2f), focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedLabelColor = StudioPrimary, unfocusedLabelColor = Color.White.copy(alpha=0.6f), cursorColor = StudioPrimary, focusedContainerColor = Color.White.copy(alpha=0.05f), unfocusedContainerColor = Color.White.copy(alpha=0.02f)),
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Password
                            OutlinedTextField(
                                value = regPassword,
                                onValueChange = {
                                    regPassword = it
                                    regError = null
                                },
                                label = { Text("وشەی نهێنی (لانی کەم ٦ پیت/ژمارە)") },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = StudioPrimary)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { regPasswordVisible = !regPasswordVisible }) {
                                        Icon(
                                            imageVector = if (regPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = if (regPasswordVisible) "شاردنەوە" else "پیشاندان"
                                        )
                                    }
                                },
                                visualTransformation = if (regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = StudioPrimary, unfocusedBorderColor = Color.White.copy(alpha = 0.2f), focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedLabelColor = StudioPrimary, unfocusedLabelColor = Color.White.copy(alpha=0.6f), cursorColor = StudioPrimary, focusedContainerColor = Color.White.copy(alpha=0.05f), unfocusedContainerColor = Color.White.copy(alpha=0.02f)),
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Confirm Password
                            OutlinedTextField(
                                value = regConfirmPassword,
                                onValueChange = {
                                    regConfirmPassword = it
                                    regError = null
                                },
                                label = { Text("دووبارەکردنەوەی وشەی نهێنی") },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = StudioPrimary)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { regConfirmPasswordVisible = !regConfirmPasswordVisible }) {
                                        Icon(
                                            imageVector = if (regConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = if (regConfirmPasswordVisible) "شاردنەوە" else "پیشاندان"
                                        )
                                    }
                                },
                                visualTransformation = if (regConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = StudioPrimary, unfocusedBorderColor = Color.White.copy(alpha = 0.2f), focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedLabelColor = StudioPrimary, unfocusedLabelColor = Color.White.copy(alpha=0.6f), cursorColor = StudioPrimary, focusedContainerColor = Color.White.copy(alpha=0.05f), unfocusedContainerColor = Color.White.copy(alpha=0.02f)),
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    regError = null
                                    
                                    if (regName.trim().length < 3) {
                                        regError = "تکایە ناوی تەواوی خۆت بنووسە."
                                        return@Button
                                    }
                                    
                                    val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
                                    if (regEmail.isBlank() || !emailRegex.matches(regEmail)) {
                                        regError = "تکایە ئیمەیڵێکی دروست بنووسە"
                                        return@Button
                                    }
                                    
                                    if (regPassword.length < 6) {
                                        regError = "وشەی نهێنی نابێت لە ٦ پیت/ژمارە کەمتر بێت."
                                        return@Button
                                    }
                                    
                                    if (regPassword != regConfirmPassword) {
                                        regError = "پاسۆردەکان وەک یەک نین"
                                        return@Button
                                    }
                                    
                                    if (viewModel.isEmailRegistered(regEmail)) {
                                        regError = "ئەم ئیمەیڵە پێشتر تۆمارکراوە."
                                        return@Button
                                    }
                                    
                                    // Success
                                    viewModel.registerWithEmail(regName.trim(), regEmail.trim(), regPassword)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                
                                shape = RoundedCornerShape(12.dp),
                                ) {
                                Text(
                                    text = "دروستکردنی هەژمار",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}
