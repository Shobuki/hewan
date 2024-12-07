package com.example.hewan

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MyRentalActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var database: DatabaseReference
    private lateinit var rentalAdapter: MyRentalAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_rental)

        recyclerView = findViewById(R.id.recyclerViewMyRental)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // Fetch rental data for current user
            fetchUserRentals(currentUser.uid)
        } else {
            Toast.makeText(this, "User is not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchUserRentals(userId: String) {
        // Reference to rentals node in Firebase
        database = FirebaseDatabase.getInstance().getReference("rentals")

        database.orderByChild("userId").equalTo(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rentals = mutableListOf<Rental>()
                for (rentalSnapshot in snapshot.children) {
                    val rental = rentalSnapshot.getValue(Rental::class.java)
                    if (rental != null) {
                        rentals.add(rental)
                    }
                }

                // Set up the adapter to show the rentals
                rentalAdapter = MyRentalAdapter(rentals)
                recyclerView.adapter = rentalAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MyRentalActivity, "Failed to load rentals", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
