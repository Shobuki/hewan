package com.example.hewan

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.awaitResponse


class EncyclopediaActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var petAdapter: PetAdapter
    private lateinit var searchBar: EditText
    private lateinit var btnPrevPage: Button
    private lateinit var btnNextPage: Button

    private val allPets = mutableListOf<Pet>() // List untuk menampung semua data dari API
    private var currentPage = 0 // Halaman awal
    private val pageSize = 10 // Jumlah item per jenis hewan

    // Cache sederhana untuk menyimpan hasil API berdasarkan halaman
    private val petCache = mutableMapOf<Int, List<Pet>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_encyclopedia)

        // Hubungkan ID dari XML
        recyclerView = findViewById(R.id.recyclerViewEncyclopedia)
        searchBar = findViewById(R.id.searchBar)
        btnPrevPage = findViewById(R.id.btnPrevPage)
        btnNextPage = findViewById(R.id.btnNextPage)

        // Atur RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        petAdapter = PetAdapter(emptyList()) { pet ->
            Toast.makeText(this, "Clicked: ${pet.name}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = petAdapter

        // Ambil data dari API atau Cache
        fetchPets()

        // Navigasi Halaman
        btnPrevPage.setOnClickListener {
            if (currentPage > 0) {
                currentPage--
                fetchPets()
            }
        }

        btnNextPage.setOnClickListener {
            currentPage++
            fetchPets()
        }

        // Pencarian
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                petAdapter.filter.filter(s)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun fetchPets() {
        // Cek apakah data sudah ada di cache
        if (petCache.containsKey(currentPage)) {
            Log.d("EncyclopediaActivity", "Fetching from cache for page $currentPage")
            val cachedPets = petCache[currentPage] ?: emptyList()
            allPets.clear()
            allPets.addAll(cachedPets)
            updateRecyclerView(allPets)
            return
        }

        // Jika tidak ada di cache, ambil data dari API
        Log.d("EncyclopediaActivity", "Fetching from API for page $currentPage")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val catResponse = ApiClient.catApi.getCatImages(limit = pageSize, page = currentPage).awaitResponse()
                val dogResponse = ApiClient.dogApi.getDogImages(limit = pageSize, page = currentPage).awaitResponse()

                if (catResponse.isSuccessful && dogResponse.isSuccessful) {
                    val cats = catResponse.body()?.map { apiResponse ->
                        Pet(
                            id = apiResponse.id,
                            name = apiResponse.breeds?.firstOrNull()?.name ?: "Unknown",
                            species = "Cat",
                            photo = apiResponse.url,
                            description = apiResponse.breeds?.firstOrNull()?.description
                        )
                    } ?: emptyList()

                    val dogs = dogResponse.body()?.map { apiResponse ->
                        Pet(
                            id = apiResponse.id,
                            name = apiResponse.breeds?.firstOrNull()?.name ?: "Unknown",
                            species = "Dog",
                            photo = apiResponse.url,
                            description = apiResponse.breeds?.firstOrNull()?.description
                        )
                    } ?: emptyList()

                    // Gabungkan hasil cat dan dog
                    val combinedPets = cats + dogs

                    // Simpan hasil ke cache
                    petCache[currentPage] = combinedPets

                    // Update UI di main thread
                    withContext(Dispatchers.Main) {
                        allPets.clear()
                        allPets.addAll(combinedPets)
                        updateRecyclerView(allPets)

                        // Update tombol navigasi
                        btnPrevPage.isEnabled = currentPage > 0
                        btnNextPage.isEnabled = combinedPets.size == (pageSize * 2)
                    }
                } else {
                    Log.e("API Response", "Failed to fetch pets")
                }
            } catch (e: Exception) {
                Log.e("API Error", "Error fetching pets", e)
            }
        }
    }

    private fun updateRecyclerView(pets: List<Pet>) {
        petAdapter = PetAdapter(pets) { pet ->
            Toast.makeText(this, "Clicked: ${pet.name}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = petAdapter
        petAdapter.notifyDataSetChanged()
        Log.d("EncyclopediaActivity", "RecyclerView updated with ${pets.size} items")
    }
}



