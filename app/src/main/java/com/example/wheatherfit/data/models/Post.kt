package com.example.wheatherfit.data.models

import com.google.firebase.firestore.DocumentId

data class Post(
  @DocumentId val id: String = "",
  val image_url: String = "",
  val description: String = "",
  val user_id: String = "",
  val timestamp: Long = 0,
  val weather: Double = 0.0,
  var likes: Int = 0,
  val likedUsers: MutableList<String> = mutableListOf()
)
