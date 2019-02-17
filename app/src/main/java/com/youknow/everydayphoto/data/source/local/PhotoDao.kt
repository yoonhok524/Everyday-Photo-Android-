package com.youknow.everydayphoto.data.source.local

import android.arch.persistence.room.*
import com.youknow.everydayphoto.data.model.Photo
import io.reactivex.Single

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photo ORDER BY createdAt DESC")
    fun getAll(): Single<List<Photo>>

    @Query("SELECT * FROM photo WHERE albumId = :albumId ORDER BY createdAt DESC")
    fun getAllByModel(albumId: String): Single<List<Photo>>

    @Query("SELECT * FROM photo WHERE albumId = :albumId ORDER BY createdAt DESC LIMIT :limit")
    fun getAllByModelWithLimit(albumId: String, limit: Int): Single<List<Photo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg photos: Photo)

    @Delete
    fun delete(vararg photos: Photo)

    @Query("SELECT COUNT(id) FROM photo WHERE albumId = :albumId")
    fun getNumberOfPhotosByModel(albumId: String): Single<Int>

    @Query("SELECT * FROM photo WHERE id = :id")
    fun get(id: String): Single<Photo>
}