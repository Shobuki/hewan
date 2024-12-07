package com.example.hewan

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase

class AdminDetailSettingRentalActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etSpecies: EditText
    private lateinit var spCategory: Spinner
    private lateinit var etDescription: EditText
    private lateinit var etStock: EditText
    private lateinit var spStatus: Spinner
    private lateinit var ivPhoto: ImageView
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button

    private lateinit var petId: String
    private lateinit var petPhotoUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_detail_setting_rental)

        // Bind Views
        etName = findViewById(R.id.etName)
        etSpecies = findViewById(R.id.etSpecies)
        spCategory = findViewById(R.id.spCategory)
        etDescription = findViewById(R.id.etDescription)
        etStock = findViewById(R.id.etStock)
        spStatus = findViewById(R.id.spStatus)
        ivPhoto = findViewById(R.id.ivPhoto)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)

        // Get data from Intent
        petId = intent.getStringExtra("petId") ?: ""
        val petName = intent.getStringExtra("petName") ?: ""
        val petSpecies = intent.getStringExtra("petSpecies") ?: ""
        val petCategory = intent.getStringExtra("petCategory") ?: "Unknown"
        petPhotoUrl = intent.getStringExtra("petPhoto") ?: ""
        val petDescription = intent.getStringExtra("petDescription") ?: ""
        val petStock = intent.getIntExtra("petStock", 0)
        val petStatus = intent.getStringExtra("petStatus") ?: "Ready"

        // Populate data
        etName.setText(petName)
        etSpecies.setText(petSpecies)
        etDescription.setText(petDescription)
        etStock.setText(petStock.toString())
        Glide.with(this).load(petPhotoUrl).placeholder(R.drawable.placeholder_image).into(ivPhoto)

        // Setup category spinner
        val categories = listOf("Cat", "Dog")
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCategory.adapter = categoryAdapter
        spCategory.setSelection(categories.indexOf(petCategory))

        // Setup status spinner
        val statuses = listOf("Ready", "Rented", "Not Ready")
        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statuses)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spStatus.adapter = statusAdapter
        spStatus.setSelection(statuses.indexOf(petStatus))

        // Save Button
        btnSave.setOnClickListener {
            savePetData()
        }

        // Cancel Button
        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun savePetData() {
        val name = etName.text.toString().trim()
        val species = etSpecies.text.toString().trim()
        val category = spCategory.selectedItem.toString()
        val description = etDescription.text.toString().trim()
        val stock = etStock.text.toString().toIntOrNull() ?: 0
        val status = spStatus.selectedItem.toString()

        if (name.isEmpty() || species.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "All fields must be filled!", Toast.LENGTH_SHORT).show()
            return
        }

        val petData = mapOf(
            "name" to name,
            "species" to species,
            "category" to category,
            "description" to description,
            "stok" to stock,
            "status" to status
        )

        FirebaseDatabase.getInstance().getReference("pets")
            .child(petId)
            .updateChildren(petData)
            .addOnSuccessListener {
                Toast.makeText(this, "Data updated successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update data.", Toast.LENGTH_SHORT).show()
            }
    }
}
