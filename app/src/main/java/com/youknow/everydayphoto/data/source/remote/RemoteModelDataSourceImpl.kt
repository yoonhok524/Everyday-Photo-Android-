package com.youknow.everydayphoto.data.source.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.youknow.everydayphoto.common.MODELS
import com.youknow.everydayphoto.common.USERS
import com.youknow.everydayphoto.data.model.Model
import com.youknow.everydayphoto.exceptions.ModelsNotFoundException
import io.reactivex.Observable
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.info


class RemoteModelDataSourceImpl(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AnkoLogger {

    fun save(model: Model) {
        auth.currentUser?.uid?.let {
            db.collection(USERS)
                .document(it)
                .collection(MODELS)
                .document(model.albumId)
                .set(model)
                .addOnCompleteListener { result ->
                    if (result.isSuccessful) {
                        info("[EP] save - success: ${auth.currentUser?.displayName} / $model")
                    } else {
                        error("[EP] save - not success: ${auth.currentUser?.displayName} / $model")
                    }
                }
                .addOnFailureListener { e ->
                    error("[EP] save - failed: ${e.message}", e)
                }
        }
    }

    fun delete(vararg models: Model) {
        auth.currentUser?.uid?.let { uid ->
            val batch = db.batch()

            models.map {
                db.collection(USERS)
                    .document(uid)
                    .collection(MODELS)
                    .document(it.albumId)
            }.forEach {
                batch.delete(it)
            }

            batch.commit()
                .addOnCompleteListener { result ->
                    if (result.isSuccessful) {
                        info("[EP] delete - success")
                    } else {
                        error("[EP] delete - not success")
                    }
                }
                .addOnFailureListener { e ->
                    error("[EP] delete - failed: ${e.message}", e)
                }
        }
    }

    fun getModels(): Observable<List<Model>> {
        info("[EP] getModels")
        return Observable.create<List<Model>> { emitter ->
            auth.currentUser?.uid?.let { uid ->
                db.collection(USERS)
                    .document(uid)
                    .collection(MODELS)
                    .get()
                    .addOnCompleteListener { task ->
                        info("[EP] getModels - task.isSuccessful: ${task.isSuccessful}")
                        if (task.isSuccessful) {
                            val models = task.result?.map {
                                it.toObject(Model::class.java)
                            }?.toList() ?: listOf()

                            emitter.onNext(models)
                            emitter.onComplete()
                        } else {
                            emitter.onError(ModelsNotFoundException())
                        }
                    }
            }
        }
    }
}