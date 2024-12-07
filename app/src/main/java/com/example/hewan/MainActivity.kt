package com.example.hewan

import RentalAdapter
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import java.io.File
import java.io.FileInputStream

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnLogin: Button
    private lateinit var btnProfile: ImageView
    private lateinit var btnAdminSettings: Button
    private lateinit var btnEncyclopedia: Button
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inisialisasi Komponen
        recyclerView = findViewById(R.id.recyclerViewMain)
        btnLogin = findViewById(R.id.btnLogin)
        btnProfile = findViewById(R.id.btnProfile)
        btnAdminSettings = findViewById(R.id.btnAdminSettings)
        btnEncyclopedia = findViewById(R.id.btnEncyclopedia)
        database = FirebaseHelper.getReference("pets")

        checkFirstLaunch()
        checkLoginStatus()
        loadRentalData()

        btnEncyclopedia.setOnClickListener {
            startActivity(Intent(this, EncyclopediaActivity::class.java))
        }
    }

    private fun checkFirstLaunch() {
        val sharedPrefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val isFirstLaunch = sharedPrefs.getBoolean("isFirstLaunch", true)

        if (isFirstLaunch) {
            FirebaseAuth.getInstance().signOut()
            val editor = sharedPrefs.edit()
            editor.putBoolean("isFirstLaunch", false)
            editor.apply()
        }
    }

    private fun checkLoginStatus() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            btnLogin.visibility = View.GONE
            btnProfile.visibility = View.VISIBLE

            loadProfileImage(currentUser.uid)

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
                        Toast.makeText(this@MainActivity, "Failed to load user data", Toast.LENGTH_SHORT).show()
                    }
                })

            btnProfile.setOnClickListener {
                showProfileMenu(it)
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
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val pets = mutableListOf<Pet>()
                for (petSnapshot in snapshot.children) {
                    val pet = petSnapshot.getValue(Pet::class.java)
                    if (pet != null) {
                        pets.add(pet)
                    }
                }

                recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
                recyclerView.adapter = RentalAdapter(pets)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Failed to load rentals", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showProfileMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.profile_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menuUserProfile -> {
                    startActivity(Intent(this, UserProfileActivity::class.java))
                    true
                }
                R.id.menuMyRental -> {
                    startActivity(Intent(this, MyRentalActivity::class.java))
                    true
                }
                R.id.menuLogout -> {
                    FirebaseAuth.getInstance().signOut()
                    Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun loadProfileImage(userId: String) {
        try {
            val file = File(cacheDir, "$userId.png")
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeStream(FileInputStream(file))
                btnProfile.setImageBitmap(bitmap)
            } else {
                btnProfile.setImageResource(R.drawable.placeholder_image) // Default image
            }
        } catch (e: Exception) {
            btnProfile.setImageResource(R.drawable.placeholder_image) // Default image
            Toast.makeText(this, "Failed to load profile image", Toast.LENGTH_SHORT).show()
        }
    }
}
