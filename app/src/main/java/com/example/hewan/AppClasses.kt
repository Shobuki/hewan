package com.example.hewan

import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

// Model Classes
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "user"
)

data class Pet(
    val id: String = "",
    val name: String = "",
    val species: String? = null,
    val category: String = "",
    val photo: String? = null,
    val description: String? = null,
    val stok: Int = 0,
    val status: String = "Ready" // Status ketersediaan hewan
)

data class PetApiResponse(
    val id: String,
    val url: String,
    val breeds: List<Breed>? = null
)

data class Breed(
    val id: String,
    val name: String,
    val description: String
)


data class Rental(
    val id: String = "",
    val petId: String = "",
    val userId: String = "",
    val startDate: String = "",
    val endDate: String = "",
)

// API Interface
interface PetApi {
    @Headers("x-api-key: live_vJqDS0PmM2zTWB58QRNnASfiqRJyPQaOUYIoz3aB7cnAKzPIHDrykNYZs3CZKcMS") // API Key Cat
    @GET("v1/images/search")
    fun getCatImages(
        @Query("limit") limit: Int = 20,
        @Query("page") page: Int = 0, // Halaman pertama
        @Query("has_breeds") hasBreeds: Int = 1
    ): Call<List<PetApiResponse>>

    @Headers("x-api-key: live_rrL8on1whavz1GIZF9GFIeQOcflj3R08KBQjV1xzZg9JvqjV0Lgs1MGJWtR89UpN") // API Key Dog
    @GET("v1/images/search")
    fun getDogImages(
        @Query("limit") limit: Int = 20,
        @Query("page") page: Int = 0, // Halaman pertama
        @Query("has_breeds") hasBreeds: Int = 1
    ): Call<List<PetApiResponse>>
}

// Retrofit Client
object ApiClient {
    private val okHttpClient = OkHttpClient()

    private val retrofitCat by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.thecatapi.com/") // Base URL untuk The Cat API
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    private val retrofitDog by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.thedogapi.com/") // Base URL untuk The Dog API
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    val catApi: PetApi by lazy {
        retrofitCat.create(PetApi::class.java)
    }

    val dogApi: PetApi by lazy {
        retrofitDog.create(PetApi::class.java)
    }
}
