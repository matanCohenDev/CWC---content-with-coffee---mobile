package com.example.wheatherfit.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wheatherfit.R
import com.example.wheatherfit.data.models.Comment
import com.example.wheatherfit.data.models.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PostAdapter(
  private val postList: MutableList<Post>,
  private val context: Context
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

  class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val userName: TextView = view.findViewById(R.id.post_user_name)
    val postImage: ImageView = view.findViewById(R.id.post_image)
    val postDescription: TextView = view.findViewById(R.id.post_description)
    val likeButton: ImageView = view.findViewById(R.id.like_button)
    val likeCount: TextView = view.findViewById(R.id.like_count)
    // רכיבי תגובות
    val etComment: EditText = view.findViewById(R.id.etComment)
    val btnSendComment: Button = view.findViewById(R.id.btnSendComment)
    val rvComments: RecyclerView = view.findViewById(R.id.rvComments)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
    val view = LayoutInflater.from(parent.context)
      .inflate(R.layout.item_post, parent, false)
    return PostViewHolder(view)
  }

  override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
    val post = postList[position]
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // טעינת תמונת הפוסט
    Glide.with(holder.itemView.context)
      .load(post.image_url)
      .into(holder.postImage)

    // הצגת תיאור הפוסט
    holder.postDescription.text = post.description

    // קבלת שם המשתמש מ-Firestore
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
      .addOnFailureListener {
        holder.userName.text = "Unknown User"
      }

    // הצגת מספר הלייקים
    holder.likeCount.text = post.likes.toString()

    // עדכון מצב כפתור הלייק
    updateLikeUI(holder, post, currentUserId)

    // מאזין ללחיצה על כפתור הלייק
    holder.likeButton.setOnClickListener {
      toggleLike(holder, post, currentUserId)
    }

    // סעיף 4: טיפול בלחיצה על כפתור "שלח תגובה"
    holder.btnSendComment.setOnClickListener {
      val commentText = holder.etComment.text.toString().trim()
      if (commentText.isEmpty()) {
        Toast.makeText(context, "אנא הכנס תגובה", Toast.LENGTH_SHORT).show()
        return@setOnClickListener
      }
      // יצירת תגובה חדשה
      val newComment = Comment(
        id = FirebaseFirestore.getInstance().collection("posts").document().id,
        userId = currentUserId,
        text = commentText,
        timestamp = System.currentTimeMillis()
      )
      // עדכון רשימת התגובות במסד הנתונים (למשל, ב-Firestore)
      val updatedComments = post.comments.toMutableList().apply { add(newComment) }
      FirebaseFirestore.getInstance().collection("posts")
        .document(post.id)
        .update("comments", updatedComments)
        .addOnSuccessListener {
          Toast.makeText(context, "תגובה נשלחה", Toast.LENGTH_SHORT).show()
          holder.etComment.text.clear()
          // עדכון רשימת התגובות המקומית והצגתן
          post.comments = updatedComments
          holder.rvComments.adapter = CommentAdapter(updatedComments)
        }
        .addOnFailureListener {
          Toast.makeText(context, "שגיאה בשליחת תגובה", Toast.LENGTH_SHORT).show()
        }
    }

    // סעיף 5: הצגת רשימת התגובות (אם קיימות)
    holder.rvComments.layoutManager = LinearLayoutManager(context)
    holder.rvComments.adapter = CommentAdapter(post.comments)
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
