package com.example.hewan

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class EncyclopediaActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchBar: EditText
    private lateinit var database: DatabaseReference
    private var petsList: MutableList<Pet> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_encyclopedia)

        // Inisialisasi
        recyclerView = findViewById(R.id.recyclerViewEncyclopedia)
        searchBar = findViewById(R.id.searchBar)
        database = FirebaseDatabase.getInstance().getReference("pets")

        // Load Data Ensiklopedia
        loadEncyclopediaData()

        // Search Functionality dengan TextWatcher
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterEncyclopediaData(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadEncyclopediaData() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                petsList.clear()
                for (petSnapshot in snapshot.children) {
                    val pet = petSnapshot.getValue(Pet::class.java)
                    if (pet != null) {
                        petsList.add(pet)
                    }
                }
                recyclerView.layoutManager = LinearLayoutManager(this@EncyclopediaActivity)
                recyclerView.adapter = PetAdapter(petsList) { pet ->
                    val intent = Intent(this@EncyclopediaActivity, DetailEncyclopediaActivity::class.java)
                    intent.putExtra("petId", pet.id)
                    startActivity(intent)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@EncyclopediaActivity, "Gagal memuat data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filterEncyclopediaData(query: String) {
        val filteredList = if (query.isEmpty()) {
            petsList
        } else {
            petsList.filter { it.name.contains(query, ignoreCase = true) }
        }
        recyclerView.adapter = PetAdapter(filteredList) { pet ->
            val intent = Intent(this@EncyclopediaActivity, DetailEncyclopediaActivity::class.java)
            intent.putExtra("petId", pet.id)
            startActivity(intent)
        }
    }
}
