package com.example.hewan

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*

class AdminSettingRentalActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddRental: FloatingActionButton
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_setting_rental)

        // Inisialisasi
        recyclerView = findViewById(R.id.recyclerViewAdminRental)
        fabAddRental = findViewById(R.id.fabAddRental)
        database = FirebaseDatabase.getInstance().getReference("rentals")

        // Load Data Rental
        loadRentalData()

        // Tambahkan Data Rental
        fabAddRental.setOnClickListener {
            showAddRentalDialog(this)
        }
    }

    private fun loadRentalData() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rentals = mutableListOf<Rental>()
                for (rentalSnapshot in snapshot.children) {
                    val rental = rentalSnapshot.getValue(Rental::class.java)
                    if (rental != null) {
                        rentals.add(rental)
                    }
                }
                recyclerView.layoutManager = LinearLayoutManager(this@AdminSettingRentalActivity)
                recyclerView.adapter = RentalAdapter(rentals)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AdminSettingRentalActivity, "Gagal memuat data rental", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Fungsi untuk menampilkan popup tambah rental
    private fun showAddRentalDialog(context: Context) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_add_rental)

        val inputPetId = dialog.findViewById<EditText>(R.id.inputPetId)
        val inputUserId = dialog.findViewById<EditText>(R.id.inputUserId)
        val inputStartDate = dialog.findViewById<EditText>(R.id.inputStartDate)
        val inputEndDate = dialog.findViewById<EditText>(R.id.inputEndDate)
        val btnAddRental = dialog.findViewById<Button>(R.id.btnAddRental)

        btnAddRental.setOnClickListener {
            val petId = inputPetId.text.toString()
            val userId = inputUserId.text.toString()
            val startDate = inputStartDate.text.toString()
            val endDate = inputEndDate.text.toString()

            if (petId.isEmpty() || userId.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
                Toast.makeText(context, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val rentalId = database.push().key ?: ""
            val rental = Rental(
                id = rentalId,
                petId = petId,
                userId = userId,
                startDate = startDate,
                endDate = endDate,
                status = "pending"
            )

            saveRentalToDatabase(rental) {
                if (it) {
                    Toast.makeText(context, "Rental berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                    loadRentalData() // Refresh data
                    dialog.dismiss()
                } else {
                    Toast.makeText(context, "Gagal menambahkan rental", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }

    private fun saveRentalToDatabase(rental: Rental, callback: (Boolean) -> Unit) {
        database.child(rental.id).setValue(rental)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }
}
