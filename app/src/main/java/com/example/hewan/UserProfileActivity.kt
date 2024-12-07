package com.example.hewan

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class UserProfileActivity : AppCompatActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var btnSaveChanges: Button
    private lateinit var btnUploadProfile: Button
    private lateinit var profileImageView: ImageView
    private var profileImageUri: Uri? = null
    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance().reference

    companion object {
        private const val TAG = "UserProfileActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        // Inisialisasi Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Inisialisasi komponen UI
        tvWelcome = findViewById(R.id.tvWelcome)
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        btnSaveChanges = findViewById(R.id.btnSaveChanges)
        btnUploadProfile = findViewById(R.id.btnUploadProfile)
        profileImageView = findViewById(R.id.profileImageView)

        // Ambil ID pengguna saat ini
        val userId = auth.currentUser?.uid
        Log.d(TAG, "Current user ID: $userId")

        if (userId != null) {
            loadProfileImage(userId) // Muat gambar profil dari cache
            fetchUserData(userId) // Ambil data pengguna dari Firebase
        } else {
            Log.e(TAG, "User ID is null")
            tvWelcome.text = "User not logged in"
        }

        // Tombol unggah gambar
        btnUploadProfile.setOnClickListener {
            pickImageFromGallery()
        }

        // Tombol simpan perubahan
        btnSaveChanges.setOnClickListener {
            val updatedName = etName.text.toString().trim()
            val updatedEmail = etEmail.text.toString().trim()

            if (userId != null && updatedName.isNotEmpty() && updatedEmail.isNotEmpty()) {
                updateUserData(userId, updatedName, updatedEmail)
            } else {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 1000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000 && resultCode == RESULT_OK) {
            profileImageUri = data?.data
            profileImageUri?.let {
                profileImageView.setImageURI(it)
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    saveProfileImage(it, userId)
                }
            }
        }
    }

    private fun saveProfileImage(uri: Uri, userId: String) {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            val file = File(cacheDir, "$userId.png")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            Toast.makeText(this, "Profile image saved successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save profile image: ${e.message}")
            Toast.makeText(this, "Failed to save profile image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadProfileImage(userId: String) {
        try {
            val file = File(cacheDir, "$userId.png")
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeStream(FileInputStream(file))
                profileImageView.setImageBitmap(bitmap)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load profile image: ${e.message}")
        }
    }

    private fun fetchUserData(userId: String) {
        database.child("users").child(userId).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val name = snapshot.child("name").getValue(String::class.java)
                    val email = snapshot.child("email").getValue(String::class.java)
                    Log.d(TAG, "Fetched user data: name = $name, email = $email")
                    if (name != null) {
                        tvWelcome.text = "Welcome, $name"
                        etName.setText(name)
                    }
                    if (email != null) {
                        etEmail.setText(email)
                    }
                } else {
                    tvWelcome.text = "User data not found"
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to fetch user data: ${exception.message}")
                tvWelcome.text = "Error loading user data"
            }
    }

    private fun updateUserData(userId: String, name: String, email: String) {
        val updates = mapOf(
            "name" to name,
            "email" to email
        )
        database.child("users").child(userId).updateChildren(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Data updated successfully", Toast.LENGTH_SHORT).show()
                tvWelcome.text = "Welcome, $name"
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to update user data: ${exception.message}")
                Toast.makeText(this, "Failed to update data", Toast.LENGTH_SHORT).show()
            }
    }
}
