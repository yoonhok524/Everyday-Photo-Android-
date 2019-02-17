package com.youknow.everydayphoto.common

import android.hardware.Camera
import java.text.SimpleDateFormat
import java.util.*

fun Camera.setOrientation(w: Int, h: Int) {
    setDisplayOrientation(if (w > h) 0 else 90)
}

val dateFormat = SimpleDateFormat("yyyy-MM-dd")
val dateFileNameFormat = SimpleDateFormat("yyyyMMdd")
val timeStampFormat = SimpleDateFormat("yyyyMMdd_HHmmss")
val yyyy_mmm = SimpleDateFormat("yyyy MMM")

// 2019-02-09T17:30:04Z
val googlePhotoTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

fun Date.timeStamp(): String = timeStampFormat.format(this)

fun Long.toYyyyMmm(): String = yyyy_mmm.format(Date(this))

fun Long.toDays(): String = (((Date().time - this) / 1000 / 60 / 60 / 24).toInt() + 1).toString()

fun Long.toDate(): String = dateFormat.format(Date(this))

fun Date.getPhotoFileName(): String = "ep_${timeStampFormat.format(this)}.jpeg"

fun String.getThumbnailFileName(suffixTime: String): String = "tn_${this}_$suffixTime.jpeg"

fun String.getVideoFileName(): String = "${this}_${Date().time.toDate()}.mp4"

fun String.toLongTime(): Long = googlePhotoTimeFormat.parse(this).time