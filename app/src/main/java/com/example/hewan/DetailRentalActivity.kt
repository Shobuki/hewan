package com.example.hewan

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
            finish()
            return
        }

        setupViews()
    }

    private fun setupViews() {
        val petImage: ImageView = findViewById(R.id.petImage)
        val petName: TextView = findViewById(R.id.petName)
        val petCategory: TextView = findViewById(R.id.petCategory)
        val petStatus: TextView = findViewById(R.id.petStatus)
        val petStock: TextView = findViewById(R.id.petStock)
        val petDescription: TextView = findViewById(R.id.petDescription)
        val rentButton: Button = findViewById(R.id.btnRent)

        Glide.with(this).load(pet.photo).placeholder(R.drawable.placeholder_image).into(petImage)
        petName.text = pet.name
        petCategory.text = "Category: ${pet.category}"
        petStatus.text = "Status: ${pet.status}"
        petStock.text = "Stock: ${pet.stok}"
        petDescription.text = pet.description

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
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        startDateText.text = dateFormat.format(calendar.time)
        endDateText.text = dateFormat.format(calendar.time)

        confirmButton.isEnabled = pet.stok > 0

        startDateText.setOnClickListener {
            showDatePickerDialog { date ->
                startDateText.text = date
                if (endDateText.text.toString() < date) {
                    endDateText.text = date
                }
            }
        }

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
        datePickerDialog.datePicker.minDate = calendar.timeInMillis
        datePickerDialog.show()
    }

    private fun handleRent(startDate: String, endDate: String) {
        if (currentUser == null) {
            Toast.makeText(this, "Anda harus login untuk menyewa!", Toast.LENGTH_SHORT).show()
            return
        }

        val rentalRef = database.getReference("rentals")
        val petRef = database.getReference("pets").child(pet.id)

        petRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val rentalId = rentalRef.push().key ?: return@addOnSuccessListener
                val rental = Rental(
                    id = rentalId,
                    petId = pet.id,
                    userId = currentUser.uid,
                    startDate = startDate,
                    endDate = endDate
                )

                petRef.child("stok").setValue(pet.stok - 1).addOnCompleteListener { updateTask ->
                    if (updateTask.isSuccessful) {
                        rentalRef.child(rentalId).setValue(rental).addOnCompleteListener { saveTask ->
                            if (saveTask.isSuccessful) {
                                Toast.makeText(this, "Penyewaan berhasil!", Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                Toast.makeText(this, "Gagal menyimpan data penyewaan!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(this, "Gagal memperbarui stok!", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Hewan tidak ditemukan di server!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
