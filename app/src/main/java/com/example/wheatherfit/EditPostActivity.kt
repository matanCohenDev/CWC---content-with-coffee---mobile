package com.example.wheatherfit.activities

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.wheatherfit.R
import com.example.wheatherfit.data.models.Post
import com.google.firebase.firestore.FirebaseFirestore
import java.io.Serializable

class EditPostActivity : AppCompatActivity() {

  private lateinit var ivPostImage: ImageView
  private lateinit var etDescription: EditText
  private lateinit var btnSave: Button
  private lateinit var btnCancel: Button

  private lateinit var post: Post

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.fragment_edit_post)


    ivPostImage = findViewById(R.id.ivPostImage)
    etDescription = findViewById(R.id.etDescription)
    btnSave = findViewById(R.id.btnSave)
    btnCancel = findViewById(R.id.btnCancel)

    // קבלת נתוני הפוסט מהאינטנט – ודא שמודל Post מממש Serializable או Parcelable
    post = intent.getSerializableExtra("post") as? Post ?: run {
      Toast.makeText(this, "No post data found", Toast.LENGTH_SHORT).show()
      finish()
      return
    }

    // טעינת תמונת הפוסט
    Glide.with(this)
      .load(post.image_url)
      .into(ivPostImage)

    // הצגת תיאור הפוסט הקיים
    etDescription.setText(post.description)

    btnSave.setOnClickListener {
      val updatedDescription = etDescription.text.toString().trim()
      if (updatedDescription.isEmpty()) {
        etDescription.error = "Description cannot be empty"
        return@setOnClickListener
      }
      updatePostDescription(updatedDescription)
    }

    btnCancel.setOnClickListener {
      finish()
    }
  }

  private fun updatePostDescription(newDescription: String) {
    FirebaseFirestore.getInstance().collection("posts")
      .document(post.id)
      .update("description", newDescription)
      .addOnSuccessListener {
        Toast.makeText(this, "Post updated", Toast.LENGTH_SHORT).show()
        // מחזירים תוצאה OK כדי שהמסך הקודם יתעדכן
        setResult(Activity.RESULT_OK)
        finish()
      }
      .addOnFailureListener { e ->
        Toast.makeText(this, "Failed to update post: ${e.message}", Toast.LENGTH_SHORT).show()
      }
  }
}
