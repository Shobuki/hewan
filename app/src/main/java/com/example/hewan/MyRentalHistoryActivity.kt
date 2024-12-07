package com.example.hewan

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MyRentalHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var database: DatabaseReference
    private lateinit var rentalAdapter: MyRentalAdapter
    private val rentals: MutableList<Rental> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_rental_history)

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerViewMyRentalHistory)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            fetchRentalHistory(currentUser.uid)
        } else {
            Toast.makeText(this, "User is not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchRentalHistory(userId: String) {
        database = FirebaseDatabase.getInstance().getReference("rentals")

        database.orderByChild("userId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    rentals.clear() // Clear list to avoid duplicate entries
                    for (rentalSnapshot in snapshot.children) {
                        val rental = rentalSnapshot.getValue(Rental::class.java)
                        if (rental != null) {
                            rental.updateStatus() // Update rental status dynamically
                            rentals.add(rental)
                        }
                    }
                    updateRecyclerView()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MyRentalHistoryActivity, "Failed to fetch rentals", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updateRecyclerView() {
        if (!::rentalAdapter.isInitialized) {
            rentalAdapter = MyRentalAdapter(rentals) // Initialize adapter
            recyclerView.adapter = rentalAdapter
        } else {
            rentalAdapter.notifyDataSetChanged() // Update adapter if already initialized
        }
    }
}
