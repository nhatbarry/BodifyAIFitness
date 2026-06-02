package com.example.bodifyaifitness.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bodifyaifitness.database.FirebaseManager
import com.example.bodifyaifitness.dataclass.User
import com.google.firebase.auth.FirebaseAuth

class UserViewModel : ViewModel() {

    private val firebaseManager = FirebaseManager()
    private val auth = FirebaseAuth.getInstance()

    private val _userState = MutableLiveData<UserProfileState>(UserProfileState.Loading)
    val userState: LiveData<UserProfileState> = _userState

    fun loadUserProfile() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _userState.value = UserProfileState.NotFound
            return
        }
        _userState.value = UserProfileState.Loading
        firebaseManager.getUserProfile(
            userId = uid,
            onSuccess = { user ->
                if (user != null) {
                    _userState.value = UserProfileState.Success(user)
                } else {
                    _userState.value = UserProfileState.NotFound
                }
            },
            onFailure = { e ->
                _userState.value = UserProfileState.Error(e.message ?: "Unknown error")
            }
        )
    }

    fun saveUserProfile(user: User, onDone: () -> Unit = {}) {
        firebaseManager.saveUserProfile(
            user = user,
            onSuccess = {
                _userState.value = UserProfileState.Success(user)
                onDone()
            },
            onFailure = { e ->
                _userState.value = UserProfileState.Error(e.message ?: "Save failed")
            }
        )
    }

    /** Returns BMI value or null when height/weight not set. */
    fun calculateBmi(user: User): Float? {
        if (user.height <= 0f || user.weight <= 0f) return null
        val heightM = user.height / 100f   // cm → m
        return user.weight / (heightM * heightM)
    }
}

sealed class UserProfileState {
    object Loading : UserProfileState()
    object NotFound : UserProfileState()
    data class Success(val user: User) : UserProfileState()
    data class Error(val message: String) : UserProfileState()
}
