package com.tomli.learnpractikapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tomli.learnpractikapp.database.DynVM
import com.tomli.learnpractikapp.database.TableRow
import com.tomli.learnpractikapp.database.TableSchema
import com.tomli.learnpractikapp.ui.theme.LearnPractikAppTheme

@Composable
fun MainScreen(navController: NavController, colVM: DynVM = viewModel(factory = DynVM.factory)){
    var colls = colVM.collections.collectAsState(initial = emptyList())
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(bottom=innerPadding.calculateBottomPadding())){
            Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.tertiary)){
                Spacer(modifier=Modifier.fillMaxWidth().height(innerPadding.calculateTopPadding()).background(color=Color(0x27000000)))
                Box(modifier=Modifier.wrapContentHeight().fillMaxWidth()){
                    Text(text = "Коллекции", color = Color.White, modifier = Modifier.fillMaxWidth().padding(15.dp),
                        textAlign = TextAlign.Center, fontSize = 22.sp)
                    Image(painterResource(R.drawable.button_add), contentDescription = null,
                        modifier = Modifier.padding(15.dp).size(22.dp).align(Alignment.CenterEnd).clickable {
                            colVM.addCollection(name="Name",
                                value=listOf(TableRow(schema= TableSchema(tableName = "Table1", columns = mapOf("column1" to TableSchema.ColumnInfo(TableSchema.ColumnType.STRING, "Столбец1", 1))), data=mapOf("column1" to "val1", "column2" to "val2")),
                                    TableRow(schema= TableSchema(tableName = "Table2", columns = mapOf("column2" to TableSchema.ColumnInfo(TableSchema.ColumnType.STRING, "Столбец2", 2))), data=mapOf("column1" to "val3", "column2" to "val4"))),
                                schema=TableSchema(tableName = "TableOne", columns = mapOf("column1" to TableSchema.ColumnInfo(TableSchema.ColumnType.STRING, "Столбец1", 1), "column2" to TableSchema.ColumnInfo(TableSchema.ColumnType.STRING, "Столбец2", 2))))
                        })
                }
            }
            LazyVerticalGrid(columns = GridCells.Fixed(2),modifier = Modifier.padding(horizontal = 2.dp)){
                items(items = colls.value, key = {item -> item.id!!}){ item->
                    Box(modifier = Modifier.fillMaxWidth(1f).height(100.dp).background(color=Color(0xFF52249B)).clickable { navController.navigate("tablesScreen/${item.id}") },
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
        //MainScreen()
    }
}