package com.example.hewan

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class UserProfileActivity : AppCompatActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var inputName: EditText
    private lateinit var inputEmail: EditText
    private lateinit var btnSaveProfile: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        // Inisialisasi Komponen
        tvWelcome = findViewById(R.id.tvWelcome)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        inputName = findViewById(R.id.inputName)
        inputEmail = findViewById(R.id.inputEmail)
        btnSaveProfile = findViewById(R.id.btnSaveProfile)

        val currentUser = FirebaseAuth.getInstance().currentUser

        // Menampilkan nama dan email pengguna
        if (currentUser != null) {
            val userId = currentUser.uid
            val dbRef = FirebaseHelper.getReference("users").child(userId)

            // Menggunakan FirebaseHelper untuk mengambil data pengguna
            dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val name = snapshot.child("name").value as? String ?: "User"
                        val email = snapshot.child("email").value as? String ?: "email@example.com"
                        Log.d("UserProfileActivity", "Name: $name, Email: $email")
                        tvWelcome.text = "Welcome, $name"
                        tvUserEmail.text = email

                        inputName.setText(name)
                        inputEmail.setText(email)
                    } else {
                        Log.e("UserProfileActivity", "Snapshot does not exist.")
                        Toast.makeText(this@UserProfileActivity, "Data pengguna tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("UserProfileActivity", "Database error: ${error.message}")
                    Toast.makeText(this@UserProfileActivity, "Gagal memuat data pengguna", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // Tombol Simpan
        btnSaveProfile.setOnClickListener {
            val updatedName = inputName.text.toString()
            val updatedEmail = inputEmail.text.toString()

            if (updatedName.isEmpty() || updatedEmail.isEmpty()) {
                Toast.makeText(this, "Nama dan email harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (currentUser != null) {
                val userId = currentUser.uid
                val dbRef = FirebaseHelper.getReference("users").child(userId)

                // Menggunakan FirebaseHelper untuk update data pengguna
                val updates = mapOf(
                    "name" to updatedName,
                    "email" to updatedEmail
                )

                dbRef.updateChildren(updates).addOnSuccessListener {
                    Toast.makeText(this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal memperbarui profil: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
