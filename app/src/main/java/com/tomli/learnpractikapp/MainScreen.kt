package com.tomli.learnpractikapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tomli.learnpractikapp.database.DynVM
import com.tomli.learnpractikapp.ui.theme.LearnPractikAppTheme

@Composable
fun MainScreen(colVM: DynVM = viewModel(factory = DynVM.factory)){
    var colls = colVM.collections.collectAsState(initial = emptyList())
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(bottom=innerPadding.calculateBottomPadding())){
            Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.tertiary)){
                Spacer(modifier=Modifier.fillMaxWidth().height(innerPadding.calculateTopPadding()).background(color=Color(
                    0x27000000
                )
                ))
                Text(text = "Коллекции", color = Color.White, modifier = Modifier.fillMaxWidth().padding(15.dp),
                    textAlign = TextAlign.Center, fontSize = 22.sp)
            }
            LazyVerticalGrid(columns = GridCells.Fixed(2),modifier = Modifier.padding(horizontal = 2.dp)){
                items(items = colls.value, key = {item -> item.id!!}){ item->
                    Box(modifier = Modifier.fillMaxWidth(1f).height(100.dp).background(color=Color(0xFF52249B)),
                        contentAlignment = Alignment.Center){
                        Text(text=item.name!!)
                    }
                }
            }
        }
    }
}






@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LearnPractikAppTheme {
        MainScreen()
    }
}