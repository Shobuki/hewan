package com.example.hewan

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class AdminListRentalActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_list_rental)

        // Inisialisasi RecyclerView
        recyclerView = findViewById(R.id.recyclerViewAdminRentals)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Inisialisasi Realtime Database
        database = FirebaseDatabase.getInstance().getReference("rentals")

        // Load Semua Data Rental
        loadAllRentals()
    }

    private fun loadAllRentals() {
        // Mendapatkan data dari Realtime Database
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rentals = mutableListOf<Rental>()
                for (rentalSnapshot in snapshot.children) {
                    val rental = rentalSnapshot.getValue(Rental::class.java)
                    if (rental != null) {
                        rentals.add(rental)
                    }
                }
                recyclerView.adapter = RentalAdapter(rentals)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AdminListRentalActivity, "Gagal memuat daftar rental: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
