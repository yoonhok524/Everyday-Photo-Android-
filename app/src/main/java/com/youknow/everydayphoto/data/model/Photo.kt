package com.youknow.everydayphoto.data.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(
    foreignKeys = [ForeignKey(
        entity = Model::class,
        parentColumns = arrayOf("albumId"),
        childColumns = arrayOf("albumId"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class Photo(
    @PrimaryKey
    var id: String = "", // Google Photo MediaItem id
    var createdAt: Long = System.currentTimeMillis(),
    var albumId: String = "",
    var fileName: String = ""
) : Parcelable