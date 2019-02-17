package com.youknow.everydayphoto.data.repository

import com.youknow.everydayphoto.data.model.Model
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface ModelRepository {
    fun save(model: Model): Observable<Model>
    fun getAll(): Single<List<Model>>
    fun delete(vararg models: Model)
    fun createModel(name: String, orientation: Int, cameraFacing: Int): Observable<Model>
    fun update(model: Model): Observable<Model>
    fun syncModels(): Observable<Model>
}