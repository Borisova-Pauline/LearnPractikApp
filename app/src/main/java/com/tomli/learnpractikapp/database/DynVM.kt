package com.tomli.learnpractikapp.database

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.tomli.learnpractikapp.Applic
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class DynVM(val database: CollectDB): ViewModel() {
    var collections = database.daoData.GetCollections()

    fun addCollection(name: String, value: List<TableRow>, schema: TableSchema)=viewModelScope.launch{
        val converter=Converters()
        val valInJson= converter.rowsToJson(value)
        val schemaInJson = converter.schemaToJson(schema)
        database.daoData.addCollection(name, valInJson, schemaInJson)
    }

    var repos=DynamicTableRepository(database.daoData)



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
    suspend fun loadTable(tableId: Int): DynamicTable {
        val entity = dao.getTableById(tableId)

        // Десериализуем JSON обратно в объекты
        return DynamicTable(
            schema = Json.decodeFromString(entity.schema!!),
            rows = Json.decodeFromString(entity.values!!)
        )
    }

    // Загружаем все таблицы (если их несколько)
    /*suspend fun loadAllTables(): List<DynamicTable> {
        return dao.getAllTables().map { entity ->
            DynamicTable(
                schema = Json.decodeFromString(entity.schemaJson),
                rows = Json.decodeFromString(entity.rowsJson)
            )
        }
    }*/
}