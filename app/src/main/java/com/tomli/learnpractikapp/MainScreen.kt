package com.tomli.learnpractikapp

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toLowerCase
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File
import android.provider.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.SnackbarHost
import com.tomli.learnpractikapp.database.DynamicTable
import kotlinx.serialization.json.JsonNull.content
import org.apache.commons.io.output.ByteArrayOutputStream
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileOutputStream


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(navController: NavController, colVM: DynVM = viewModel(factory = DynVM.factory)){
    var colls = colVM.collections.collectAsState(initial = emptyList())
    val isCreateCollection= remember { mutableStateOf(false) }
    val isUpdateCollection = remember { mutableStateOf(false) }
    val itemId= remember { mutableStateOf(0) }
    val itemName=remember { mutableStateOf("") }
    val isDeleteCollection =remember { mutableStateOf(false) }
    val isExportCollection =remember { mutableStateOf(false) }
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(bottom=innerPadding.calculateBottomPadding())){
            Column(modifier = Modifier.fillMaxWidth().background(color=Color(0xff9967cc))){
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
                                onClick = {isUpdateCollection.value=true; showDropDown.value= false })
                            DropdownMenuItem(text = { Text("Экспорт") },
                                onClick = {isExportCollection.value=true; showDropDown.value= false })
                            DropdownMenuItem(text = { Text("Удалить", color=Color.Red) },
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
        if(isExportCollection.value){
            ExportDialog({isExportCollection.value=false}, itemId.value, itemName.value)
        }
    }
}

@Composable
fun CreateCollection(onDismiss:()->Unit, colVM: DynVM = viewModel(factory = DynVM.factory)){
    val name = remember { mutableStateOf("") }
    val columns = remember { mutableStateOf("1") }
    val rows =remember { mutableStateOf("0") }
    val wayToCreate= remember { mutableStateOf(WayToCreate.Grid) }
    val pathImport = remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val filePickerLauncher= rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ){
        uri: Uri? ->
            pathImport.value= uri
    }
    Dialog(onDismiss){
        Card(modifier=Modifier.padding(horizontal = 30.dp)){
            Column(modifier=Modifier.padding(15.dp)) {
                Text(text="Новая коллекция")
                OutlinedTextField(value=name.value, onValueChange = {text-> name.value=text},
                    placeholder={ Text(text="Введите название", color=Color.Gray)},
                    label={Text(text="Название коллекции")})
                Row(Modifier.height(25.dp)){
                    Text(text="Вручную", textAlign = TextAlign.Center, modifier=Modifier.weight(1f).clickable { wayToCreate.value=WayToCreate.Grid }, color= if(wayToCreate.value==WayToCreate.Grid) Color(0xff7b00ff) else Color(0xff838383))
                    Text(text="Импорт", textAlign = TextAlign.Center, modifier=Modifier.weight(1f).clickable { wayToCreate.value=WayToCreate.Import }, color= if(wayToCreate.value==WayToCreate.Import) Color(0xff7b00ff) else Color(0xff838383))
                }
                when(wayToCreate.value){
                    WayToCreate.Grid->{
                        OutlinedTextField(value=columns.value, onValueChange = {text-> columns.value=text}, placeholder={
                            Text(text="Введите число больше 0", color=Color.Gray)
                        }, label={Text(text="Количество столбцов")}, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        OutlinedTextField(value=rows.value, onValueChange = {text-> rows.value=text}, placeholder={
                            Text(text="Введите число >=0", color=Color.Gray)
                        }, label={Text(text="Количество строк")}, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        Text(text="Создать", textAlign = TextAlign.Center, modifier=Modifier.padding(vertical = 30.dp).fillMaxWidth().clickable {
                            if(name.value!=""){
                                try{
                                    if(columns.value.toInt()>0 && rows.value.toInt()>=0){
                                        val schema = createSchemaOnCreating(name.value, columns.value.toInt())
                                        colVM.addCollection(name=name.value,
                                            value=createValueOnCreating(schema, rows.value.toInt()),
                                            schema=schema)
                                        onDismiss()
                                    }else{
                                        Toast.makeText(context, "Неправильный ввод строк или столбцов", Toast.LENGTH_LONG).show()
                                    }
                                }catch (e: Exception){
                                    Toast.makeText(context, "Неправильный ввод строк или столбцов", Toast.LENGTH_LONG).show()
                                }
                            }else{
                                Toast.makeText(context, "Введите название", Toast.LENGTH_LONG).show()
                            }})
                    }
                    WayToCreate.Import->{
                        val fileName=getFileName(pathImport.value, context)
                        OutlinedCard(modifier = Modifier.fillMaxWidth().padding(top=10.dp)){
                            Text(text= fileName, modifier = Modifier.fillMaxWidth().padding(5.dp), textAlign = TextAlign.Center)
                        }
                        Text(text="Выбрать файл", modifier = Modifier.clickable {
                            filePickerLauncher.launch("*/*")
                        }.padding(vertical=20.dp).fillMaxWidth(), textAlign = TextAlign.Center)
                        Text(text="Создать", modifier = Modifier.padding(bottom=20.dp).clickable {
                            if(name.value!=""){
                                if(getFileExtension(fileName) && fileName!="Unknown.xlsx"){
                                    colVM.CreateFromImport(context, pathImport.value, name.value)
                                }else if(fileName=="Unknown.xlsx"){
                                    Toast.makeText(context, "Не выбран файл", Toast.LENGTH_LONG).show()
                                }else if(!getFileExtension(fileName)){
                                    Toast.makeText(context, "Расширение файла не подходит, должно быть .XLSX", Toast.LENGTH_LONG).show()
                                }else{
                                    Toast.makeText(context, "Ошибка", Toast.LENGTH_LONG).show()
                                }
                            }else{
                                Toast.makeText(context, "Введите имя", Toast.LENGTH_LONG).show()
                            }
                            onDismiss()
                        }.padding(vertical=10.dp).fillMaxWidth(), textAlign = TextAlign.Center,
                            color=if(getFileExtension(fileName) && fileName!="Unknown.xlsx") Color(0xff00be19) else Color(0xffff0000))
                    }
                }
            }
        }
    }
}

enum class WayToCreate{
    Grid, Import
}



@Composable
fun UpdateCollection(onDismiss: () -> Unit, id: Int, origName: String, colVM: DynVM = viewModel(factory = DynVM.factory)){
    val name = remember { mutableStateOf(origName) }
    val context = LocalContext.current
    Dialog(onDismiss){
        Card(modifier=Modifier.padding(horizontal = 30.dp)){
            Column(modifier=Modifier.padding(15.dp)){
                Text(text="Редактирование коллекции")
                OutlinedTextField(value=name.value, onValueChange = {text-> name.value=text},
                    placeholder={ Text(text="Введите название", color=Color.Gray)},
                    label={Text(text="Название коллекции")})
                Text(text="Сохранить", textAlign = TextAlign.Center, modifier=Modifier.padding(vertical = 30.dp).fillMaxWidth().clickable {
                    if(name.value!=""){
                        colVM.setNewName(name.value, id)
                        onDismiss()
                    }else{
                        Toast.makeText(context, "Введите название", Toast.LENGTH_LONG).show()
                    }})
            }
        }
    }
}


@Composable
fun ExportDialog(onDismiss: () -> Unit, id: Int, tableName: String, colVM: DynVM = viewModel(factory = DynVM.factory)){
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Состояния
    var selectedFolderUri = remember { mutableStateOf<Uri?>(null) }
    var folderName = remember { mutableStateOf("") }
    var fileName = remember { mutableStateOf("$tableName.xlsx") }

    // Ланчер для выбора папки (Storage Access Framework)
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { uri ->
            uri?.let {
                // Запрашиваем постоянный доступ к папке
                context.contentResolver.takePersistableUriPermission(uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                selectedFolderUri.value = uri
                folderName.value = getFolderNameFromUri(uri) ?: "Неизвестная папка"
            }
        }
    )

    val manageStorageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {  }


    Dialog(onDismiss) {
        Card(modifier = Modifier.padding(horizontal = 30.dp)) {
            Column(modifier = Modifier.padding(15.dp)) {
                Text(text = "Экспорт коллекции \"$tableName\"", modifier=Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                OutlinedCard(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                    if(selectedFolderUri.value!=null){
                        val folder: String? = selectedFolderUri.value!!.path
                        Text(
                            text = /*folder ?: "Unknown",*/ folderName.value,
                            modifier = Modifier.fillMaxWidth().padding(5.dp),
                            textAlign = TextAlign.Center
                        )
                    }else{
                        Text(
                            text = "Unknown",
                            modifier = Modifier.fillMaxWidth().padding(5.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Text(text = "Выбрать папку", modifier = Modifier.clickable {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        // Проверяем, есть ли доступ ко всему хранилищу
                        if (Environment.isExternalStorageManager()) {
                            folderPickerLauncher.launch(null)
                        } else {
                            // Запрашиваем полный доступ (опционально)
                            val intent =
                                Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                            intent.data = Uri.parse("package:${context.packageName}")
                            manageStorageLauncher.launch(intent)
                        }
                    } else {
                        folderPickerLauncher.launch(null)
                    }
                }.padding(vertical = 20.dp).fillMaxWidth(), textAlign = TextAlign.Center)
                Text(
                    text = "Создать XLSX в выбранной папке",
                    modifier = Modifier.padding(bottom = 20.dp).clickable {
                        if (selectedFolderUri.value != null && fileName.value.isNotBlank()) {
                            colVM.ExportFromDynamicTable(id, context, selectedFolderUri.value!!, fileName.value)
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Сначала выберите папку")
                            }
                        }
                        onDismiss()
                    }.padding(vertical = 10.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = if (selectedFolderUri.value != null) Color(0xff00be19) else Color(0xffff0000)
                )
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


fun getFileName(uri: Uri?, context: Context): String{
    var fileName="Unknown.xlsx"
    if(uri!=null){
        if(uri.scheme=="content"){
            context.contentResolver.query(uri, null, null, null, null)?.use{ cursor->
                if(cursor.moveToFirst()){
                    val nameIndex=cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if(nameIndex!=-1){
                        fileName=cursor.getString(nameIndex)
                    }
                }
            }
        }else if(uri.scheme=="file"){
            fileName=uri.lastPathSegment ?: "Unknown.xlsx"
        }
    }
    return fileName
}


fun getFileExtension(fileName: String): Boolean{
    val dotIndex=fileName.lastIndexOf('.')
    var ext=".h"
    if(dotIndex!=-1 && dotIndex<fileName.length-1){
        ext=fileName.substring(dotIndex+1).toLowerCase()
    }
    if(ext=="xlsx"){
        return true
    }else{
        return false
    }
}


fun getFolderNameFromUri(fileUri: Uri?): String {
    var folderName="Unknown folder"
    if(fileUri!=null){
        if(fileUri.scheme=="content"){
            val path = fileUri.path
            val lastIndex = path?.lastIndexOf(':')
            folderName=path?.substring(lastIndex!!+1, path.length).toString()
        }
    }
    return folderName
}



// Функция для создания Excel файла с использованием Apache POI
fun createExcelFileInFolder(
    context: Context,
    folderUri: Uri,
    fileName: String,
    sheetName: String,
    table: DynamicTable
) {
    try {
        // Получаем DocumentFile для папки
        val documentFile = androidx.documentfile.provider.DocumentFile
            .fromTreeUri(context, folderUri)

        // Проверяем существующий файл
        val existingFile = documentFile?.findFile(fileName)
        if (existingFile != null) {
            Toast.makeText(context, "Файл уже существует. Удалите его или измените имя.", Toast.LENGTH_LONG).show()
            return
        }

        // Создаем новый файл
        val newFile = documentFile?.createFile(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            fileName
        )
        newFile?.let { docFile ->
            // Создаем данные для Excel
            val excelData = createExcelData(
                sheetName = sheetName,
                table
            )
            // Записываем данные
            context.contentResolver.openOutputStream(docFile.uri)?.use { outputStream ->
                outputStream.write(excelData)
                Toast.makeText(context, "Excel файл $fileName создан успешно!", Toast.LENGTH_LONG).show()
            }
        } ?: run {
            Toast.makeText(context, "Не удалось создать файл", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Ошибка создания Excel: ${e.message}", Toast.LENGTH_LONG).show()
        e.printStackTrace()
    }
}


fun createExcelData(
    sheetName: String,
    table: DynamicTable
): ByteArray {
    return try {
        // Способ 1: Используем Apache POI (полнофункциональный)
        createExcelWithApachePOI(sheetName, table)
    } catch (e: NoClassDefFoundError) {
        e.printStackTrace()
        // Способ 2: Если POI не работает, создаем простой CSV в XLSX обертке
        createSimpleExcelData(table)
    }
}


// Альтернативная реализация без Apache POI (простые данные)
fun createSimpleExcelData(
    table: DynamicTable
): ByteArray {
    val content = buildString {
        // Заголовки CSV
        val listHeader=table.schema.columns.keys.toList()
        append((0..<listHeader.size).joinToString(",") { listHeader[it] })
        append("\n")

        // Данные
        val listRows = table.rows
        for (row in 0..<listRows.size) {
            val listCells = table.rows[row].data.values.toList()
            append((0..<listCells.size).joinToString(",") { listCells[it] })
            append("\n")
        }
    }
    return content.toByteArray()
}


// Реализация с Apache POI
fun createExcelWithApachePOI(
    sheetName: String,
    table: DynamicTable
): ByteArray {
    ByteArrayOutputStream().use { outputStream ->
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet(sheetName)
        // Создаем заголовки
        val headerRow = sheet.createRow(0)
        val listHeader=table.schema.columns.values.toList()
        for (col in 0 until  listHeader.size) {
            val cell = headerRow.createCell(col)
            if(listHeader[col].displayName!=""){
                cell.setCellValue(listHeader[col].displayName)
            }
        }
        // Заполняем данными
        val listRows = table.rows
        for (rowNum in 1..listRows.size) {
            val row = sheet.createRow(rowNum)
            val listCells = table.rows[rowNum-1].data.values.toList()
            for (col in 0 until listCells.size) {
                val cell = row.createCell(col)
                if(listCells[col]!=""){
                    cell.setCellValue(listCells[col])
                }
            }
        }
        workbook.write(outputStream)
        workbook.close()

        return outputStream.toByteArray()
    }
}

