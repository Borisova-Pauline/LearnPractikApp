package com.tomli.learnpractikapp.database

import androidx.room.ColumnInfo
import androidx.room.TypeConverter
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/*data class DynamicRow(
    val tableId: Int,
    val data: Map<String, Any?> = emptyMap()
)*/

@Serializable
data class TableRow(
    val schema: TableSchema,
    var data: Map<String, String>
)

@Serializable
data class DynamicTable(
    val schema: TableSchema,
    var rows: List<TableRow>
)

@Serializable
data class DynamicTableEntity(
    val tableId: Int,
    val tableName: String,
    val schemaJson: String,
    val rowsJson: String
)

@Serializable
data class TableSchema(
    val tableName: String,
    val columns: Map<String, ColumnInfo>
){
    @Serializable
    data class ColumnInfo(
        val type: ColumnType,
        var displayName: String,
        val order: Int //Порядок отображения
    )

    @Serializable
    enum class ColumnType { STRING, INTEGER, DOUBLE, BOOLEAN, DATE }
}