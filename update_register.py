import re

with open('app/src/main/java/com/example/ui/account/AccountScreen.kt', 'r') as f:
    text = f.read()

bad = """                        // === دروستکردنی هەژمار (Register Form) ===
                        Text(
                            text = "دروستکردنی هەژماری نوێ",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "زانیارییەکانت پڕ بکەرەوە بۆ دەستپێکردنی ڕاستەوخۆ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (regError != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = regError ?: "",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // ناوی تەواو (Full Name)
                        OutlinedTextField(
                            value = regName,
                            onValueChange = {
                                regName = it
                                regError = null
                            },
                            label = { Text("ناوی تەواو (Full Name)") },
                            placeholder = { Text("وەک: مەبەست یاسین") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = StudioPrimary)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // ئیمەیڵ (Email)
                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = {
                                regEmail = it.trim()
                                regError = null
                            },
                            label = { Text("ئیمەیڵ (Email)") },
                            placeholder = { Text("user@example.com") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = StudioPrimary)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // وشەی نهێنی (Password)
                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = {
                                regPassword = it
                                regError = null
                            },
                            label = { Text("وشەی نهێنی (Password)") },
                            placeholder = { Text("وشەی نهێنی (لانیکەم ٤ کارەکتەر)") },
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
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // دوگمەی سەرەکی: "دروستکردنی هەژمار"
                        Button(
                            onClick = {
                                if (regName.isBlank()) {
                                    regError = "تکایە ناوی تەواوی خۆت بنووسە."
                                    return@Button
                                }
                                if (regEmail.isBlank() || !regEmail.contains("@") || !regEmail.contains(".")) {
                                    regError = "تکایە ناونیشانی ئیمەیڵی دروست بنووسە."
                                    return@Button
                                }
                                if (regPassword.length < 4) {
                                    regError = "تکایە وشەی نهێنی لانیکەم ٤ پیت بێت."
                                    return@Button
                                }
                                viewModel.registerWithEmail(regName.trim(), regEmail.trim(), regPassword)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "دروستکردنی هەژمار",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }"""

good = """                        // === دروستکردنی هەژمار (Register Form) ===
                        if (!isVerifyingOtp) {
                            Text(
                                text = "دروستکردنی هەژماری نوێ",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "زانیارییەکانت پڕ بکەرەوە بۆ دەستپێکردنی ڕاستەوخۆ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            if (regError != null) {
                                Surface(
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = regError ?: "",
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // ناوی تەواو (Full Name)
                            OutlinedTextField(
                                value = regName,
                                onValueChange = {
                                    regName = it
                                    regError = null
                                },
                                label = { Text("ناوی تەواو (Full Name)") },
                                placeholder = { Text("وەک: مەبەست یاسین") },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = StudioPrimary)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // ئیمەیڵ (Email)
                            OutlinedTextField(
                                value = regEmail,
                                onValueChange = {
                                    regEmail = it.trim()
                                    regError = null
                                },
                                label = { Text("ئیمەیڵ (Email)") },
                                placeholder = { Text("user@example.com") },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = StudioPrimary)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // وشەی نهێنی (Password)
                            OutlinedTextField(
                                value = regPassword,
                                onValueChange = {
                                    regPassword = it
                                    regError = null
                                },
                                label = { Text("وشەی نهێنی (Password)") },
                                placeholder = { Text("وشەی نهێنی (لانیکەم ٦ کارەکتەر)") },
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
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // دوگمەی سەرەکی: "دروستکردنی هەژمار"
                            Button(
                                onClick = {
                                    if (regName.trim().length < 3) {
                                        regError = "تکایە ناوی تەواوی خۆت بنووسە (لانیکەم ٣ پیت)."
                                        return@Button
                                    }
                                    
                                    val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
                                    if (regEmail.isBlank() || !emailRegex.matches(regEmail)) {
                                        regError = "تکایە ناونیشانی ئیمەیڵی دروست بنووسە."
                                        return@Button
                                    }
                                    
                                    if (regPassword.length < 6) {
                                        regError = "وشەی نهێنی بە هیچ شێوەیەک نابێت کەمتر لە ٦ پیت بێت."
                                        return@Button
                                    }
                                    
                                    regError = null
                                    isSendingOtp = true
                                    viewModel.sendVerificationEmail(
                                        name = regName.trim(),
                                        email = regEmail.trim(),
                                        onSent = { code -> 
                                            sentOtpCode = code
                                            isSendingOtp = false
                                            isVerifyingOtp = true
                                        },
                                        onError = { error ->
                                            regError = error
                                            isSendingOtp = false
                                        }
                                    )
                                },
                                enabled = !isSendingOtp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = StudioPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isSendingOtp) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                } else {
                                    Text(
                                        text = "دروستکردنی هەژمار",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        } else {
                            // === مۆداڵی داخڵکردنی کۆد (OTP Verification) ===
                            Text(
                                text = "پشتڕاستکردنەوەی ئیمەیڵ",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "کۆدێکی ٦ ژمارەییمان ناردووە بۆ ئیمەیڵەکەت: ${regEmail}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            if (regError != null) {
                                Surface(
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = regError ?: "",
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                            
                            OutlinedTextField(
                                value = enteredOtpCode,
                                onValueChange = {
                                    enteredOtpCode = it.filter { char -> char.isDigit() }.take(6)
                                    regError = null
                                },
                                label = { Text("کۆدی ٦ ژمارەیی بنووسە") },
                                placeholder = { Text("XXXXXX") },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = StudioEmerald)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            Button(
                                onClick = {
                                    if (enteredOtpCode == sentOtpCode) {
                                        viewModel.registerWithEmail(regName.trim(), regEmail.trim(), regPassword)
                                        isVerifyingOtp = false
                                        enteredOtpCode = ""
                                    } else {
                                        regError = "کۆدەکە هەڵەیە، تکایە ئیمەیڵەکەت بپشکنەوە"
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = StudioEmerald),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "پشتڕاستکردنەوە",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            OutlinedButton(
                                onClick = {
                                    isVerifyingOtp = false
                                    enteredOtpCode = ""
                                    regError = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("گەڕانەوە بۆ ڕووکاری پێشوو")
                            }
                        }"""

if bad in text:
    text = text.replace(bad, good)
    with open('app/src/main/java/com/example/ui/account/AccountScreen.kt', 'w') as f:
        f.write(text)
    print("Replaced successfully")
else:
    print("Could not find bad block")

