package com.example.hewan

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnLogin: Button
    private lateinit var btnProfile: ImageView
    private lateinit var btnAdminSettings: Button
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Memanggil monitorConnection dan testConnection dari FirebaseHelper
        FirebaseHelper.monitorConnection()
        FirebaseHelper.testConnection()

        // Inisialisasi Komponen
        recyclerView = findViewById(R.id.recyclerViewMain)
        btnLogin = findViewById(R.id.btnLogin)
        btnProfile = findViewById(R.id.btnProfile)
        btnAdminSettings = findViewById(R.id.btnAdminSettings)
        database = FirebaseHelper.getReference("pets") // Menggunakan FirebaseHelper

        // Cek Status Login
        checkLoginStatus()

        // Load Data Rental
        loadRentalData()
    }

    private fun checkLoginStatus() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            btnLogin.visibility = View.GONE
            btnProfile.visibility = View.VISIBLE

            // Periksa apakah user adalah admin menggunakan FirebaseHelper
            FirebaseHelper.getReference("users").child(currentUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val role = snapshot.child("role").value as? String ?: "user"
                        if (role == "admin") {
                            btnAdminSettings.visibility = View.VISIBLE
                            btnAdminSettings.setOnClickListener {
                                startActivity(Intent(this@MainActivity, AdminSettingRentalActivity::class.java))
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@MainActivity, "Gagal memuat data user", Toast.LENGTH_SHORT).show()
                    }
                })

            // Tambahkan Aksi Klik Foto Profil
            btnProfile.setOnClickListener {
                startActivity(Intent(this, SettingUserActivity::class.java))
            }
        } else {
            btnLogin.visibility = View.VISIBLE
            btnProfile.visibility = View.GONE
            btnLogin.setOnClickListener {
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }
    }

    private fun loadRentalData() {
        FirebaseHelper.getReference("pets").orderByChild("status").equalTo("available")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val pets = mutableListOf<Pet>()
                    for (petSnapshot in snapshot.children) {
                        val pet = petSnapshot.getValue(Pet::class.java)
                        if (pet != null) {
                            pets.add(pet)
                        }
                    }
                    recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
                    recyclerView.adapter = PetAdapter(pets) { pet ->
                        val intent = Intent(this@MainActivity, DetailRentalActivity::class.java)
                        intent.putExtra("petId", pet.id)
                        startActivity(intent)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MainActivity, "Gagal memuat data rental", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
