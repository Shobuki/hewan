package com.example.hewan

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DetailRentalActivity : AppCompatActivity() {

    private lateinit var tvPetName: TextView
    private lateinit var tvPetDescription: TextView
    private lateinit var btnRent: Button
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_rental)

        // Inisialisasi
        tvPetName = findViewById(R.id.tvPetName)
        tvPetDescription = findViewById(R.id.tvPetDescription)
        btnRent = findViewById(R.id.btnRent)
        database = FirebaseDatabase.getInstance().getReference("pets")

        val petId = intent.getStringExtra("petId")

        if (petId.isNullOrEmpty()) {
            Toast.makeText(this, "Data hewan tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Load Detail Hewan
        loadPetDetails(petId)

        // Aksi Tombol Rental
        btnRent.setOnClickListener {
            if (FirebaseAuth.getInstance().currentUser == null) {
                Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
            } else {
                showRentalPopup(petId)
            }
        }
    }

    private fun loadPetDetails(petId: String) {
        database.child(petId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val pet = snapshot.getValue(Pet::class.java)
                    tvPetName.text = pet?.name ?: "Nama tidak tersedia"
                    tvPetDescription.text = pet?.description ?: "Deskripsi tidak tersedia"
                } else {
                    Toast.makeText(this@DetailRentalActivity, "Data hewan tidak ditemukan", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DetailRentalActivity, "Gagal memuat data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showRentalPopup(petId: String) {
        // TODO: Implementasikan popup untuk memasukkan tanggal rental
        Toast.makeText(this, "Fitur rental belum diimplementasikan", Toast.LENGTH_SHORT).show()
    }
}
