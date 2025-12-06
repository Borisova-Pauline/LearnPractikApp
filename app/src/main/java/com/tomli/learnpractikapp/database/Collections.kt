package com.tomli.learnpractikapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = "collections")
data class Collections(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    val name: String?,
    val values: String?, //rows list
    val schema: String?
)

// TypeConverters
class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    // Для схемы
    @TypeConverter
    fun schemaFromJson(value: String): TableSchema =
        json.decodeFromString(value)

    @TypeConverter
    fun schemaToJson(schema: TableSchema): String =
        json.encodeToString(schema)

    // Для строк
    @TypeConverter
    fun rowsFromJson(value: String): List<TableRow> =
        json.decodeFromString(value)

    @TypeConverter
    fun rowsToJson(rows: List<TableRow>): String =
        json.encodeToString(rows)
}