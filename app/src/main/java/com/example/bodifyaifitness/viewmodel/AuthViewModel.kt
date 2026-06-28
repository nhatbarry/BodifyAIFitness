package com.example.bodifyaifitness.viewmodel

import android.os.Message
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class AuthViewModel : ViewModel(){
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init {
        checkAuthStatus()
    }
    fun checkAuthStatus(){
        if (auth.currentUser==null){
            _authState.value = AuthState.Unauthenticated
        }
        else{
            _authState.value = AuthState.Authenticated
        }
    }

    fun login(email:String, password:String){

        if(email.isEmpty() || password.isEmpty()){
            _authState.value = AuthState.Error("Email và mật khẩu không được trống")
            return
        }

        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener{task->
                if(task.isSuccessful){
                    _authState.value = AuthState.Authenticated
                }
                else{
                    _authState.value = AuthState.Error(task.exception?.message?:"Something went wrong")
                }
            }
    }

    fun resetPassword(email: String, onResult: (Boolean, String) -> Unit) {
        if (email.isEmpty()) {
            onResult(false, "Vui lòng nhập email")
            return
        }

        _authState.value = AuthState.Loading
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Unauthenticated // Reset về trạng thái chờ
                    onResult(true, "Link đặt lại mật khẩu đã được gửi vào Email của bạn!")
                } else {
                    val errorMsg = task.exception?.message ?: "Gửi email thất bại"
                    _authState.value = AuthState.Error(errorMsg)
                    onResult(false, errorMsg)
                }
            }
    }

    fun signUp(email:String, password:String){

        if(email.isEmpty() || password.isEmpty()){
            _authState.value = AuthState.Error("Email và mật khẩu không được trống")
            return
        }

        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener{task->
                if(task.isSuccessful){
                    _authState.value = AuthState.Authenticated
                }
                else{
                    _authState.value = AuthState.Error(task.exception?.message?:"Something went wrong")
                }
            }
    }

    fun signOut(){
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }

}

sealed class AuthState{
    object Authenticated: AuthState()
    object Unauthenticated: AuthState()
    object Loading: AuthState()
    data class Error(val message: String): AuthState()
}