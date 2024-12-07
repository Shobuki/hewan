package com.example.hewan

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase

class AdminSettingRentalActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdminRentalAdapter
    private val pets = mutableListOf<Pet>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_setting_rental)

        recyclerView = findViewById(R.id.recyclerViewAdminRental)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadPets()
    }

    private fun loadPets() {
        FirebaseDatabase.getInstance().getReference("pets")
            .get()
            .addOnSuccessListener { snapshot ->
                pets.clear()
                for (petSnapshot in snapshot.children) {
                    val pet = petSnapshot.getValue(Pet::class.java)
                    if (pet != null) {
                        pets.add(pet)
                    }
                }
                adapter = AdminRentalAdapter(this, pets) { pet ->
                    confirmDelete(pet)
                }
                recyclerView.adapter = adapter
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load pets.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun confirmDelete(pet: Pet) {
        AlertDialog.Builder(this)
            .setTitle("Delete Pet")
            .setMessage("Are you sure you want to delete this pet?")
            .setPositiveButton("Yes") { _, _ ->
                deletePet(pet)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deletePet(pet: Pet) {
        FirebaseDatabase.getInstance().getReference("pets")
            .child(pet.id)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Pet deleted successfully.", Toast.LENGTH_SHORT).show()
                loadPets()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to delete pet.", Toast.LENGTH_SHORT).show()
            }
    }
}
