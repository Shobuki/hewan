package com.example.hewan

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class AdminListUserActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_list_user)

        // Inisialisasi RecyclerView dan Realtime Database
        recyclerView = findViewById(R.id.recyclerViewAdminUsers)
        recyclerView.layoutManager = LinearLayoutManager(this)
        database = FirebaseDatabase.getInstance().getReference("users")

        // Load Data User
        loadUserList()
    }

    private fun loadUserList() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = mutableListOf<User>()
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    if (user != null) {
                        users.add(user)
                    }
                }
                recyclerView.adapter = UserAdapter(users) { user, action ->
                    when (action) {
                        "promote" -> updateUserRole(user.id, "admin")
                        "demote" -> updateUserRole(user.id, "user")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@AdminListUserActivity,
                    "Gagal memuat daftar pengguna: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun updateUserRole(userId: String, newRole: String) {
        val userRef = database.child(userId)
        userRef.child("role").setValue(newRole)
            .addOnSuccessListener {
                Toast.makeText(this, "Role diperbarui menjadi $newRole", Toast.LENGTH_SHORT).show()
                loadUserList() // Refresh list
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memperbarui role: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
