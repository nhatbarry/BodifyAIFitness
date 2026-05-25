package com.example.bodifyaifitness

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.bodifyaifitness.composable.NavBar
import com.example.bodifyaifitness.ui.theme.BodifyAIFitnessTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BodifyAIFitnessTheme {
                NavBar()
            }
        }
    }
}