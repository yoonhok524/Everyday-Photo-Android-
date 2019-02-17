package com.youknow.everydayphoto.factory.file

import com.youknow.everydayphoto.data.model.Model
import java.io.File

interface FileFactory {
    fun createPhotoFile(modelName: String): File
    fun createPhotoFile(modelName: String, fileName: String): File
    fun getFilePathName(modelName: String, fileName: String): String
    fun getVideoFiles(model: Model): List<File>
    fun getVideoFileName(modelName: String): String

}