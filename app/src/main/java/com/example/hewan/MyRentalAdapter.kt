package com.example.hewan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyRentalAdapter(private val rentals: List<Rental>) : RecyclerView.Adapter<MyRentalAdapter.MyRentalViewHolder>() {

    class MyRentalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rentalStartDate: TextView = view.findViewById(R.id.rentalStartDate)
        val rentalEndDate: TextView = view.findViewById(R.id.rentalEndDate)
        val petId: TextView = view.findViewById(R.id.petId)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyRentalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_rental_user, parent, false)
        return MyRentalViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyRentalViewHolder, position: Int) {
        val rental = rentals[position]

        holder.rentalStartDate.text = "Start Date: ${rental.startDate}"
        holder.rentalEndDate.text = "End Date: ${rental.endDate}"
        holder.petId.text = "Pet ID: ${rental.petId}"
    }

    override fun getItemCount(): Int = rentals.size
}
