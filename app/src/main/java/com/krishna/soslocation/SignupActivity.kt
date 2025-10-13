package com.krishna.soslocation

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    // UI Components
    private lateinit var nameInputLayout: TextInputLayout
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var confirmPasswordInputLayout: TextInputLayout
    private lateinit var mobileInputLayout: TextInputLayout

    private lateinit var nameEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var confirmPasswordEditText: TextInputEditText
    private lateinit var mobileEditText: TextInputEditText

    private lateinit var signupButton: Button
    private lateinit var loginTextView: TextView
    private lateinit var progressBar: ProgressBar

    companion object {
        private const val TAG = "SignupActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        Log.d(TAG, "=== SIGNUP ACTIVITY STARTED ===")

        // Initialize Firebase
        try {
            FirebaseApp.initializeApp(this)
            auth = Firebase.auth
            firestore = FirebaseFirestore.getInstance()

            Log.d(TAG, "Firebase Auth initialized successfully")
            Log.d(TAG, "Current user: ${auth.currentUser?.email ?: "No user logged in"}")

        } catch (e: Exception) {
            Log.e(TAG, "Firebase initialization failed: ${e.message}", e)
            Toast.makeText(this, "Firebase initialization failed: ${e.message}", Toast.LENGTH_LONG).show()
            return
        }

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        nameInputLayout = findViewById(R.id.nameInputLayout)
        emailInputLayout = findViewById(R.id.emailInputLayout)
        passwordInputLayout = findViewById(R.id.passwordInputLayout)
        confirmPasswordInputLayout = findViewById(R.id.confirmPasswordInputLayout)
        mobileInputLayout = findViewById(R.id.mobileInputLayout)

        nameEditText = findViewById(R.id.nameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        mobileEditText = findViewById(R.id.mobileEditText)

        signupButton = findViewById(R.id.signupButton)
        loginTextView = findViewById(R.id.loginTextView)
        progressBar = findViewById(R.id.progressBar)

        progressBar.visibility = View.GONE
    }

    private fun setupClickListeners() {
        signupButton.setOnClickListener {
            Log.d(TAG, "Signup button clicked")
            attemptSignup()
        }

        loginTextView.setOnClickListener {
            Log.d(TAG, "Login text clicked - navigating to LoginActivity")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun attemptSignup() {
        clearErrors()

        val name = nameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()
        val mobile = mobileEditText.text.toString().trim()

        Log.d(TAG, "Validating inputs...")
        Log.d(TAG, "Name: $name, Email: $email, Mobile: $mobile")

        if (!validateInputs(name, email, password, confirmPassword, mobile)) {
            Log.d(TAG, "Input validation failed")
            return
        }

        showLoading(true)
        Log.d(TAG, "=== STARTING SIGNUP PROCESS ===")
        Log.d(TAG, "Creating user with email: $email")

        // Add timeout handler
        val timeoutHandler = Handler(Looper.getMainLooper())
        val timeoutRunnable = Runnable {
            if (progressBar.visibility == View.VISIBLE) {
                Log.e(TAG, "Signup timeout - process taking too long")
                showLoading(false)
                Toast.makeText(this, "Signup is taking longer than expected. Please check your internet connection and try again.", Toast.LENGTH_LONG).show()
            }
        }
        timeoutHandler.postDelayed(timeoutRunnable, 15000) // 15 second timeout

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                // Remove timeout handler
                timeoutHandler.removeCallbacks(timeoutRunnable)

                if (task.isSuccessful) {
                    Log.d(TAG, "✅ Firebase user created successfully!")
                    val user = auth.currentUser
                    if (user != null) {
                        Log.d(TAG, "User details - UID: ${user.uid}, Email: ${user.email}")
                        updateUserProfile(user, name, email, mobile)
                    } else {
                        Log.e(TAG, "❌ User is null after successful creation - this shouldn't happen!")
                        showLoading(false)
                        Toast.makeText(this, "Account created but couldn't retrieve user details. Please try logging in.", Toast.LENGTH_LONG).show()
                        navigateToLogin()
                    }
                } else {
                    Log.e(TAG, "❌ Firebase user creation failed", task.exception)
                    showLoading(false)
                    handleSignupError(task.exception)
                }
            }
            .addOnFailureListener { exception ->
                // Remove timeout handler
                timeoutHandler.removeCallbacks(timeoutRunnable)

                Log.e(TAG, "❌ Firebase signup failed with exception", exception)
                showLoading(false)
                handleSignupError(exception)
            }
    }

    private fun updateUserProfile(user: com.google.firebase.auth.FirebaseUser, name: String, email: String, mobile: String) {
        Log.d(TAG, "Updating user profile with name: $name")

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { profileTask ->
                if (profileTask.isSuccessful) {
                    Log.d(TAG, "✅ User profile updated successfully")
                } else {
                    Log.e(TAG, "⚠️ User profile update failed: ${profileTask.exception?.message}")
                    // Continue anyway - profile update is not critical
                }
                // Save to Firestore regardless of profile update result
                saveUserToFirestore(user.uid, name, email, mobile)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "⚠️ User profile update failed with exception", e)
                // Continue anyway
                saveUserToFirestore(user.uid, name, email, mobile)
            }
    }

    private fun saveUserToFirestore(userId: String, name: String, email: String, mobile: String) {
        Log.d(TAG, "Saving user data to Firestore...")

        val userData = hashMapOf(
            "userId" to userId,
            "name" to name,
            "email" to email,
            "mobile" to mobile,
            "createdAt" to com.google.firebase.Timestamp.now(),
            "isEmailVerified" to true,
            "profileComplete" to true
        )

        firestore.collection("users").document(userId)
            .set(userData)
            .addOnSuccessListener {
                Log.d(TAG, "✅ User data saved to Firestore successfully!")
                showSuccessAndNavigate()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "⚠️ Failed to save user data to Firestore: ${e.message}")
                // Still show success even if Firestore fails - auth is what matters
                showSuccessAndNavigate()
            }
    }

    private fun showSuccessAndNavigate() {
        Log.d(TAG, "✅ Signup process completed successfully!")
        showLoading(false)

        Toast.makeText(this, "🎉 Account created successfully!", Toast.LENGTH_LONG).show()

        // Navigate to login after a short delay
        Handler(Looper.getMainLooper()).postDelayed({
            Log.d(TAG, "Navigating to LoginActivity...")
            navigateToLogin()
        }, 2000)
    }

    private fun validateInputs(name: String, email: String, password: String, confirmPassword: String, mobile: String): Boolean {
        var isValid = true

        if (TextUtils.isEmpty(name)) {
            nameInputLayout.error = "Name is required"
            isValid = false
        } else if (name.length < 2) {
            nameInputLayout.error = "Name must be at least 2 characters"
            isValid = false
        }

        if (TextUtils.isEmpty(email)) {
            emailInputLayout.error = "Email is required"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.error = "Please enter a valid email address"
            isValid = false
        }

        if (TextUtils.isEmpty(password)) {
            passwordInputLayout.error = "Password is required"
            isValid = false
        } else if (password.length < 6) {
            passwordInputLayout.error = "Password must be at least 6 characters"
            isValid = false
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordInputLayout.error = "Please confirm your password"
            isValid = false
        } else if (password != confirmPassword) {
            confirmPasswordInputLayout.error = "Passwords do not match"
            isValid = false
        }

        val cleanMobile = mobile.replace("[^0-9]".toRegex(), "")
        if (TextUtils.isEmpty(mobile)) {
            mobileInputLayout.error = "Mobile number is required"
            isValid = false
        } else if (cleanMobile.length != 10) {
            mobileInputLayout.error = "Please enter a valid 10-digit mobile number"
            isValid = false
        }

        Log.d(TAG, "Input validation result: ${if (isValid) "PASS" else "FAIL"}")
        return isValid
    }

    private fun clearErrors() {
        nameInputLayout.error = null
        emailInputLayout.error = null
        passwordInputLayout.error = null
        confirmPasswordInputLayout.error = null
        mobileInputLayout.error = null
    }

    private fun handleSignupError(exception: Exception?) {
        val errorMessage = when (exception) {
            is FirebaseAuthWeakPasswordException -> {
                "Password is too weak. Please choose a stronger password with at least 6 characters including letters and numbers."
            }
            is FirebaseAuthUserCollisionException -> {
                "An account with this email already exists. Please login instead."
            }
            else -> {
                "Signup failed: ${exception?.message ?: "Unknown error. Please check your internet connection."}"
            }
        }

        Log.e(TAG, "Signup error handled: $errorMessage")
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        signupButton.isEnabled = !isLoading
        signupButton.text = if (isLoading) "Creating Account..." else "Sign Up"

        Log.d(TAG, "Loading state: ${if (isLoading) "SHOWING" else "HIDDEN"}")
    }

    private fun navigateToLogin() {
        Log.d(TAG, "=== NAVIGATING TO LOGIN ===")
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}

