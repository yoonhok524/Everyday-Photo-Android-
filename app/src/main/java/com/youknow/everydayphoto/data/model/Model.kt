package com.youknow.everydayphoto.data.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcelable
import com.youknow.everydayphoto.common.CameraFacing
import com.youknow.everydayphoto.common.Orientation
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity
data class Model(
    @PrimaryKey
    var albumId: String = "", // Google Photo Album id
    var name: String = "",
    var desc: String = "",
    var profileUrl: String = "",
    var orientation: Int = Orientation.Landscape.value,
    var cameraFacing: Int = CameraFacing.Back.value,
    var createdAt: Long = System.currentTimeMillis(),
    var numOfDays: Int = 0
) : Parcelable