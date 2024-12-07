package com.example.hewan

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PetAdapter(
    private val pets: List<Pet>,
    private val onClick: ((Pet) -> Unit)?
) : RecyclerView.Adapter<PetAdapter.PetViewHolder>(), Filterable {

    private var filteredPets: List<Pet> = pets

    class PetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvPetName: TextView = itemView.findViewById(R.id.tvPetName)
        private val tvPetSpecies: TextView = itemView.findViewById(R.id.tvPetSpecies)
        private val ivPetPhoto: ImageView = itemView.findViewById(R.id.ivPetPhoto)

        fun bind(pet: Pet, onClick: ((Pet) -> Unit)?) {
            tvPetName.text = pet.name
            tvPetSpecies.text = pet.species
            Log.d("PetAdapter", "Binding pet: ${pet.name}, Photo: ${pet.photo}")

            Glide.with(itemView.context)
                .load(pet.photo)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(ivPetPhoto)

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, DetailRentalActivity::class.java).apply {
                    putExtra("petId", pet.id)
                    putExtra("petName", pet.name)
                    putExtra("petSpecies", pet.species)
                    putExtra("petCategory", pet.category)
                    putExtra("petPhoto", pet.photo)
                    putExtra("petDescription", pet.description)
                    putExtra("petStock", pet.stok)
                    putExtra("petStatus", pet.status)
                }
                Log.d("PetAdapter", "Mengirim data Pet ke DetailRentalActivity: ID=${pet.id}")
                itemView.context.startActivity(intent)
            }



        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pet, parent, false)
        return PetViewHolder(view)
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        holder.bind(filteredPets[position], onClick)
    }

    override fun getItemCount() = filteredPets.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = if (constraint.isNullOrEmpty()) {
                    pets
                } else {
                    val filterPattern = constraint.toString().lowercase().trim()
                    pets.filter {
                        it.name.lowercase().contains(filterPattern) ||
                                (it.species?.lowercase()?.contains(filterPattern) ?: false)
                    }
                }

                return FilterResults().apply { values = filteredList }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredPets = results?.values as List<Pet>
                notifyDataSetChanged()
            }
        }
    }
}
