package com.tomli.learnpractikapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tomli.learnpractikapp.ui.theme.LearnPractikAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LearnPractikAppTheme {
                NavigScreens()
            }
        }
    }
}


@Composable
fun NavigScreens(){
    val navController = rememberNavController()
    NavHost(
        navController = navController, startDestination = "mainScreen"
    ){
        composable("mainScreen"){
            MainScreen(navController)
        }
        composable("tablesScreen"){
            TablesScreen(navController)
        }
    }
}