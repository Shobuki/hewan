package com.example.hewan

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase

class MyRentalAdapter(
    private val rentals: List<Rental>
) : RecyclerView.Adapter<MyRentalAdapter.RentalViewHolder>() {

    companion object {
        private const val TAG = "MyRentalAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RentalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_my_rental, parent, false)
        Log.d(TAG, "Layout inflated for position: $viewType")
        return RentalViewHolder(view)
    }

    override fun onBindViewHolder(holder: RentalViewHolder, position: Int) {
        val rental = rentals[position]
        Log.d(TAG, "Binding data for rental: ${rental.id} at position: $position")
        holder.bind(rental)
    }

    override fun getItemCount(): Int {
        val size = rentals.size
        Log.d(TAG, "Total rentals: $size")
        return size
    }

    class RentalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val rentalStartDate: TextView = itemView.findViewById(R.id.rentalStartDate)
        private val rentalEndDate: TextView = itemView.findViewById(R.id.rentalEndDate)
        private val petName: TextView = itemView.findViewById(R.id.petName) // Updated to show pet name
        private val rentalStatus: TextView = itemView.findViewById(R.id.rentalStatus)

        private val database = FirebaseDatabase.getInstance()

        fun bind(rental: Rental) {
            Log.d(TAG, "Binding rental: $rental")
            try {
                // Set Start and End Dates
                rentalStartDate.text = "Start Date: ${rental.startDate}"
                rentalEndDate.text = "End Date: ${rental.endDate}"

                // Fetch Pet Name using petId
                database.getReference("pets").child(rental.petId).get().addOnSuccessListener { snapshot ->
                    val petNameValue = snapshot.child("name").value as? String ?: "Unknown Pet"
                    petName.text = "Pet Name: $petNameValue"
                }.addOnFailureListener {
                    Log.e(TAG, "Failed to fetch pet name: ${it.message}")
                    petName.text = "Pet Name: Error"
                }

                // Set Rental Status with Color
                rentalStatus.text = "Status: ${rental.status}"
                when (rental.status) {
                    "Active" -> rentalStatus.setTextColor(itemView.context.getColor(android.R.color.holo_green_dark))
                    "Overdue" -> rentalStatus.setTextColor(itemView.context.getColor(android.R.color.holo_red_dark))
                    "Upcoming" -> rentalStatus.setTextColor(itemView.context.getColor(android.R.color.holo_orange_dark))
                    else -> rentalStatus.setTextColor(itemView.context.getColor(android.R.color.darker_gray))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error binding data: ${e.message}")
            }
        }
    }
}
