package com.example

import android.app.Application
import com.example.util.PhoneAuthHelper

class StudioApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase with the official project configuration
        PhoneAuthHelper.initFirebase(this)
    }
}
