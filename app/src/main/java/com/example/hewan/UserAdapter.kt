package com.example.hewan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserAdapter(
    private val users: List<User>,
    private val onAction: (User, String) -> Unit // Callback untuk promote/demote
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    // ViewHolder untuk mengelola tampilan setiap item
    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvUserEmail: TextView = itemView.findViewById(R.id.tvUserEmail)
        val btnPromote: Button = itemView.findViewById(R.id.btnPromote)
        val btnDemote: Button = itemView.findViewById(R.id.btnDemote)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.tvUserName.text = user.name
        holder.tvUserEmail.text = user.email

        // Tombol Promote
        holder.btnPromote.setOnClickListener {
            onAction(user, "promote")
        }

        // Tombol Demote
        holder.btnDemote.setOnClickListener {
            onAction(user, "demote")
        }
    }

    override fun getItemCount(): Int = users.size
}
