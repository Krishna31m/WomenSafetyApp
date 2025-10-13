package com.krishna.soslocation

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseApp



class SplashActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private val splashDuration = 2500L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        firebaseAuth = FirebaseAuth.getInstance()

        // Start animations
        startAnimations()

        // Check authentication status after splash duration
        Handler(Looper.getMainLooper()).postDelayed({
            checkAuthenticationStatus()
        }, splashDuration)
    }

    /**
     * Start splash screen animations
     */
    private fun startAnimations() {
        val appLogoImageView: ImageView = findViewById(R.id.appLogoImageView)
        val appNameTextView: TextView = findViewById(R.id.appNameTextView)
        val loadingTextView: TextView = findViewById(R.id.loadingTextView)

        // Fade in animation for logo
        val fadeInAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        fadeInAnimation.duration = 1000
        appLogoImageView.startAnimation(fadeInAnimation)

        // Slide in animation for app name
        val slideInAnimation = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left)
        slideInAnimation.duration = 800
        slideInAnimation.startOffset = 300
        appNameTextView.startAnimation(slideInAnimation)

        // Blinking animation for loading text
        val blinkAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        blinkAnimation.duration = 1000
        blinkAnimation.repeatCount = 2
        blinkAnimation.startOffset = 800
        loadingTextView.startAnimation(blinkAnimation)
    }

    /**
     * Check if user is already logged in and navigate accordingly
     */
//    private fun checkAuthenticationStatus() {
//        val currentUser = firebaseAuth.currentUser
//
//        if (currentUser != null && currentUser.isEmailVerified) {
//            navigateToMainActivity()
//        } else {
//            navigateToLoginActivity()
//        }
//    }
    private fun checkAuthenticationStatus() {
        val currentUser = firebaseAuth.currentUser

        if (currentUser != null) {
            // User is logged in - NO EMAIL VERIFICATION REQUIRED
            navigateToMainActivity()
        } else {
            // User is not logged in
            navigateToLoginActivity()
        }
    }

    /**
     * Navigate to MainActivity (user already logged in)
     */
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    /**
     * Navigate to LoginActivity (user not logged in)
     */
    private fun navigateToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        // Use standard Android animations instead of custom ones
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        finish()
    }

    /**
     * Handle back button - prevent going back during splash
     */
    override fun onBackPressed() {
        // Do nothing - prevent user from going back during splash screen
    }
}


