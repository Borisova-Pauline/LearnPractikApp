package com.tomli.learnpractikapp.database

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DaoData {
    @Query("select * from collections")
    fun GetCollections(): Flow<List<Collections>>

    @Query("update collections set name=:value where id=:id")
    suspend fun updateName(value: String, id: Int)

    @Query("update collections set `values`=:values where id=:id")
    suspend fun updateVals(values: String, id: Int)

    @Query("select (id, name, `schema`, `values`) from collections where id=:tableId ")
    suspend fun getTableById(tableId: Int): DynamicTableEntity

    @Query("update collections set schema=:schemaJson")
    suspend fun update(schemaJson: String, rowsJson: String, id: Int)
}