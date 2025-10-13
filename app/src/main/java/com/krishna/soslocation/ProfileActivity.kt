package com.krishna.soslocation

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvMemberSince: TextView
    private lateinit var tvPhoneNumber: TextView
    private lateinit var tvJoinDate: TextView
    private lateinit var tvSOSCount: TextView
    private lateinit var tvContactsCount: TextView
    private lateinit var ivProfileImage: ImageView
    private lateinit var btnEditProfile: MaterialButton
    private lateinit var btnLogout: MaterialButton

    private lateinit var cardProfile: CardView
    private lateinit var cardAccountDetails: CardView

    private lateinit var firebaseUser: FirebaseUser
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initializeViews()

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        firestore = FirebaseFirestore.getInstance()

        loadUserData()
        setupClickListeners()
        animateViews()
    }

    private fun initializeViews() {
        tvUserName = findViewById(R.id.tvUserName)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        tvMemberSince = findViewById(R.id.tvMemberSince)
        tvPhoneNumber = findViewById(R.id.tvPhoneNumber)
        tvJoinDate = findViewById(R.id.tvJoinDate)
        tvSOSCount = findViewById(R.id.tvSOSCount)
        tvContactsCount = findViewById(R.id.tvContactsCount)
        ivProfileImage = findViewById(R.id.ivProfileImage)
        btnEditProfile = findViewById(R.id.btnEditProfile)
        btnLogout = findViewById(R.id.btnLogout)
        cardProfile = findViewById(R.id.cardProfile)
        cardAccountDetails = findViewById(R.id.cardAccountDetails)
    }

    private fun loadUserData() {
        // Set basic user info
        tvUserName.text = firebaseUser.displayName ?: "User Name"
        tvUserEmail.text = firebaseUser.email ?: "No Email"

        // Format and display creation date
        val creationDate = firebaseUser.metadata?.creationTimestamp
        if (creationDate != null) {
            val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            val date = Date(creationDate)
            tvMemberSince.text = "Member since ${sdf.format(date)}"
            tvJoinDate.text = "Joined: ${sdf.format(date)}"
        }

        // Load phone number
        tvPhoneNumber.text = firebaseUser.phoneNumber ?: "+91 Not Set"

        // Load profile image if available
        val photoUrl = firebaseUser.photoUrl
        if (photoUrl != null) {
            // Use Glide or Picasso to load image
            // Glide.with(this).load(photoUrl).into(ivProfileImage)
        }

        // Load statistics from Firestore
        loadStatistics()
    }

    private fun loadStatistics() {
        val userId = firebaseUser.uid

        // Load SOS count
        firestore.collection("sos_alerts")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                tvSOSCount.text = documents.size().toString()
                animateCounter(tvSOSCount, 0, documents.size())
            }
            .addOnFailureListener {
                tvSOSCount.text = "0"
            }

        // Load contacts count
        firestore.collection("users")
            .document(userId)
            .collection("emergency_contacts")
            .get()
            .addOnSuccessListener { documents ->
                tvContactsCount.text = documents.size().toString()
                animateCounter(tvContactsCount, 0, documents.size())
            }
            .addOnFailureListener {
                tvContactsCount.text = "0"
            }
    }

    private fun animateCounter(textView: TextView, start: Int, end: Int) {
        val animator = ObjectAnimator.ofInt(start, end)
        animator.duration = 1000
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { animation ->
            textView.text = animation.animatedValue.toString()
        }
        animator.start()
    }

    private fun setupClickListeners() {
        btnEditProfile.setOnClickListener {
            // Navigate to edit profile screen
            // startActivity(Intent(this, EditProfileActivity::class.java))
        }

        btnLogout.setOnClickListener {
            // Show confirmation dialog
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes") { _, _ ->
                    FirebaseAuth.getInstance().signOut()
                    // Navigate back to login screen
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        ivProfileImage.setOnClickListener {
            // Open image picker or profile image viewer
        }
    }

    private fun animateViews() {
        // Animate profile card
        cardProfile.alpha = 0f
        cardProfile.translationY = -50f
        cardProfile.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .setInterpolator(DecelerateInterpolator())
            .start()

        // Animate stats cards
        val layoutStats = findViewById<View>(R.id.layoutStats)
        layoutStats.alpha = 0f
        layoutStats.translationY = -30f
        layoutStats.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .setStartDelay(150)
            .setInterpolator(DecelerateInterpolator())
            .start()

        // Animate account details card
        cardAccountDetails.alpha = 0f
        cardAccountDetails.translationY = -30f
        cardAccountDetails.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .setStartDelay(300)
            .setInterpolator(DecelerateInterpolator())
            .start()

        // Animate buttons
        val layoutActions = findViewById<View>(R.id.layoutActions)
        layoutActions.alpha = 0f
        layoutActions.translationY = -30f
        layoutActions.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .setStartDelay(450)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }
}


