package com.tomli.learnpractikapp

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tomli.learnpractikapp.database.DynVM
import com.tomli.learnpractikapp.database.TableRow
import com.tomli.learnpractikapp.database.TableSchema
import com.tomli.learnpractikapp.ui.theme.LearnPractikAppTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(navController: NavController, colVM: DynVM = viewModel(factory = DynVM.factory)){
    var colls = colVM.collections.collectAsState(initial = emptyList())
    val isCreateCollection= remember { mutableStateOf(false) }
    val isUpdateCollection = remember { mutableStateOf(false) }
    val itemId= remember { mutableStateOf(0) }
    val itemName=remember { mutableStateOf("") }
    val isDeleteCollection =remember { mutableStateOf(false) }
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(bottom=innerPadding.calculateBottomPadding())){
            Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.tertiary)){
                Spacer(modifier=Modifier.fillMaxWidth().height(innerPadding.calculateTopPadding()).background(color=Color(0x27000000)))
                Box(modifier=Modifier.wrapContentHeight().fillMaxWidth()){
                    Text(text = "Коллекции", color = Color.White, modifier = Modifier.fillMaxWidth().padding(15.dp),
                        textAlign = TextAlign.Center, fontSize = 22.sp)
                    Image(painterResource(R.drawable.button_add), contentDescription = null,
                        modifier = Modifier.padding(15.dp).size(22.dp).align(Alignment.CenterEnd).clickable {
                            isCreateCollection.value=true })
                }
            }
            LazyVerticalGrid(columns = GridCells.Fixed(2),modifier = Modifier.padding(5.dp)){
                items(items = colls.value, key = {item -> item.id!!}){ item->
                    val showDropDown= remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth(1f).padding(5.dp).height(100.dp).background(color=Color(0xFF52249B), shape = RoundedCornerShape(5.dp))/*.clickable { navController.navigate("tablesScreen/${item.id}") }*/
                        .combinedClickable(enabled = true, onClick = {
                            navController.navigate("tablesScreen/${item.id}")
                    },onLongClick = {
                        showDropDown.value=true; itemId.value=item.id!!; itemName.value=item.name!!
                    }),
                        contentAlignment = Alignment.Center){
                        Text(text=item.name!!, color=Color.White)
                        DropdownMenu(expanded = showDropDown.value, onDismissRequest = { showDropDown.value = false }) {
                            DropdownMenuItem(text = { Text("Редактировать") },
                                onClick = {isUpdateCollection.value=true ; showDropDown.value= false })
                            DropdownMenuItem(text = { Text("Удалить") },
                                onClick = { isDeleteCollection.value=true; showDropDown.value= false })
                        }
                    }
                }
            }
        }
        if(isCreateCollection.value){
            CreateCollection({isCreateCollection.value=false})
        }
        if(isUpdateCollection.value){
            UpdateCollection({isUpdateCollection.value=false}, itemId.value, itemName.value)
        }
        if(isDeleteCollection.value){
            AlertDialog(onDismissRequest = {isDeleteCollection.value=false},
                title = {Text(text="Удалить коллекцию?")},
                text = {Text(text="Все записи внутри неё будут также удалены")},
                confirmButton = {Text(text = "Удалить", modifier = Modifier.padding(5.dp)
                    .clickable {
                        colVM.deleteCollection(itemId.value)
                        isDeleteCollection.value=false})},
                dismissButton = {Text(text = "Отменить", modifier = Modifier.padding(5.dp)
                    .clickable { isDeleteCollection.value=false})})
        }
    }
}

@Composable
fun CreateCollection(onDismiss:()->Unit, colVM: DynVM = viewModel(factory = DynVM.factory)){
    val name = remember { mutableStateOf("") }
    val columns = remember { mutableStateOf("1") }
    val rows =remember { mutableStateOf("0") }
    Dialog(onDismiss){
        Card(modifier=Modifier.padding(horizontal = 30.dp)){
            Column(modifier=Modifier.padding(15.dp)) {
                Text(text="Новая коллекция")
                OutlinedTextField(value=name.value, onValueChange = {text-> name.value=text},
                    placeholder={ Text(text="Введите название", color=Color.Gray)},
                    label={Text(text="Название коллекции")})
                OutlinedTextField(value=columns.value, onValueChange = {text-> columns.value=text}, placeholder={
                    Text(text="Введите число больше 0", color=Color.Gray)
                }, label={Text(text="Количество столбцов")}, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value=rows.value, onValueChange = {text-> rows.value=text}, placeholder={
                    Text(text="Введите число >=0", color=Color.Gray)
                }, label={Text(text="Количество строк")}, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                Text(text="Создать", textAlign = TextAlign.Center, modifier=Modifier.padding(vertical = 30.dp).fillMaxWidth().clickable {
                    val schema = createSchemaOnCreating(name.value, columns.value.toInt())
                    colVM.addCollection(name=name.value,
                        value=createValueOnCreating(schema, rows.value.toInt()),
                        schema=schema)
                    onDismiss() })
            }
        }
    }
}

@Composable
fun UpdateCollection(onDismiss: () -> Unit, id: Int, origName: String, colVM: DynVM = viewModel(factory = DynVM.factory)){
    val name = remember { mutableStateOf(origName) }
    Dialog(onDismiss){
        Card(modifier=Modifier.padding(horizontal = 30.dp)){
            Column(modifier=Modifier.padding(15.dp)){
                Text(text="Редактирование коллекции")
                OutlinedTextField(value=name.value, onValueChange = {text-> name.value=text},
                    placeholder={ Text(text="Введите название", color=Color.Gray)},
                    label={Text(text="Название коллекции")})
                Text(text="Сохранить", textAlign = TextAlign.Center, modifier=Modifier.padding(vertical = 30.dp).fillMaxWidth().clickable {
                    colVM.setNewName(name.value, id)
                    onDismiss() })
            }
        }
    }
}


fun createValueOnCreating(schema: TableSchema, rowCount: Int): List<TableRow>{
    var valueList= mutableListOf<TableRow>()

    for(i in 0..rowCount-1){
        var data = mutableMapOf<String, String>()
        schema.columns.keys.forEach{ key->
            data.put(key, "")
        }
        valueList.add(TableRow(schema, data))
    }

    return valueList
}


fun createSchemaOnCreating(name: String, columnCount: Int): TableSchema{
    val columns = mutableMapOf<String, TableSchema.ColumnInfo>()
    for(i in 0..columnCount-1){
        columns.put("column$i", TableSchema.ColumnInfo(TableSchema.ColumnType.STRING, "column${i+1}", i))
    }

    return TableSchema(tableName = name, columns = columns)
}