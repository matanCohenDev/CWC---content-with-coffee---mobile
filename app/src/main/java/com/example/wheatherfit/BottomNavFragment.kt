package com.example.wheatherfit

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.wheatherfit.data.local.User
import com.google.android.material.bottomnavigation.BottomNavigationView

class BottomNavFragment : Fragment() {

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    // Inflate the layout for this fragment
    val view = inflater.inflate(R.layout.fragment_bottom_nav, container, false)
    val navigationBar = view.findViewById<BottomNavigationView>(R.id.bottom_navigation)
    val currentPage = arguments?.getString("current_page")

    if (currentPage == "home") {
      navigationBar.selectedItemId = R.id.home;
    }
    else if (currentPage == "profile") {
      navigationBar.selectedItemId = R.id.profile;
    }
    else if (currentPage == "upload") {
      navigationBar.selectedItemId = R.id.upload;
    }


    navigationBar.setOnItemSelectedListener { item: MenuItem ->
      when(item.itemId) {
        R.id.home -> {
          Log.d("BottomNavFragment", "Home button clicked")
          handleMove(currentPage, "home")
          true
        }
        R.id.profile -> {
          // Respond to navigation item 2 click
          Log.d("BottomNavFragment", "Profile button clicked")
          handleMove(currentPage, "profile")
          true
        }
        R.id.upload -> {
          // Respond to navigation item 2 click
          Log.d("BottomNavFragment", "Upload button clicked")
          handleMove(currentPage, "upload")
          true
        }
        R.id.logout -> {
          // Respond to navigation item 2 click
          Log.d("BottomNavFragment", "Logout button clicked")
          handleMove(currentPage, "logout")
          true
        }
        else -> false
      }
    }
    return view
  }

  fun handleMove(currentPage: String?, newPage: String) {
    val action = when (currentPage) {
      "home" -> when (newPage) {
        "profile" -> findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
        "upload" -> findNavController().navigate(R.id.action_homeFragment_to_uploadFragment)
        "logout" -> findNavController().navigate(R.id.action_homeFragment_to_logoutFragment)
        else -> null
      }
      "profile" -> when (newPage) {
        "home" -> findNavController().navigate(R.id.action_profileFragment_to_homeFragment)
        "upload" -> findNavController().navigate(R.id.action_profileFragment_to_uploadFragment)
        "logout" -> findNavController().navigate(R.id.action_profileFragment_to_logoutFragment)
        else -> null
      }
      "upload" -> when (newPage) {
        "home" -> findNavController().navigate(R.id.action_uploadFragment_to_homeFragment)
        "profile" -> findNavController().navigate(R.id.action_uploadFragment_to_profileFragment)
        "logout" -> findNavController().navigate(R.id.action_uploadFragment_to_logoutFragment)
        else -> null
      }
      else -> null
    }
  }


}
