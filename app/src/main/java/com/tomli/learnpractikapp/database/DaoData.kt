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

    @Query("select * from collections where id=:tableId ")
    suspend fun getTableById(tableId: Int): Collections

    @Query("update collections set schema=:schemaJson, `values`=:rowsJson where id=:id")
    suspend fun update(schemaJson: String, rowsJson: String, id: Int)

    @Query("insert into collections (name, `values`, schema) values (:name, :value, :schema)")
    suspend fun addCollection(name: String, value: String, schema: String)

    @Query("update collections set name=:name where id=:id")
    suspend fun setNewName(name: String, id: Int)

    @Query("delete from collections where id=:id")
    suspend fun deleteCollection(id: Int)
}