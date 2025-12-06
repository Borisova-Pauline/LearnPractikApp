package com.tomli.learnpractikapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import com.tomli.learnpractikapp.ui.theme.LearnPractikAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LearnPractikAppTheme {
                MainScreen()
            }
        }
    }
}


/*@Composable
fun NavigScreens(
    val navController = 1
)*/