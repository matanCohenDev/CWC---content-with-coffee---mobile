package com.example.wheatherfit.viewmodel

import android.graphics.BitmapFactory
import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wheatherfit.data.local.User
import com.example.wheatherfit.data.repository.UserRepository
import kotlinx.coroutines.launch

class UserViewModel(private val repository: UserRepository) : ViewModel() {

  private val _users = MutableLiveData<List<User>>()
  val users: LiveData<List<User>> get() = _users

  private val _user = MutableLiveData<User?>()
  val user: LiveData<User?> get() = _user

  private val _logoutStatus = MutableLiveData<Boolean>()
  val logoutStatus: LiveData<Boolean> get() = _logoutStatus

  // הוספת משתמש
  fun addUser(user: User) {
    viewModelScope.launch {
      repository.insertUser(user)
    }
  }

  // קבלת כל המשתמשים
  fun getUsers() {
    viewModelScope.launch {
      val userList = repository.getAllUsers()
      _users.value = userList
    }
  }

  // קבלת משתמש לפי ID עם LiveData
  fun getUser(id: String) {
    viewModelScope.launch {
      val retrievedUser = repository.getUser(id)
      _user.postValue(retrievedUser)
    }
  }

  // מחיקת משתמש
  fun deleteUser(user: User) {
    viewModelScope.launch {
      repository.deleteUser(user)
    }
  }

  // יציאה מהמערכת (logout)
  fun logout() {
    viewModelScope.launch {
      repository.logout()
      _logoutStatus.postValue(true)
    }
  }

  // טעינת תמונת פרופיל
  fun loadProfilePicture(userId: String, profileImage: ImageView) {
    user.observeForever { user ->
      user?.let {
        val imageBlob = it.imageBlob
        if (imageBlob != null) {
          val bitmap = BitmapFactory.decodeByteArray(imageBlob, 0, imageBlob.size)
          profileImage.setImageBitmap(bitmap)
        }
      }
    }
  }
}
