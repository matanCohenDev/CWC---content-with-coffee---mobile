package com.example.wheatherfit.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wheatherfit.R
import com.example.wheatherfit.data.models.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class PostAdapter(private val postList: List<Post>, private val context: Context) :
  RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

  class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val userName: TextView = view.findViewById(R.id.post_user_name)
    val postImage: ImageView = view.findViewById(R.id.post_image)
    val postDescription: TextView = view.findViewById(R.id.post_description)
    val likeButton: ImageView = view.findViewById(R.id.like_button)
    val likeCount: TextView = view.findViewById(R.id.like_count)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
    return PostViewHolder(view)
  }

  override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
    val post = postList[position]
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Load post image
    Glide.with(holder.itemView.context)
      .load(post.image_url)
      .into(holder.postImage)

    // Set description
    holder.postDescription.text = post.description

    // Fetch user name
    FirebaseFirestore.getInstance().collection("users")
      .document(post.user_id)
      .get()
      .addOnSuccessListener { document ->
        if (document.exists()) {
          val firstName = document.getString("firstname") ?: "Unknown"
          val lastName = document.getString("lastname") ?: ""
          holder.userName.text = "$firstName $lastName"
        } else {
          holder.userName.text = "Unknown User"
        }
      }

    // Set Like count
    holder.likeCount.text = post.likes.toString()

    // Update Like button state
    updateLikeUI(holder, post, currentUserId)

    // Like button click listener
    holder.likeButton.setOnClickListener {
      toggleLike(holder, post, currentUserId)
    }
  }

  override fun getItemCount(): Int = postList.size

  private fun updateLikeUI(holder: PostViewHolder, post: Post, userId: String) {
    val isLiked = post.likedUsers.contains(userId)
    holder.likeButton.setImageResource(
      if (isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
    )
    holder.likeCount.text = post.likes.toString()
  }

  private fun toggleLike(holder: PostViewHolder, post: Post, userId: String) {
    val db = FirebaseFirestore.getInstance().collection("posts").document(post.id)
    val isLiked = post.likedUsers.contains(userId)

    if (isLiked) {
      post.likedUsers.remove(userId)
      post.likes -= 1
    } else {
      post.likedUsers.add(userId)
      post.likes += 1
    }

    db.update("likes", post.likes, "likedUsers", post.likedUsers)
      .addOnSuccessListener {
        updateLikeUI(holder, post, userId)
      }
  }
}
