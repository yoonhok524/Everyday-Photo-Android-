package com.youknow.everydayphoto.data.source.local

import android.arch.persistence.room.*
import com.youknow.everydayphoto.data.model.Model
import io.reactivex.Single

@Dao
interface ModelDao {
    @Query("SELECT * FROM model")
    fun getAll(): Single<List<Model>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg models: Model)

    @Update
    fun update(model: Model)

    @Delete
    fun delete(vararg models: Model)
}