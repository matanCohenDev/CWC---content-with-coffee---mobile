package com.example.wheatherfit

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class UploadFragment : Fragment() {
  private var selectedImageUri: Uri? = null
  private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
  private val auth: FirebaseAuth = FirebaseAuth.getInstance()

  private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
    uri?.let {
      selectedImageUri = it
      Toast.makeText(requireContext(), "Image selected", Toast.LENGTH_SHORT).show()
    }
  }

  private fun openGallery() {
    pickImageLauncher.launch("image/*")
  }

  private fun uploadPost(imageUri: Uri, description: String) {
    val userId = auth.currentUser?.uid ?: return
    val postImageRef = FirebaseStorage.getInstance().reference.child("posts/$userId-${System.currentTimeMillis()}.jpg")

    postImageRef.putFile(imageUri)
      .addOnSuccessListener {
        postImageRef.downloadUrl.addOnSuccessListener { downloadUri ->
          savePostToFirestore(downloadUri.toString(), description)
          Toast.makeText(requireContext(), "Post Uploaded!", Toast.LENGTH_SHORT).show()

          // ✅ מונע קריסה - בודק אם `findNavController()` אפשרי
          if (isAdded && view != null) {
            try {
              findNavController().navigateUp()
            } catch (e: Exception) {
              Log.e("UploadFragment", "Navigation failed: ${e.message}")
            }
          }
        }
      }
      .addOnFailureListener {
        Toast.makeText(requireContext(), "Upload Failed: ${it.message}", Toast.LENGTH_SHORT).show()
      }
  }

  private fun savePostToFirestore(imageUrl: String, description: String) {
    val userId = auth.currentUser?.uid ?: return

    val post = hashMapOf(
      "user_id" to userId,
      "image_url" to imageUrl,
      "description" to description,
      "timestamp" to System.currentTimeMillis(),
      "likes" to 0,
      "likedUsers" to emptyList<String>(),
      "comments" to emptyList<String>()
    )

    db.collection("posts").add(post)
      .addOnSuccessListener {
        context?.let { ctx -> // ✅ בודק אם ה-Fragment עדיין קיים
          Toast.makeText(ctx, "Post Shared!", Toast.LENGTH_SHORT).show()
        }
      }
      .addOnFailureListener {
        context?.let { ctx ->
          Toast.makeText(ctx, "Failed to share post: ${it.message}", Toast.LENGTH_SHORT).show()
        }
      }
  }


  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    val view = inflater.inflate(R.layout.fragment_upload, container, false)

    // ✅ הוספת כפתור חזרה למעלה בלי להגדיר ב- XML
    setHasOptionsMenu(true)
    (activity as AppCompatActivity).supportActionBar?.apply {
      setDisplayHomeAsUpEnabled(true)
      title = "Upload Post" // אפשר לשנות את הכותרת אם צריך
    }

    view.findViewById<Button>(R.id.upload_button).setOnClickListener {
      openGallery()
    }

    view.findViewById<Button>(R.id.share).setOnClickListener {
      val descriptionInput = view.findViewById<TextInputLayout>(R.id.description).editText?.text.toString()
      if (selectedImageUri == null) {
        Toast.makeText(requireContext(), "Please select an image first", Toast.LENGTH_SHORT).show()
      } else {
        uploadPost(selectedImageUri!!, descriptionInput)
      }
    }

    return view
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      android.R.id.home -> {
        findNavController().navigateUp() // כפתור חזרה לוחץ Back
        true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    // ✅ מסיר את כפתור החזרה כשהמשתמש יוצא מהמסך
    (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
  }
}
