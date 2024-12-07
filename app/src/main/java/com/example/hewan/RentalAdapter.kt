import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hewan.DetailRentalActivity
import com.example.hewan.Pet
import com.example.hewan.R

class RentalAdapter(
    private val pets: MutableList<Pet>
) : RecyclerView.Adapter<RentalAdapter.RentalViewHolder>() {

    class RentalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val petImage: ImageView = view.findViewById(R.id.petImage)
        val petName: TextView = view.findViewById(R.id.petName)
        val petCategory: TextView = view.findViewById(R.id.petCategory) // For category
        val petStatus: TextView = view.findViewById(R.id.petStatus)
        val petStock: TextView = view.findViewById(R.id.petStock)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RentalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_rental, parent, false)
        return RentalViewHolder(view)
    }

    override fun onBindViewHolder(holder: RentalViewHolder, position: Int) {
        val pet = pets[position]

        holder.petName.text = pet.name
        holder.petCategory.text = "Category: ${pet.category}" // Display category
        holder.petStatus.text = "Status: ${pet.status}"
        holder.petStock.text = "Stock: ${pet.stok}"
        Glide.with(holder.itemView.context)
            .load(pet.photo)
            .placeholder(R.drawable.placeholder_image)
            .into(holder.petImage)

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, DetailRentalActivity::class.java)
            intent.putExtra("petId", pet.id) // Add petId here
            intent.putExtra("petName", pet.name)
            intent.putExtra("petSpecies", pet.species)
            intent.putExtra("petCategory", pet.category)
            intent.putExtra("petPhoto", pet.photo)
            intent.putExtra("petDescription", pet.description)
            intent.putExtra("petStock", pet.stok)
            intent.putExtra("petStatus", pet.status)
            context.startActivity(intent)
            Log.d("RentalAdapter", "Mengirim Pet ke DetailRentalActivity: ID=${pet.id}, Name=${pet.name}")
        }
    }

    override fun getItemCount(): Int = pets.size

    fun updateData(newPets: List<Pet>) {
        pets.clear()
        pets.addAll(newPets)
        notifyDataSetChanged()
    }
}




