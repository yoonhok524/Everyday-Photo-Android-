package com.youknow.everydayphoto.ui.model.image

import com.youknow.everydayphoto.data.model.Photo

interface PhotoContract {
    interface View {
        fun onPhotosLoaded(photos: List<Photo>)

    }

    interface Presenter {
        fun unsubscribe()
        fun getPhotos(albumId: String)

    }
}