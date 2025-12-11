package com.tomli.learnpractikapp

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tomli.learnpractikapp.database.DynVM
import com.tomli.learnpractikapp.database.DynamicTableRepository
import com.tomli.learnpractikapp.database.TableManager
import com.tomli.learnpractikapp.database.TableSchema
import com.tomli.learnpractikapp.ui.theme.LearnPractikAppTheme
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TablesScreen(navController: NavController, id: Int, colVM: DynVM = viewModel(factory = DynVM.factory)){
    val scope= rememberCoroutineScope()
    var tableManager =remember{mutableStateOf(TableManager(colVM.repos, id))}
    var table = tableManager.value.dynamicTable.collectAsState()
    var scrollStateHorizontal= rememberScrollState()
    var scrollStateVertical= rememberScrollState()

    val isCreateColumn= remember { mutableStateOf(false) }
    var updateColumnNameLambda: () -> Unit = {}
    var deleteColumnLambda: () -> Unit = {}
    val isUpdateColumn= remember { mutableStateOf(false) }
    val nameRow = remember { mutableStateOf("") }

    val isEditCell = remember{ mutableStateOf(false)}
    var updateCellLambda: ()->Unit={}
    var deleteRowLambda: ()->Unit={}
    val cellValue= remember { mutableStateOf("") }

    val launchedEffectDoer = remember { mutableStateOf(0) }
    val nameTable = remember { mutableStateOf<String?>(null) }
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        LaunchedEffect(Unit) {
            scope.launch{
                tableManager.value.loadTableFromDatabase()
            }
        }
        LaunchedEffect(key1=launchedEffectDoer.value) {
            if(launchedEffectDoer.value>0){
                scope.launch{
                    tableManager.value.saveToDatabase()
                }
            }
        }
        if(table.value!=null){
            nameTable.value=table.value!!.schema.tableName
        }
        Column(modifier = Modifier.fillMaxSize().padding(bottom=innerPadding.calculateBottomPadding())){
            Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.tertiary)){
                Spacer(modifier= Modifier.fillMaxWidth().height(innerPadding.calculateTopPadding()).background(color= Color(0x27000000)))
                Box{
                    Text(text = "${nameTable.value ?: "Таблица"}", color = Color.White, modifier = Modifier.fillMaxWidth().padding(top=15.dp, bottom = 5.dp),
                        textAlign = TextAlign.Center, fontSize = 22.sp)
                    Image(painter= painterResource(R.drawable.ic_launcher_foreground), contentDescription = null,
                        modifier= Modifier.padding(bottom = 10.dp, start=10.dp).size(35.dp).clickable { navController.navigate("mainScreen") })
                }

                Row(modifier= Modifier.fillMaxWidth()){
                    Image(painter= painterResource(R.drawable.add_column), contentDescription = null,
                        modifier= Modifier.padding(bottom = 10.dp, start=10.dp).size(35.dp).clickable { isCreateColumn.value=true })
                    Image(painter= painterResource(R.drawable.add_row), contentDescription = null,
                        modifier= Modifier.padding(bottom = 10.dp, start=10.dp).size(35.dp).
                    clickable { tableManager.value.addRow(); launchedEffectDoer.value++ })
                }
            }
            Column(modifier=Modifier.wrapContentSize().horizontalScroll(state=scrollStateHorizontal).verticalScroll(scrollStateVertical)){
                Row(modifier=Modifier.wrapContentSize()){
                    if(table.value!=null){
                        table.value!!.schema?.columns?.forEach{ item ->
                            Box(modifier=Modifier.height(30.dp).width(120.dp).background(color=Color(0xFFAD92C9)).padding(2.dp).background(color=Color(0xffeadff5))){
                                Text(text="${item!!.value.displayName}", textAlign = TextAlign.Center, modifier=Modifier.fillMaxWidth().clickable {
                                    nameRow.value=item!!.value.displayName
                                    updateColumnNameLambda = {item!!.value.displayName=nameRow.value; launchedEffectDoer.value++ }
                                    deleteColumnLambda = {tableManager.value.removeColumn(item.key)}
                                    isUpdateColumn.value=true
                                })
                            }
                        }
                    }
                }
                Column(modifier=Modifier.wrapContentSize()){
                    if(table.value!=null){
                        table.value!!.rows.forEach{ item ->
                            Row(modifier=Modifier.wrapContentSize()){
                                item.data.forEach{ item2 ->
                                    Box(modifier=Modifier.height(30.dp).width(120.dp).background(color=Color(
                                        0xFF6A6173
                                    )
                                    ).padding(2.dp).background(color=Color(0xffffffff))){
                                        Text(text= item2!!.value, textAlign = TextAlign.Center, modifier=Modifier.fillMaxWidth().clickable {
                                            cellValue.value=item2!!.value
                                            val keyThis = item2!!.key
                                            updateCellLambda={var updatedTableRow: Map<String, String> = item.data.toMutableMap().apply {
                                                this[keyThis]=cellValue.value }
                                                item.data=updatedTableRow
                                                launchedEffectDoer.value++
                                            }
                                            deleteRowLambda={
                                                var updatedList=table.value!!.rows.toMutableList().apply {
                                                    this.remove(item)
                                                }
                                                table.value!!.rows=updatedList
                                            }
                                            isEditCell.value=true
                                        })
                                    }
                                }
                            }
                        }
                    }
                }
            }
            //Text(text="${table.value ?: "a"}", modifier=Modifier.padding(10.dp))
        }
        if(isCreateColumn.value){
            CreateRowDialog({isCreateColumn.value=false}, {name->tableManager.value.addColumn(name, TableSchema.ColumnType.STRING)}, "Название")
        }
        if(isUpdateColumn.value){
            EditRowDialog({isUpdateColumn.value=false}, updateColumnNameLambda, deleteColumnLambda, "Новое название", nameRow.value, {newName-> nameRow.value=newName})
        }
        if(isEditCell.value){
            CellEditing({isEditCell.value=false}, updateCellLambda, deleteRowLambda, "Значение ячейки", cellValue.value,{newCellValue ->  cellValue.value=newCellValue})
        }
    }
}

@Composable
fun CreateRowDialog(onDismiss:()->Unit, onCreate:(name: String)-> Unit, placeholder: String){
    var name = remember{ mutableStateOf("")}
    Dialog(onDismiss) {
        Card(modifier=Modifier.padding(horizontal = 30.dp)){
            Column(modifier=Modifier.padding(15.dp)) {
                Text(text="Новый столбец")
                OutlinedTextField(value=name.value, onValueChange = {text-> name.value=text}, placeholder={
                    Text(text=placeholder, color=Color.Gray)
                })
                Text(text="Создать", textAlign = TextAlign.Center, modifier=Modifier.padding(vertical = 30.dp).fillMaxWidth().clickable { onCreate(name.value); onDismiss() })
            }
        }
    }
}


@Composable
fun EditRowDialog(onDismiss:()->Unit, onUpdate:()-> Unit, onDelete:()->Unit, placeholder: String, nameRow: String, onTextChange:(newName: String)->Unit){
    val isDelete = remember { mutableStateOf(false) }
    Dialog(onDismiss) {
        Card(modifier=Modifier.padding(horizontal = 30.dp)){
            Column(modifier=Modifier.padding(15.dp)) {
                Text(text="Редактирование столбца")
                OutlinedTextField(value=nameRow, onValueChange = onTextChange, placeholder={
                    Text(text=placeholder, color=Color.Gray)
                })
                Text(text="Сохранить", textAlign = TextAlign.Center, modifier=Modifier.padding(vertical = 30.dp).fillMaxWidth().clickable { onUpdate(); onDismiss() })
                Text(text="Удалить", textAlign = TextAlign.Center, modifier=Modifier.padding(bottom = 30.dp).fillMaxWidth().clickable { isDelete.value=true })
            }
        }
        if(isDelete.value){
            AlertDialog(onDismissRequest = {isDelete.value=false},
                title = {Text(text="Удалить столбец?")},
                text = {Text(text="Все записи внутри будут также удалены")},
                confirmButton = {Text(text = "Удалить", modifier = Modifier.padding(5.dp)
                    .clickable { onDelete(); isDelete.value=false; onDismiss()})},
                dismissButton = {Text(text = "Отменить", modifier = Modifier.padding(5.dp)
                    .clickable { isDelete.value=false})})
        }
    }
}


@Composable
fun CellEditing(onDismiss: () -> Unit, onUpdate:()-> Unit, onDeleteRow:()->Unit, placeholder: String, cellValue: String, onTextChange:(newCellValue: String)->Unit){
    val isDelete = remember { mutableStateOf(false) }
    Dialog(onDismiss) {
        Card(modifier=Modifier.padding(horizontal = 30.dp)){
            Column(modifier=Modifier.padding(15.dp)) {
                Text(text="Редактирование столбца")
                OutlinedTextField(value=cellValue, onValueChange = onTextChange, placeholder={
                    Text(text=placeholder, color=Color.Gray)
                })
                Text(text="Сохранить", textAlign = TextAlign.Center, modifier=Modifier.padding(vertical = 30.dp).fillMaxWidth().clickable { onUpdate(); onDismiss() })
                Text(text="Удалить строку", textAlign = TextAlign.Center, modifier=Modifier.padding(bottom = 30.dp).fillMaxWidth().clickable {isDelete.value })
            }
        }
        if(isDelete.value){
            AlertDialog(onDismissRequest = {isDelete.value=false},
                title = {Text(text="Удалить строку?")},
                text = {Text(text="Все записи внутри неё будут также удалены")},
                confirmButton = {Text(text = "Удалить", modifier = Modifier.padding(5.dp)
                    .clickable { onDeleteRow(); isDelete.value=false; onDismiss()})},
                dismissButton = {Text(text = "Отменить", modifier = Modifier.padding(5.dp)
                    .clickable { isDelete.value=false})})
        }
    }
}










@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LearnPractikAppTheme {
        Box(modifier=Modifier.fillMaxSize()){
            CellEditing({}, {}, {}, "Значение ячейки", "",{})

        }
    }
}