package com.example.wheatherfit

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.wheatherfit.adapters.ProfilePostAdapter
import com.example.wheatherfit.data.local.AppDatabase
import com.example.wheatherfit.data.models.Post
import com.example.wheatherfit.data.repository.UserRepository
import com.example.wheatherfit.viewmodel.UserViewModel
import com.example.wheatherfit.viewmodel.UserViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {
  private lateinit var profilePostAdapter: ProfilePostAdapter
  private lateinit var recyclerView: RecyclerView
  private lateinit var swipeRefreshLayout: SwipeRefreshLayout
  private var postList = mutableListOf<Post>()

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val view = inflater.inflate(R.layout.fragment_profile, container, false)

    val childFragment = BottomNavFragment()
    val bundle = Bundle()
    bundle.putString("current_page", "profile")

    val profileImage = view.findViewById<ImageView>(R.id.profile_picture)
    loadProfilePicture(profileImage)

    val editProfilePictureText = view.findViewById<TextView>(R.id.edit_profile_picture_text)
    editProfilePictureText.setOnClickListener {
      openGallery()
      loadProfilePicture(profileImage)
    }

    // Set up SwipeRefreshLayout
    swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout_profile)
    swipeRefreshLayout.setOnRefreshListener {
      fetchPosts()
    }

    // Setup RecyclerView
    recyclerView = view.findViewById(R.id.recycler_view_profile)
    recyclerView.layoutManager = LinearLayoutManager(requireContext())
    profilePostAdapter = ProfilePostAdapter(postList, requireContext())
    recyclerView.adapter = profilePostAdapter

    childFragment.arguments = bundle
    fetchPosts()

    childFragmentManager.beginTransaction()
      .replace(R.id.navbar_container, childFragment)
      .commit()

    return view
  }

  val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
    uri?.let {
      uploadProfilePicture(it)
    }
  }

  fun openGallery() {
    pickImageLauncher.launch("image/*")
  }

  fun uploadProfilePicture(imageUri: Uri) {
    val storageRef = FirebaseStorage.getInstance().reference
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val profileImageRef = storageRef.child("profile_pictures/$userId.jpg")

    profileImageRef.putFile(imageUri)
      .addOnSuccessListener {
        profileImageRef.downloadUrl.addOnSuccessListener { downloadUri ->
          saveImageUrlToFirestore(downloadUri.toString())
        }
        Toast.makeText(requireContext(), "Profile Picture Uploaded!", Toast.LENGTH_SHORT).show()
      }
      .addOnFailureListener {
        Toast.makeText(requireContext(), "Upload Failed: ${it.message}", Toast.LENGTH_SHORT).show()
      }
  }

  fun saveImageUrlToFirestore(imageUrl: String) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val db = FirebaseFirestore.getInstance()

    db.collection("users").document(userId!!)
      .update("profileImageUrl", imageUrl)
      .addOnSuccessListener {
        Toast.makeText(requireContext(), "Profile Updated!", Toast.LENGTH_SHORT).show()
      }
      .addOnFailureListener {
        Toast.makeText(requireContext(), "Failed to update profile: ${it.message}", Toast.LENGTH_SHORT).show()
      }
  }

  private fun loadProfilePicture(profileImage: ImageView) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val db = FirebaseFirestore.getInstance()

    val userDao = AppDatabase.getDatabase(requireContext()).userDao()
    val repository = UserRepository(userDao)
    val factory = UserViewModelFactory(repository)
    val userViewModel = ViewModelProvider(this, factory).get(UserViewModel::class.java)

    userViewModel.user.observe(viewLifecycleOwner) { user ->
      if (user != null) {
        val imageBlob = user.imageBlob
        if (imageBlob != null) {
          val bitmap = BitmapFactory.decodeByteArray(imageBlob, 0, imageBlob.size)
          profileImage.setImageBitmap(bitmap)
        }
      }
    }
  }

  private fun fetchPosts() {
    swipeRefreshLayout.isRefreshing = true

    FirebaseFirestore.getInstance().collection("posts")
      .whereEqualTo("user_id", FirebaseAuth.getInstance().currentUser?.uid)
      .orderBy("timestamp", Query.Direction.DESCENDING)
      .get()
      .addOnSuccessListener { documents ->
        postList.clear()
        for (document in documents) {
          val post = document.toObject(Post::class.java)
          postList.add(post)
        }
        profilePostAdapter.notifyDataSetChanged()
        swipeRefreshLayout.isRefreshing = false
      }
      .addOnFailureListener {
        Log.e("ProfileFragment", "Error fetching posts", it)
        Toast.makeText(requireContext(), "Failed to load posts", Toast.LENGTH_SHORT).show()
        swipeRefreshLayout.isRefreshing = false
      }
  }
}
