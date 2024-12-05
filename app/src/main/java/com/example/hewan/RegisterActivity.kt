package com.example.hewan

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var inputName: EditText
    private lateinit var inputEmail: EditText
    private lateinit var inputPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var btnLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_activity)

        // Inisialisasi komponen
        inputName = findViewById(R.id.inputName)
        inputEmail = findViewById(R.id.inputEmail)
        inputPassword = findViewById(R.id.inputPassword)
        btnRegister = findViewById(R.id.btnRegister)
        btnLogin = findViewById(R.id.btnLogin)

        // Tombol Register
        btnRegister.setOnClickListener {
            val name = inputName.text.toString().trim()
            val email = inputEmail.text.toString().trim()
            val password = inputPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { authResult ->
                    val userId = authResult.user?.uid ?: ""
                    val userMap = hashMapOf(
                        "id" to userId,
                        "name" to name,
                        "email" to email,
                        "role" to "user" // Default role
                    )

                    // Simpan ke Realtime Database menggunakan FirebaseHelper
                    FirebaseHelper.getReference("users")
                        .child(userId)
                        .setValue(userMap)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Registrasi berhasil", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Gagal menyimpan data: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e ->
                    // Menangani error saat registrasi
                    if (e.message?.contains("email address is already in use") == true) {
                        Toast.makeText(this, "Email sudah digunakan", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Registrasi gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Tombol Login
        btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
