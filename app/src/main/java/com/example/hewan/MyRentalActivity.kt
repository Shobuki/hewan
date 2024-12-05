package com.example.hewan

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MyRentalActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var btnListRental: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_my_rental)

        // Inisialisasi
        recyclerView = findViewById(R.id.recyclerViewUserRentals)
        btnListRental = findViewById(R.id.btnListRental)

        // Default tombol list rental tidak terlihat
        btnListRental.visibility = View.GONE

        // Cek Role User
        checkUserRole()

        // Load History Rental
        loadUserRentals()
    }

    private fun checkUserRole() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Anda harus login terlebih dahulu", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val dbRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.uid)
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val role = snapshot.child("role").value as? String ?: "user"
                if (role == "admin") {
                    btnListRental.visibility = View.VISIBLE
                    btnListRental.setOnClickListener {
                        startActivity(Intent(this@MyRentalActivity, AdminListRentalActivity::class.java))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MyRentalActivity, "Gagal memuat data pengguna", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadUserRentals() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Anda harus login terlebih dahulu", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val dbRef = FirebaseDatabase.getInstance().getReference("rentals")
        dbRef.orderByChild("userId").equalTo(currentUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val rentals = mutableListOf<Rental>()
                    for (data in snapshot.children) {
                        val rental = data.getValue(Rental::class.java)
                        if (rental != null) {
                            rentals.add(rental)
                        }
                    }
                    recyclerView.layoutManager = LinearLayoutManager(this@MyRentalActivity)
                    recyclerView.adapter = RentalAdapter(rentals)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MyRentalActivity, "Gagal memuat daftar rental", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
