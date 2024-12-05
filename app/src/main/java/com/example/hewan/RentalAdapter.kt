package com.example.hewan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RentalAdapter(
    private val rentals: List<Rental>
) : RecyclerView.Adapter<RentalAdapter.RentalViewHolder>() {

    class RentalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPetName: TextView = itemView.findViewById(R.id.tvPetName)
        val tvRentalDates: TextView = itemView.findViewById(R.id.tvRentalDates)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RentalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_rental, parent, false)
        return RentalViewHolder(view)
    }

    override fun onBindViewHolder(holder: RentalViewHolder, position: Int) {
        val rental = rentals[position]
        holder.tvPetName.text = rental.petId // Ganti dengan nama hewan jika tersedia
        holder.tvRentalDates.text = "Start: ${rental.startDate}, End: ${rental.endDate}"
    }

    override fun getItemCount(): Int {
        return rentals.size
    }
}
