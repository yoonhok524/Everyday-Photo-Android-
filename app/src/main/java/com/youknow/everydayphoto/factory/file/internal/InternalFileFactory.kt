package com.youknow.everydayphoto.factory.file.internal

import android.content.Context
import com.youknow.everydayphoto.common.getPhotoFileName
import com.youknow.everydayphoto.common.getVideoFileName
import com.youknow.everydayphoto.data.model.Model
import com.youknow.everydayphoto.factory.file.FileFactory
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.io.File
import java.util.*

class InternalFileFactory(private val context: Context) : FileFactory, AnkoLogger {

    override fun createPhotoFile(modelName: String, fileName: String): File =
        createFile(getFolderPath(modelName), File.separator + fileName)

    override fun createPhotoFile(modelName: String): File = createPhotoFile(modelName, Date().getPhotoFileName())

    override fun getFilePathName(modelName: String, fileName: String): String =
        getFolderPath(modelName) + File.separator + fileName

    override fun getVideoFileName(modelName: String): String =
        getFolderPath(modelName) + File.separator + modelName.getVideoFileName()

    override fun getVideoFiles(model: Model): List<File> =
        File(getFolderPath(model.name))
            .listFiles()
            .filter { it.name.endsWith(".mp4") }
            .toList()

    private fun getFolderPath(modelName: String): String =
        context.filesDir.path + File.separator + "EverydayPhoto" + File.separator + modelName

    private fun createFile(folderPath: String, fileName: String): File {
        info("[EP] createFile - $folderPath/$fileName")
        val folder = File(folderPath).let {
            if (!it.exists() || !it.isDirectory) {
                it.mkdirs()
            }

            it
        }

        return File(folder.path + fileName)
    }

}