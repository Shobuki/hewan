package com.example.hewan

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Logger
import com.google.firebase.database.ValueEventListener

object FirebaseHelper {
    private const val DATABASE_URL = "https://hewan-2a8e4-default-rtdb.asia-southeast1.firebasedatabase.app"

    // Mendapatkan referensi ke Realtime Database
    val databaseInstance: FirebaseDatabase by lazy {
        val instance = FirebaseDatabase.getInstance(DATABASE_URL)
        instance.setLogLevel(Logger.Level.DEBUG) // Aktifkan log level debug
        instance
    }

    // Mendapatkan referensi spesifik untuk path tertentu
    fun getReference(path: String): DatabaseReference {
        return databaseInstance.getReference(path)
    }

    // Fungsi untuk menguji koneksi ke Realtime Database
    fun testConnection() {
        val testRef = getReference("test_connection")
        val testData = mapOf(
            "timestamp" to System.currentTimeMillis(),
            "message" to "Connection successful"
        )

        testRef.setValue(testData)
            .addOnSuccessListener {
                Log.d("FirebaseHelper", "Test connection successful!")
            }
            .addOnFailureListener { error ->
                Log.e("FirebaseHelper", "Test connection failed: ${error.message}")
            }
    }

    fun monitorConnection() {
        val connectedRef = getReference(".info/connected")
        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    Log.d("FirebaseHelper", "Connected to Firebase Realtime Database.")
                } else {
                    Log.w("FirebaseHelper", "Disconnected from Firebase Realtime Database.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseHelper", "Connection monitoring cancelled: ${error.message}")
            }
        })
    }


    // Fungsi untuk mencatat log debugging
    fun enableDebugLogging() {
        databaseInstance.setLogLevel(Logger.Level.DEBUG)
        Log.d("FirebaseHelper", "Firebase debug logging enabled.")
    }
}
