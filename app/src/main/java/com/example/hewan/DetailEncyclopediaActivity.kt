package com.example.hewan

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class DetailEncyclopediaActivity : AppCompatActivity() {

    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_encyclopedia)

        // Retrieve data from Intent
        val petId = intent.getStringExtra("PET_ID") ?: ""
        val petName = intent.getStringExtra("PET_NAME") ?: "Unknown"
        val petSpecies = intent.getStringExtra("PET_SPECIES") ?: "Unknown"
        val petDescription = intent.getStringExtra("PET_DESCRIPTION") ?: "No description available"
        val petImageUri = intent.getStringExtra("PET_IMAGE")
        val petStatus = intent.getStringExtra("PET_STATUS") ?: "Ready"
        val petStok = intent.getIntExtra("PET_STOK", 0)

        // Bind views
        val tvPetName: TextView = findViewById(R.id.tvPetName)
        val tvPetDescription: TextView = findViewById(R.id.tvPetDescription)
        val ivPetImage: ImageView = findViewById(R.id.ivPetImage)
        val fabAddRental: FloatingActionButton = findViewById(R.id.fabAddRental)
        val fabBack: FloatingActionButton = findViewById(R.id.fabBack)

        // Populate views
        tvPetName.text = "$petName ($petSpecies)"
        tvPetDescription.text = petDescription
        Glide.with(this).load(petImageUri).placeholder(R.drawable.placeholder_image).into(ivPetImage)

        // Check admin status and enable "+" button
        checkIfAdmin { isAdmin ->
            if (isAdmin) {
                fabAddRental.visibility = View.VISIBLE
                fabAddRental.setOnClickListener {
                    showAddRentalPopup(petName, petSpecies, petDescription, petImageUri, petStatus, petStok)
                }
            } else {
                fabAddRental.visibility = View.GONE
            }
        }

        // Back button functionality
        fabBack.setOnClickListener { finish() }
    }

    private fun checkIfAdmin(callback: (Boolean) -> Unit) {
        val userId = currentUser?.uid ?: return callback(false)
        val userRef = FirebaseDatabase.getInstance().getReference("users/$userId")
        userRef.get().addOnSuccessListener { snapshot ->
            val role = snapshot.child("role").getValue(String::class.java)
            callback(role == "admin")
        }.addOnFailureListener {
            Log.e("DetailEncyclopediaActivity", "Error fetching user role")
            callback(false)
        }
    }

    private fun showAddRentalPopup(
        name: String,
        species: String?,
        description: String?,
        imageUri: String?,
        currentStatus: String,
        currentStok: Int
    ) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.popup_add_rental, null)
        val spinnerStatus: Spinner = dialogView.findViewById(R.id.spinnerStatus)
        val etStok: EditText = dialogView.findViewById(R.id.etStok)
        val btnSubmit: Button = dialogView.findViewById(R.id.btnSubmit)

        // Pre-fill the current stock and status
        etStok.setText(currentStok.toString())
        val statusOptions = listOf("Ready", "Rented", "Not Ready")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStatus.adapter = adapter
        spinnerStatus.setSelection(statusOptions.indexOf(currentStatus))

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnSubmit.setOnClickListener {
            val selectedStatus = spinnerStatus.selectedItem.toString()
            val newStok = etStok.text.toString().toIntOrNull() ?: 0

            if (newStok >= 0) {
                saveToFirebase(name, species, imageUri, description, selectedStatus, newStok)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Stok harus berupa angka positif!", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun saveToFirebase(
        name: String,
        species: String?,
        photoUrl: String?,
        description: String?,
        status: String,
        stok: Int
    ) {
        val petRef = FirebaseDatabase.getInstance().getReference("pets")
        val rentalRef = FirebaseDatabase.getInstance().getReference("rentals")

        // Generate a unique ID for both pets and rentals
        val petId = petRef.push().key ?: return
        val rentalId = rentalRef.push().key ?: return

        // Determine category based on species
        val category = when (species?.lowercase()) {
            "dog" -> "Dog"
            "cat" -> "Cat"
            else -> "Unknown"
        }

        // Save to "pets" database
        val petData = mapOf(
            "id" to petId,
            "name" to name,
            "species" to species,
            "category" to category, // Add category
            "photo" to photoUrl,
            "description" to description,
            "stok" to stok,
            "status" to status
        )

        // Save to "rentals" database
        val rentalData = mapOf(
            "id" to rentalId,
            "petId" to petId, // Reference to pets ID
            "userId" to (currentUser?.uid ?: "unknown"),
            "startDate" to "",
            "endDate" to ""
        )

        petRef.child(petId).setValue(petData).addOnCompleteListener { petTask ->
            if (petTask.isSuccessful) {
                rentalRef.child(rentalId).setValue(rentalData).addOnCompleteListener { rentalTask ->
                    if (rentalTask.isSuccessful) {
                        Toast.makeText(this, "Data berhasil disimpan.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Gagal menyimpan ke rentals.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Gagal memperbarui data hewan.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
