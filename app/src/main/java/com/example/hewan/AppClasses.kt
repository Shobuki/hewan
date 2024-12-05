package com.example.hewan

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


// **Model Classes**
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "user"
)

data class Pet(
    val id: String = "",
    val name: String = "",
    val species: String = "",
    val photoUrl: String = "",
    val description: String = "",
    val status: String = "available",
    val stok: Int = 0
)

data class Rental(
    val id: String = "",
    val petId: String = "",
    val userId: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val status: String = "pending"
)






// **DetailRentalActivity**


// **SettingUserActivity**
class SettingUserActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        loadUserProfile()
    }

    private fun loadUserProfile() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val dbRef = FirebaseDatabase.getInstance().getReference("users")
        dbRef.child(currentUser.uid).get()
            .addOnSuccessListener { snapshot ->
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    Toast.makeText(this, "User: ${user.name}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Data pengguna tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memuat profil pengguna", Toast.LENGTH_SHORT).show()
            }
    }

}

// **PetAdapter**
class PetAdapter(
    private val pets: List<Pet>,
    private val onClick: ((Pet) -> Unit)?
) : RecyclerView.Adapter<PetAdapter.PetViewHolder>() {

    class PetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvPetName: TextView = itemView.findViewById(R.id.tvPetName)
        private val tvPetSpecies: TextView = itemView.findViewById(R.id.tvPetSpecies)

        fun bind(pet: Pet, onClick: ((Pet) -> Unit)?) {
            tvPetName.text = pet.name
            tvPetSpecies.text = pet.species
            itemView.setOnClickListener { onClick?.invoke(pet) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pet, parent, false)
        return PetViewHolder(view)
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        holder.bind(pets[position], onClick)
    }

    override fun getItemCount() = pets.size
}
