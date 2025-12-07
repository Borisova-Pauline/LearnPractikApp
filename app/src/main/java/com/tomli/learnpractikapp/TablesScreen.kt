package com.tomli.learnpractikapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tomli.learnpractikapp.database.DynVM

@Composable
fun TablesScreen(navController: NavController, colVM: DynVM = viewModel(factory = DynVM.factory)){
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(bottom=innerPadding.calculateBottomPadding())){
            Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.tertiary)){
                Spacer(modifier= Modifier.fillMaxWidth().height(innerPadding.calculateTopPadding()).background(color= Color(0x27000000)))
                Text(text = "[Таблица]", color = Color.White, modifier = Modifier.fillMaxWidth().padding(15.dp),
                    textAlign = TextAlign.Center, fontSize = 22.sp)
            }
        }
    }
}