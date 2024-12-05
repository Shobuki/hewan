package com.example.hewan

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class DetailEncyclopediaActivity : AppCompatActivity() {

    private lateinit var tvPetName: TextView
    private lateinit var tvPetDescription: TextView
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_encyclopedia)

        // Inisialisasi
        tvPetName = findViewById(R.id.tvPetName)
        tvPetDescription = findViewById(R.id.tvPetDescription)
        database = FirebaseDatabase.getInstance().getReference("pets")

        val petId = intent.getStringExtra("petId")

        // Load Detail Ensiklopedia
        if (petId != null) {
            loadPetDetails(petId)
        } else {
            Toast.makeText(this, "Pet ID tidak ditemukan", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this@DetailEncyclopediaActivity, "Detail tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DetailEncyclopediaActivity, "Gagal memuat detail: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
