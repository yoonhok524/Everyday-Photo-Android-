package com.youknow.everydayphoto.data.source.googlephoto.model

data class Album(
    var id: String? = null,
    var title: String,
    var productUrl: String? = null,
    var isWriteable: Boolean = true,
    var shareInfo: ShareInfo? = null,
    var mediaItemsCount: String? = null,
    var coverPhotoBaseUrl: String? = null,
    var coverPhotoMediaItemId: String? = null
)

data class AlbumRequest (
    var album: Album
)

data class AlbumsList(
    var albums: List<Album>,
    var nextPageToken: String
)

data class ShareInfo(
    var sharedAlbumOptions: SharedAlbumOptions,
    var shareableUrl: String,
    var shareToken: String,
    var isJoined: Boolean
)

data class SharedAlbumOptions(
    var isCollaborative: Boolean,
    var isCommentable: Boolean
)