package com.tomli.learnpractikapp.database

import androidx.room.ColumnInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class TableManager(private val repository: DynamicTableRepository, private val tableId: Int) {
    private var _currentTable = MutableStateFlow<DynamicTable?>(null)
    var dynamicTable: StateFlow<DynamicTable?> = _currentTable.asStateFlow()

    // Загрузка данных из БД
    suspend fun loadTableFromDatabase() {
        try {
            val table = repository.loadTable(tableId)
            _currentTable.value = table
        } catch (e: Exception) {
            // Обработка ошибок (таблица не найдена и т.д.)
            //_currentTable.value = createEmptyTable()
        }
    }

    suspend fun saveToDatabase() {
        val currentTable = _currentTable.value ?: return

        val entity = DynamicTableEntity(
            tableId = tableId,
            tableName = currentTable.schema.tableName,
            schemaJson = Json.encodeToString(currentTable.schema),
            rowsJson = Json.encodeToString(currentTable.rows)
        )
        repository.dao.update(entity.schemaJson, entity.rowsJson, entity.tableId)
    }

    fun addColumn(
        columnName: String,
        type: TableSchema.ColumnType,
        displayName: String = columnName,
        defaultValue: String = ""
    ) {
        // 1. Обновляем схему
        val newColumnInfo = TableSchema.ColumnInfo(
            type = type,
            displayName = displayName,
            order = dynamicTable.value!!.schema.columns.size // следующий порядок
        )
        val updatedSchema = dynamicTable.value!!.schema.copy(
            columns = dynamicTable.value!!.schema.columns + (columnName to newColumnInfo)
        )

        // 2. Обновляем ВСЕ строки, добавляя новый столбец
        val updatedRows = dynamicTable.value!!.rows.map { row ->
            val updatedData = row.data.toMutableMap().apply {
                // Добавляем ключ, если его еще нет
                if (!containsKey(columnName)) {
                    put(columnName, defaultValue)
                }
            }
            row.copy(data = updatedData)
        }

        // 3. Сохраняем новое состояние
        _currentTable.value = DynamicTable(updatedSchema, updatedRows)

    }

    fun removeColumn(columnName: String) {
        // 1. Удаляем столбец из схемы
        val updatedSchema = dynamicTable.value!!.schema.copy(
            columns = dynamicTable.value!!.schema.columns - columnName
        )

        // 2. Удаляем этот ключ из данных КАЖДОЙ строки
        val updatedRows = dynamicTable.value!!.rows.map { row ->
            val updatedData = row.data.toMutableMap().apply {
                remove(columnName) // Удаляем ключ из Map этой строки
            }
            row.copy(data = updatedData)
        }
        _currentTable.value = DynamicTable(updatedSchema, updatedRows)
    }

    fun addRow(){
        if(!dynamicTable.value!!.rows[0].data.isEmpty()){
            var updatedTableRow: Map<String, String> = dynamicTable.value!!.rows[0].data.toMutableMap().apply {
                keys.forEach{key->
                    this[key]=""
                }
            }
            val newTableRow = dynamicTable.value?.rows!!.toMutableList()
            newTableRow.add(TableRow(schema = dynamicTable.value?.schema!!, data = updatedTableRow))
            _currentTable.value= DynamicTable(dynamicTable.value?.schema!!, newTableRow)
        }else{
            val tableRows: MutableMap<String, String> = mutableMapOf()
            dynamicTable.value?.schema!!.columns.toMutableMap().apply {
                keys.forEach{key->
                    tableRows.put(key, "")
                }
            }
            _currentTable.value= DynamicTable(dynamicTable.value?.schema!!, listOf(TableRow(dynamicTable.value?.schema!!, data=tableRows)))
        }
    }
}