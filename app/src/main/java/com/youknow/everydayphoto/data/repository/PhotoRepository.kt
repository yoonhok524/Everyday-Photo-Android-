package com.youknow.everydayphoto.data.repository

import com.youknow.everydayphoto.data.model.Model
import com.youknow.everydayphoto.data.model.Photo
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.io.File

interface PhotoRepository {
    fun savePhoto(model: Model, data: ByteArray, width: Int, height: Int): Completable
    fun getNumberOfPhotos(albumId: String): Observable<String>
    fun getPhotos(albumId: String): Observable<List<Photo>>
    fun syncPhotos(model: Model, nextPageToken: String? = null): Observable<String>
    fun getPhotosByModelWithLimit(albumId: String, limit: Int): Single<List<Photo>>
    fun makeVideo(albumId: String, modelName: String): Completable
    fun getVideos(model: Model): List<File>

}