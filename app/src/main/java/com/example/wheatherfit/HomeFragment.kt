package com.example.wheatherfit

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.wheatherfit.adapters.PostAdapter
import com.example.wheatherfit.data.local.AppDatabase
import com.example.wheatherfit.data.local.User
import com.example.wheatherfit.data.models.Post
import com.example.wheatherfit.data.repository.UserRepository
import com.example.wheatherfit.viewmodel.PostViewModel
import com.example.wheatherfit.viewmodel.UserViewModel
import com.example.wheatherfit.viewmodel.UserViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
  private lateinit var userViewModel: UserViewModel
  private lateinit var recyclerView: RecyclerView
  private var postList = mutableListOf<Post>()
  private lateinit var postAdapter: PostAdapter
  private lateinit var swipeRefreshLayout: SwipeRefreshLayout
  private val postViewModel = PostViewModel()

  // פונקציה שמוסיפה את ה- BottomNavFragment
  fun renderNav(user: User) {
    Log.d("HomeFragment", "Rendering BottomNavFragment for user: ${user.firstname}")

    val childFragment = BottomNavFragment()
    val bundle = Bundle()
    bundle.putString("current_page", "home")
    bundle.putString("firstname", user.firstname)

    childFragment.arguments = bundle

    parentFragmentManager.beginTransaction()
      .replace(R.id.navbar_container, childFragment)
      .commit()
  }

  // טעינת פוסטים מ- Firestore
  private fun fetchPosts() {
    swipeRefreshLayout.isRefreshing = true

    FirebaseFirestore.getInstance().collection("posts")
      .orderBy("timestamp", Query.Direction.DESCENDING)
      .get()
      .addOnSuccessListener { documents ->
        postList.clear()
        for (document in documents) {
          val post = document.toObject(Post::class.java)
          postList.add(post)
        }
        postAdapter.notifyDataSetChanged()
        swipeRefreshLayout.isRefreshing = false
      }
      .addOnFailureListener { exception ->
        Log.e("HomeFragment", "Error fetching posts", exception)
        Toast.makeText(requireContext(), "Failed to load posts", Toast.LENGTH_SHORT).show()
        swipeRefreshLayout.isRefreshing = false
      }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    val view = inflater.inflate(R.layout.fragment_home, container, false)

    // אתחול SwipeRefreshLayout
    swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
    swipeRefreshLayout.setOnRefreshListener { fetchPosts() }

    // אתחול RecyclerView
    recyclerView = view.findViewById(R.id.recycler_view)
    recyclerView.layoutManager = LinearLayoutManager(requireContext())
    postAdapter = PostAdapter(postList, requireContext())
    recyclerView.adapter = postAdapter

    // יצירת ViewModel למשתמשים
    val userDao = AppDatabase.getDatabase(requireContext()).userDao()
    val repository = UserRepository(userDao)
    val factory = UserViewModelFactory(repository)
    userViewModel = ViewModelProvider(this, factory).get(UserViewModel::class.java)

    // קריאה לטעינת המשתמשים
    viewLifecycleOwner.lifecycleScope.launch {
      delay(1000) // השהייה קטנה כדי לוודא שהנתונים נטענים
      userViewModel.getUsers()
    }

    // מעקב אחרי השינויים ב- LiveData של המשתמשים
    userViewModel.users.observe(viewLifecycleOwner) { users ->
      Log.d("Home", "Users list: $users")
      if (users.isEmpty()) {
        Toast.makeText(requireActivity(), "Not logged in", Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.action_homeFragment_to_logoutFragment)
      } else {
        renderNav(users[0])
      }
    }

    // טעינת פוסטים מהשרת
    fetchPosts()

    return view
  }

  override fun onResume() {
    super.onResume()
    Log.d("HomeFragment", "onResume")
    fetchPosts()
  }
}
