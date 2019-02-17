package com.youknow.everydayphoto.ui.model.photos

import com.youknow.everydayphoto.data.model.Photo

interface PhotosContract{
    interface View {
        fun onPhotosLoaded(photoList: List<Photo>)

    }

    interface Presenter {
        fun unsubscribe()
        fun getPhotos(albumId: String)

    }
}