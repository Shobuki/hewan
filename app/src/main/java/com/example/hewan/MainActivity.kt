package com.example.hewan

import RentalAdapter
import android.content.Context
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
    private lateinit var btnEncyclopedia: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        // Inisialisasi Komponen
        recyclerView = findViewById(R.id.recyclerViewMain)
        btnLogin = findViewById(R.id.btnLogin)
        btnProfile = findViewById(R.id.btnProfile)
        btnAdminSettings = findViewById(R.id.btnAdminSettings)
        btnEncyclopedia = findViewById(R.id.btnEncyclopedia) // Initialize the new button from the toolbar
        database = FirebaseHelper.getReference("pets")


        checkFirstLaunch()
        // Memeriksa status login
        checkLoginStatus()

        // Load Data Rental
        loadRentalData()

        btnEncyclopedia.setOnClickListener {
            // Navigate to the Encyclopedia Activity
            startActivity(Intent(this, EncyclopediaActivity::class.java))
        }
    }

    private fun checkFirstLaunch() {
        // Check if it's the first time launching the app
        val sharedPrefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val isFirstLaunch = sharedPrefs.getBoolean("isFirstLaunch", true)

        if (isFirstLaunch) {
            // If it's the first launch, sign out the user to break the session
            FirebaseAuth.getInstance().signOut()

            // Set the flag so that the app knows it's not the first launch anymore
            val editor = sharedPrefs.edit()
            editor.putBoolean("isFirstLaunch", false)
            editor.apply()
        }
    }

    private fun checkLoginStatus() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            // User logged in, hide login button and show profile button
            btnLogin.visibility = View.GONE
            btnProfile.visibility = View.VISIBLE

            // Periksa role pengguna dan apakah admin
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

            // Aksi klik foto profil
            btnProfile.setOnClickListener {
                // Navigasi ke halaman pengaturan user (UserProfileActivity)
                startActivity(Intent(this@MainActivity, UserProfileActivity::class.java))
            }
        } else {
            // User is not logged in, show login button and hide profile button
            btnLogin.visibility = View.VISIBLE
            btnProfile.visibility = View.GONE

            // Handle click on login button
            btnLogin.setOnClickListener {
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }
    }

    private fun loadRentalData() {
        FirebaseHelper.getReference("pets")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val pets = mutableListOf<Pet>()
                    for (petSnapshot in snapshot.children) {
                        val pet = petSnapshot.getValue(Pet::class.java)
                        if (pet != null) {
                            pets.add(pet) // Tambahkan semua pet, baik stok kosong atau tidak
                        }
                    }

                    // Set up RecyclerView
                    recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
                    recyclerView.adapter = RentalAdapter(pets) // Menggunakan RentalAdapter
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MainActivity, "Failed to load rentals", Toast.LENGTH_SHORT).show()
                }
            })
    }





}
