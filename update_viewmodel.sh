#!/bin/bash

# Insert sendVerificationEmail before registerWithEmail
sed -i '/fun registerWithEmail/i \
    fun sendVerificationEmail(name: String, email: String, onSent: (String) -> Unit, onError: (String) -> Unit) {\
        viewModelScope.launch {\
            val otpCode = com.example.util.EmailJsHelper.generate6DigitOtp()\
            val result = com.example.util.EmailJsHelper.sendOtpEmail(email, otpCode)\
            if (result.isSuccess) {\
                onSent(otpCode)\
            } else {\
                onError(result.exceptionOrNull()?.message ?: "هەڵە لە ناردنی کۆد")\
            }\
        }\
    }\
' app/src/main/java/com/example/ui/StudioViewModel.kt

