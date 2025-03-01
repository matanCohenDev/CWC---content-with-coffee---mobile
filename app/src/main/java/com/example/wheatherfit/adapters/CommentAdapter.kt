package com.example.wheatherfit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wheatherfit.R
import com.example.wheatherfit.data.models.Comment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CommentAdapter(private val commentList: List<Comment>) :
  RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

  class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val commentUserName: TextView = view.findViewById(R.id.comment_user_name)
    val commentText: TextView = view.findViewById(R.id.comment_text)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
    val view = LayoutInflater.from(parent.context)
      .inflate(R.layout.item_comment, parent, false)
    return CommentViewHolder(view)
  }

  override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
    val comment = commentList[position]
    holder.commentText.text = comment.text

    FirebaseFirestore.getInstance().collection("users")
      .document(comment.userId)
      .get()
      .addOnSuccessListener { document ->
        if (document.exists()) {
          val firstName = document.getString("firstname") ?: ""
          val lastName = document.getString("lastname") ?: ""
          holder.commentUserName.text = "$firstName $lastName"
        } else {
          holder.commentUserName.text = "Unknown User"
        }
      }
      .addOnFailureListener {
        holder.commentUserName.text = "Unknown User"
      }
  }


  override fun getItemCount(): Int = commentList.size
}
