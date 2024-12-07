package com.example.hewan

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.Log
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DetailRentalActivity : AppCompatActivity() {

    private lateinit var pet: Pet
    private val database = FirebaseDatabase.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_rental)

        // Retrieve pet data from Intent
        pet = Pet(
            id = intent.getStringExtra("petId") ?: "",
            name = intent.getStringExtra("petName") ?: "",
            species = intent.getStringExtra("petSpecies"),
            category = intent.getStringExtra("petCategory") ?: "",
            photo = intent.getStringExtra("petPhoto"),
            description = intent.getStringExtra("petDescription"),
            stok = intent.getIntExtra("petStock", 0),
            status = intent.getStringExtra("petStatus") ?: "Ready"
        )

        if (pet.id.isEmpty()) {
            Toast.makeText(this, "Pet ID tidak ditemukan!", Toast.LENGTH_SHORT).show()
            Log.e("DetailRentalActivity", "Pet ID kosong saat menerima data dari Intent.")
            finish()
            return
        } else {
            Log.d("DetailRentalActivity", "Pet ID diterima: ${pet.id}")
        }

        // Bind views
        val petImage: ImageView = findViewById(R.id.petImage)
        val petName: TextView = findViewById(R.id.petName)
        val petCategory: TextView = findViewById(R.id.petCategory)
        val petStatus: TextView = findViewById(R.id.petStatus)
        val petStock: TextView = findViewById(R.id.petStock)
        val petDescription: TextView = findViewById(R.id.petDescription)
        val rentButton: Button = findViewById(R.id.btnRent)

        // Populate views
        Glide.with(this).load(pet.photo).placeholder(R.drawable.placeholder_image).into(petImage)
        petName.text = pet.name
        petCategory.text = "Category: ${pet.category}"
        petStatus.text = "Status: ${pet.status}"
        petStock.text = "Stock: ${pet.stok}"
        petDescription.text = pet.description

        // Handle Rent Button
        rentButton.setOnClickListener {
            if (pet.stok > 0) {
                showRentPopup()
            } else {
                Toast.makeText(this, "Stok tidak tersedia!", Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun showRentPopup() {
        val dialogView = layoutInflater.inflate(R.layout.popup_add_rental_list, null)
        val startDateText: TextView = dialogView.findViewById(R.id.startDate)
        val endDateText: TextView = dialogView.findViewById(R.id.endDate)
        val confirmButton: Button = dialogView.findViewById(R.id.btnConfirm)

        val calendar = Calendar.getInstance()

        // Set default dates
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        startDateText.text = dateFormat.format(calendar.time)
        endDateText.text = dateFormat.format(calendar.time)

        // Disable submit button jika stok kosong
        if (pet.stok <= 0) {
            confirmButton.isEnabled = false
            Toast.makeText(this, "Stok kosong, tidak bisa menyewa!", Toast.LENGTH_SHORT).show()
        } else {
            confirmButton.isEnabled = true
        }

        // Date picker for start date
        startDateText.setOnClickListener {
            showDatePickerDialog { date ->
                startDateText.text = date
                if (endDateText.text.toString() < date) {
                    endDateText.text = date // Adjust end date if it's earlier than start date
                }
            }
        }

        // Date picker for end date
        endDateText.setOnClickListener {
            showDatePickerDialog { date ->
                if (date >= startDateText.text.toString()) {
                    endDateText.text = date
                } else {
                    Toast.makeText(this, "Tanggal akhir tidak boleh sebelum tanggal mulai!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // Confirm rent action
        confirmButton.setOnClickListener {
            val startDate = startDateText.text.toString()
            val endDate = endDateText.text.toString()

            if (startDate.isNotEmpty() && endDate.isNotEmpty()) {
                handleRent(startDate, endDate)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Tanggal harus dipilih!", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }


    private fun showDatePickerDialog(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, day ->
                val selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day)
                onDateSelected(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.minDate = calendar.timeInMillis // Set minimum date to today
        datePickerDialog.show()
    }

    private fun handleRent(startDate: String, endDate: String) {
        if (currentUser == null) {
            Toast.makeText(this, "Anda harus login untuk menyewa!", Toast.LENGTH_SHORT).show()
            return
        }

        if (pet.id.isEmpty()) {
            Toast.makeText(this, "Pet ID tidak valid!", Toast.LENGTH_SHORT).show()
            Log.e("DetailRentalActivity", "Pet ID kosong saat mencoba menyewa.")
            return
        }

        val rentalRef = database.getReference("rentals")
        val petRef = database.getReference("pets").child(pet.id)
        petRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                Log.d("DetailRentalActivity", "Pet ditemukan di Firebase: ${snapshot.key}")
            } else {
                Log.e("DetailRentalActivity", "Pet ID tidak ditemukan di Firebase: ${pet.id}")
                Toast.makeText(this, "Data hewan tidak ditemukan di server.", Toast.LENGTH_SHORT).show()
            }
        }


        val rentalId = rentalRef.push().key ?: return
        val rental = Rental(
            id = rentalId,
            petId = pet.id,
            userId = currentUser.uid,
            startDate = startDate,
            endDate = endDate
        )

        Log.d("DetailRentalActivity", "Menyimpan rental: $rental")

        // Save rental and update stock
        petRef.child("stok").setValue(pet.stok - 1).addOnCompleteListener { updateTask ->
            if (updateTask.isSuccessful) {
                Log.d("DetailRentalActivity", "Stok berhasil diperbarui untuk Pet ID: ${pet.id}")
                Toast.makeText(this, "Penyewaan berhasil!", Toast.LENGTH_SHORT).show()
                // Kirim sinyal ke MainActivity untuk memperbarui data
                finish() // Close activity
            } else {
                Log.e("DetailRentalActivity", "Gagal memperbarui stok hewan.")
                Toast.makeText(this, "Gagal memperbarui stok hewan!", Toast.LENGTH_SHORT).show()
            }
        }

    }


}
