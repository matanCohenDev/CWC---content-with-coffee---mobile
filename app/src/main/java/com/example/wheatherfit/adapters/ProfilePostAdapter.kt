package com.example.wheatherfit.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wheatherfit.R
import com.example.wheatherfit.data.models.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class ProfilePostAdapter(private val postList: List<Post>, private val context: Context) :
  RecyclerView.Adapter<ProfilePostAdapter.ProfilePostViewHolder>() {

  class ProfilePostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val userName: TextView = view.findViewById(R.id.post_user_name_profile)
    val postImage: ImageView = view.findViewById(R.id.post_image_profile)
    val postDescription: TextView = view.findViewById(R.id.post_description_profile)
    val likeButton: ImageView = view.findViewById(R.id.like_button_profile)
    val likeCount: TextView = view.findViewById(R.id.like_count_profile)
    val postProfilePicture: ImageView = view.findViewById(R.id.profile_picture_in_post_profile)
    val deletePost: Button = view.findViewById(R.id.delete_button)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfilePostViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post_profile, parent, false)
    return ProfilePostViewHolder(view)
  }

  override fun onBindViewHolder(holder: ProfilePostViewHolder, position: Int) {
    val post = postList[position]
    val currentUser = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // טעינת תמונת הפוסט
    Glide.with(holder.itemView.context)
      .load(post.image_url)
      .into(holder.postImage)

    // הצגת תיאור הפוסט
    holder.postDescription.text = post.description

    // הבאת שם המשתמש מה-Firestore
    FirebaseFirestore.getInstance().collection("users")
      .document(post.user_id)
      .get()
      .addOnSuccessListener { document ->
        if (document.exists()) {
          val firstName = document.getString("firstname") ?: ""
          val lastName = document.getString("lastname") ?: ""
          val profileUrl = document.getString("profileImageUrl") ?: ""
          holder.userName.text = "$firstName $lastName"
          if (profileUrl.isNotEmpty()) {
            Picasso.get()
              .load(profileUrl)
              .placeholder(R.drawable.profile_foreground)
              .error(R.drawable.profile_foreground)
              .resize(200, 200)
              .centerCrop()
              .into(holder.postProfilePicture)
          }
        } else {
          holder.userName.text = "Unknown User"
        }
      }
      .addOnFailureListener {
        holder.userName.text = "Unknown User"
      }

    // הצגת מספר הלייקים
    holder.likeCount.text = post.likes.toString()

    // שינוי כפתור הלייק בהתאם למצב הנוכחי
    updateLikeUI(holder, post, currentUser)

    // לחיצה על כפתור הלייק
    holder.likeButton.setOnClickListener {
      toggleLike(holder, post, currentUser)
    }

    // מחיקת פוסט (רק אם המשתמש הנוכחי הוא הבעלים)
    holder.deletePost.setOnClickListener {
      FirebaseFirestore.getInstance().collection("posts").document(post.id).delete()
        .addOnSuccessListener {
          Toast.makeText(context, "Post Deleted!", Toast.LENGTH_SHORT).show()
          (postList as MutableList).removeAt(position)
          notifyItemRemoved(position)
        }
        .addOnFailureListener {
          Toast.makeText(context, "Failed to delete post", Toast.LENGTH_SHORT).show()
        }
    }
  }

  override fun getItemCount(): Int = postList.size

  private fun updateLikeUI(holder: ProfilePostViewHolder, post: Post, userId: String) {
    val isLiked = post.likedUsers.contains(userId)
    holder.likeButton.setImageResource(
      if (isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
    )
    holder.likeCount.text = post.likes.toString()
  }

  private fun toggleLike(holder: ProfilePostViewHolder, post: Post, userId: String) {
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
