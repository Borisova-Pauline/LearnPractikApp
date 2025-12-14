package com.tomli.learnpractikapp.database

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.android.material.tabs.TabLayout.Tab
import com.tomli.learnpractikapp.Applic
import com.tomli.learnpractikapp.createExcelFileInFolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat

class DynVM(val database: CollectDB): ViewModel() {
    var collections = database.daoData.GetCollections()

    fun addCollection(name: String, value: List<TableRow>, schema: TableSchema)=viewModelScope.launch{
        val converter=Converters()
        val valInJson= converter.rowsToJson(value)
        val schemaInJson = converter.schemaToJson(schema)
        database.daoData.addCollection(name, valInJson, schemaInJson)
    }

    var repos=DynamicTableRepository(database.daoData)

    fun setNewName(name: String, id: Int)=viewModelScope.launch {
        database.daoData.setNewName(name, id)
    }

    fun deleteCollection(id: Int)=viewModelScope.launch {
        database.daoData.deleteCollection(id)
    }

    suspend fun copyFileToCache(context: Context, fileUri: Uri, destinationNameFile: String): File?{
        val destinationDir=context.cacheDir
        val destinationFile=File(destinationDir, destinationNameFile)

        return withContext(Dispatchers.IO){
            var inputStream: InputStream? =null
            var outputStream: OutputStream?=null
            try{
                inputStream=context.contentResolver.openInputStream(fileUri)
                outputStream=FileOutputStream(destinationFile)
                inputStream?.copyTo(outputStream)
                destinationFile
            }catch (e: Exception){
                e.printStackTrace()
                null
            }finally {
                inputStream?.close()
                outputStream?.close()
            }
        }
    }


    fun CreateFromImport(context: Context, fileUri: Uri?, name: String)=viewModelScope.launch {
        if(fileUri!=null){
            val xlsxFile = copyFileToCache(context, fileUri, "file"+System.currentTimeMillis().toString()+".xlsx")
            if(xlsxFile!=null){
                Toast.makeText(context, "Импорт коллекции", Toast.LENGTH_LONG).show()
                val entity = convertXLSXtoDynamicTable(xlsxFile, name)
                database.daoData.addCollection(name, value= Json.encodeToString(entity.rows), schema = Json.encodeToString(entity.schema))
            }else{
                Toast.makeText(context, "Не удалось создать коллекцию :(", Toast.LENGTH_LONG).show()
            }
        }else{
            Toast.makeText(context, "Не удалось получить путь к файлу :(", Toast.LENGTH_LONG).show()
        }
    }


    fun ExportFromDynamicTable(id: Int, context: Context, folderUri: Uri, fileName: String)=viewModelScope.launch {
        val entityTable=database.daoData.getTableById(id)
        val table = DynamicTable(schema = Json.decodeFromString(entityTable.schema!!),
            rows = Json.decodeFromString(entityTable.values!!))
        createExcelFileInFolder(
            context = context,
            folderUri = folderUri,
            fileName = fileName,
            sheetName = "Лист1",
            table = table
        )
    }


    companion object{
        val factory: ViewModelProvider.Factory= object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val database = (checkNotNull(extras[APPLICATION_KEY]) as Applic).database
                return DynVM(database) as T
            }
        }
    }
}


// 1. Репозиторий для работы с БД
class DynamicTableRepository(val dao: DaoData) {

    // Загружаем всю таблицу по её ID
    suspend fun loadTable(tableId: Int): Collections {
        //val entity = dao.getTableById(tableId)

        // Десериализуем JSON обратно в объекты
        /*return DynamicTable(
            schema = Json.decodeFromString(entity.schema!!),
            rows = Json.decodeFromString(entity.values!!)
        )*/
        return dao.getTableById(tableId)
    }
}


fun convertXLSXtoDynamicTable(xlsxFile: File, name: String): DynamicTable{
    val workbook=XSSFWorkbook(FileInputStream(xlsxFile))
    val sheet = workbook.getSheetAt(0)
    var i = 0
    var j=0
    val columns = mutableMapOf<String, TableSchema.ColumnInfo>()
    var valueList= mutableListOf<TableRow>()
    var schema = TableSchema(name, columns)
    val maxColumns = sheet.map{it.lastCellNum}.maxOrNull()?:0
    for(row in sheet){
        var rowData = mutableListOf<String>()
        for(/*cell in row*/ cellIndex in 0 until maxColumns){
            val cell=row.getCell(cellIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)
            val cellValue= when(cell.cellType){
                CellType.STRING -> cell.stringCellValue
                CellType.NUMERIC -> {
                    if(DateUtil.isCellDateFormatted(cell)){
                        val date = cell.dateCellValue
                        val dateFormat = SimpleDateFormat("dd.MM.yyyy")
                        dateFormat.format(date)
                    }else{
                        cell.numericCellValue.toString()
                    }
                }
                CellType.BOOLEAN -> cell.booleanCellValue.toString()
                CellType.FORMULA -> {
                    try{
                        cell.numericCellValue.toString()
                    }catch (e: Exception){
                        cell.stringCellValue
                    }
                }
                else -> { "" }
            }
            rowData.add(cellValue)
        }
        if(i==0){
            for(cell in rowData){
                columns.put(key = cell, value = TableSchema.ColumnInfo(TableSchema.ColumnType.STRING, cell, j))
                j++
            }
        }else{
                var data = mutableMapOf<String, String>()
                var v = 0
                schema.columns.keys.forEach{ key->
                    try{
                        if(v<j){
                            data.put(key, rowData.get(v))
                            v++
                        }
                    }catch (e: Exception){
                        e.printStackTrace()
                        data.put(key, "")
                    }
                }
                valueList.add(TableRow(schema, data))
        }
        i++
    }
    return DynamicTable(schema = schema, rows = valueList)
}