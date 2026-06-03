package com.example.bodifyaifitness

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.bodifyaifitness.composable.AppNavigation
import com.example.bodifyaifitness.ui.theme.BodifyAIFitnessTheme
import com.example.bodifyaifitness.viewmodel.AuthViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val authViewModel: AuthViewModel by viewModels()
        setContent {
            BodifyAIFitnessTheme {
                AppNavigation(Modifier.fillMaxSize(), authViewModel = authViewModel)
            }
        }
    }
}
