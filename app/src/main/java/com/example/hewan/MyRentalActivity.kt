package com.example.hewan

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MyRentalActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var database: DatabaseReference
    private lateinit var rentalAdapter: MyRentalAdapter
    private val rentals: MutableList<Rental> = mutableListOf()

    companion object {
        private const val TAG = "MyRentalActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_rental)

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerViewMyRental)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Get the current user
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            Log.d(TAG, "Fetching rentals for user ID: ${currentUser.uid}")
            fetchUserRentals(currentUser.uid)
        } else {
            Log.e(TAG, "User is not logged in")
            Toast.makeText(this, "User is not logged in", Toast.LENGTH_SHORT).show()
        }

        // Set up button for rental history
        val btnMyRentalHistory: FloatingActionButton = findViewById(R.id.btnMyRentalHistory)
        btnMyRentalHistory.setOnClickListener {
            val intent = Intent(this, MyRentalHistoryActivity::class.java)
            startActivity(intent)
        }
    }

    private fun fetchUserRentals(userId: String) {
        // Reference to the rentals node in Firebase
        database = FirebaseDatabase.getInstance().getReference("rentals")

        database.orderByChild("userId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    rentals.clear() // Clear the list to avoid duplication

                    Log.d(TAG, "Snapshot size: ${snapshot.childrenCount}")
                    for (rentalSnapshot in snapshot.children) {
                        val rental = rentalSnapshot.getValue(Rental::class.java)
                        if (rental != null) {
                            Log.d(TAG, "Rental data retrieved: $rental")
                            rental.updateStatus() // Update status dynamically

                            if (rental.status == "Active") {
                                rentals.add(rental)
                                Log.d(TAG, "Rental added: $rental")
                            } else {
                                Log.d(TAG, "Rental is not active: $rental")
                            }
                        }
                    }

                    if (rentals.isEmpty()) {
                        Log.w(TAG, "No active rentals found")
                        Toast.makeText(this@MyRentalActivity, "No active rentals", Toast.LENGTH_SHORT).show()
                    }

                    updateRecyclerView()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Failed to fetch rentals: ${error.message}")
                    Toast.makeText(this@MyRentalActivity, "Failed to load rentals", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updateRecyclerView() {
        // Initialize or update the adapter
        if (!::rentalAdapter.isInitialized) {
            rentalAdapter = MyRentalAdapter(rentals)
            recyclerView.adapter = rentalAdapter
        } else {
            rentalAdapter.notifyDataSetChanged()
        }
    }

    private fun sendOverdueNotification(rental: Rental) {
        Log.w(TAG, "Rental overdue: ${rental.id}")
        Toast.makeText(this, "Rental ${rental.id} is overdue", Toast.LENGTH_SHORT).show()
    }
}
