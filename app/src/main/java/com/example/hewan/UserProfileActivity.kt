package com.example.hewan

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hewan.MainActivity
import com.example.hewan.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class UserProfileActivity : AppCompatActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var btnSaveChanges: Button
    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Find views by ID
        tvWelcome = findViewById(R.id.tvWelcome)
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        btnSaveChanges = findViewById(R.id.btnSaveChanges)

        val userId = auth.currentUser?.uid
        Log.d("UserProfile", "Current user ID: $userId")

        if (userId != null) {
            Log.d("UserProfile", "Attempting to fetch user data for userId: $userId")

            // Fetch user data from Firebase
            database.child("users").child(userId).get()
                .addOnSuccessListener { snapshot ->
                    Log.d("UserProfile", "Snapshot received: $snapshot")

                    if (snapshot.exists()) {
                        val name = snapshot.child("name").getValue(String::class.java)
                        val email = snapshot.child("email").getValue(String::class.java)
                        Log.d("UserProfile", "Fetched user data: name = $name, email = $email")

                        // Set the current name and email in the EditText fields
                        runOnUiThread {
                            if (name != null) {
                                tvWelcome.text = "Welcome, $name"
                                etName.setText(name)
                            }
                            if (email != null) {
                                etEmail.setText(email)
                            }
                        }
                    } else {
                        runOnUiThread {
                            tvWelcome.text = "User data not found"
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("UserProfile", "Failed to fetch data: ${exception.message}")
                    runOnUiThread {
                        tvWelcome.text = "Error loading user data"
                    }
                }
        } else {
            Log.e("UserProfile", "User ID is null")
            tvWelcome.text = "User not logged in"
        }

        // Save changes when the button is clicked
        btnSaveChanges.setOnClickListener {
            val updatedName = etName.text.toString()
            val updatedEmail = etEmail.text.toString()

            // Validate inputs
            if (updatedName.isNotEmpty() && updatedEmail.isNotEmpty()) {
                updateUserData(userId, updatedName, updatedEmail)
            } else {
                Toast.makeText(this, "Please fill out both fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUserData(userId: String?, name: String, email: String) {
        if (userId != null) {
            val userUpdates = mapOf(
                "name" to name,
                "email" to email
            )

            // Update the user data in Firebase
            database.child("users").child(userId).updateChildren(userUpdates)
                .addOnSuccessListener {
                    // Data updated successfully
                    Toast.makeText(this, "Data updated successfully", Toast.LENGTH_SHORT).show()
                    Log.d("UserProfile", "User data updated: name = $name, email = $email")

                    // Update UI with the new data
                    runOnUiThread {
                        // Update EditText with new values
                        etName.setText(name)
                        etEmail.setText(email)

                        // Optionally, update the TextView as well
                        tvWelcome.text = "Welcome, $name"
                    }
                }
                .addOnFailureListener { exception ->
                    // Show error if update fails
                    Toast.makeText(this, "Failed to update data: ${exception.message}", Toast.LENGTH_SHORT).show()
                    Log.e("UserProfile", "Error updating data: ${exception.message}")
                }
        }
    }


    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null) {
            // Redirect to login if user is not logged in
            val intent = Intent(this@UserProfileActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
