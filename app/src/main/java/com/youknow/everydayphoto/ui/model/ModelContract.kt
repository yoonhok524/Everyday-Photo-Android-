package com.youknow.everydayphoto.ui.model

import com.youknow.everydayphoto.data.model.Model
import com.youknow.everydayphoto.data.model.Photo
import java.io.File

interface ModelContract {
    interface View {
        fun onPhotosLoaded(photos: List<Photo>)
        fun onNumberOfPhotoLoaded(numberOfPhotos: String)
        fun onVideosLoaded(videos: List<File>)

    }

    interface Presenter {
        fun unsubscribe()
        fun getNumberOfPhotos(albumId: String)
        fun getLast10Photos(albumId: String)
        fun getVideos(model: Model)
    }
}