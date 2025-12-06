package com.tomli.learnpractikapp.database

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.tomli.learnpractikapp.Applic
import kotlinx.serialization.json.Json

class DynVM(val database: CollectDB): ViewModel() {
    var collections = database.daoData.GetCollections()



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
            schema = Json.decodeFromString(entity.schemaJson),
            rows = Json.decodeFromString(entity.rowsJson)
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