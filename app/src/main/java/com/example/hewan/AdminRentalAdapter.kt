package com.example.hewan

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class AdminRentalAdapter(
    private val context: Context,
    private val pets: List<Pet>,
    private val onDelete: (Pet) -> Unit
) : RecyclerView.Adapter<AdminRentalAdapter.RentalViewHolder>() {

    class RentalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val petImage: ImageView = view.findViewById(R.id.petImage)
        val petName: TextView = view.findViewById(R.id.petName)
        val petCategory: TextView = view.findViewById(R.id.petCategory)
        val petStatus: TextView = view.findViewById(R.id.petStatus)
        val petStock: TextView = view.findViewById(R.id.petStock)
        val editButton: Button = view.findViewById(R.id.btnEdit) // Button for editing
        val deleteButton: Button = view.findViewById(R.id.btnDelete) // Button for deleting
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RentalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_rental, parent, false)
        return RentalViewHolder(view)
    }

    override fun onBindViewHolder(holder: RentalViewHolder, position: Int) {
        val pet = pets[position]

        holder.petName.text = pet.name
        holder.petCategory.text = "Category: ${pet.category}"
        holder.petStatus.text = "Status: ${pet.status}"
        holder.petStock.text = "Stock: ${pet.stok}"
        Glide.with(holder.petImage.context).load(pet.photo).into(holder.petImage)

        // Edit button
        holder.editButton.setOnClickListener {
            val intent = Intent(context, AdminDetailSettingRentalActivity::class.java)
            intent.putExtra("petId", pet.id)
            intent.putExtra("petName", pet.name)
            intent.putExtra("petSpecies", pet.species)
            intent.putExtra("petCategory", pet.category)
            intent.putExtra("petPhoto", pet.photo)
            intent.putExtra("petDescription", pet.description)
            intent.putExtra("petStock", pet.stok)
            intent.putExtra("petStatus", pet.status)
            Log.d("SendingData", "Sending Pet ID: ${pet.id}")
            context.startActivity(intent)
        }

        // Delete button
        holder.deleteButton.setOnClickListener {
            onDelete(pet)
        }
    }

    override fun getItemCount(): Int = pets.size
}
