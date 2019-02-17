package com.youknow.everydayphoto.factory.file.external

import android.os.Environment
import com.youknow.everydayphoto.common.getPhotoFileName
import com.youknow.everydayphoto.common.getVideoFileName
import com.youknow.everydayphoto.data.model.Model
import com.youknow.everydayphoto.factory.file.FileFactory
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.io.File
import java.util.*

class ExternalFileFactory : FileFactory, AnkoLogger {

    override fun createPhotoFile(modelName: String, fileName: String): File =
        createFile(getFolderPath(modelName), File.separator + fileName)

    override fun createPhotoFile(modelName: String): File = createPhotoFile(modelName, Date().getPhotoFileName())

    override fun getFilePathName(modelName: String, fileName: String): String =
        getFolderPath(modelName) + File.separator + fileName

    override fun getVideoFileName(modelName: String): String {
        return getFolderPath(modelName) + File.separator + modelName.getVideoFileName()
    }

    override fun getVideoFiles(model: Model): List<File> {
        val file = File(getFolderPath(model.name))
        info("[EP] getVideoFiles - file: ${file.path}")

        return if (file.exists()) {
            file.listFiles()
                .filter { it.name.endsWith(".mp4") }
                .toList()
        } else {
            listOf()
        }
    }

    private fun getFolderPath(modelName: String): String =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path + File.separator + "EverydayPhoto" + File.separator + modelName

    private fun createFile(folderPath: String, fileName: String): File {
        val folder = File(folderPath).let {
            if (!it.exists() || !it.isDirectory) {
                it.mkdirs()
            }

            it
        }

        return File(folder.path + fileName)
    }

}