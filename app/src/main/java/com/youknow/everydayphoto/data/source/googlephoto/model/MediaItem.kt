package com.youknow.everydayphoto.data.source.googlephoto.model

data class BatchCreateRequest(
    var albumId: String,
    var newMediaItems: List<NewMediaItem>
)

data class SearchMediaItemsRequest(
    var albumId: String = "",
    var pageSize: Int = 25,
    var pageToken: String? = null,
    var filters: Any? = null
)

data class NewMediaItem(
    var description: String = "",
    var simpleMediaItem: SimpleMediaItem
)

data class SimpleMediaItem(
    var uploadToken: String
)

data class BatchCreateResponse(
    var newMediaItemResults: List<NewMediaItemResult>
)

data class SearchMediaItemsResponse(
    var mediaItems: List<MediaItem>?,
    var nextPageToken: String? = null
)

data class NewMediaItemResult(
    var uploadToken: String,
    var status: Status,
    var mediaItem: MediaItem
)

data class Status(
    var code: Int,
    var message: String? = null,
    var details: List<Details>? = null
)

data class Details(
    var any: Any
)

data class MediaItem(
    var id: String,
    var description: String,
    var productUrl: String,
    var baseUrl: String,
    var mimeType: String,
    var mediaMetadata: MediaMetadata,
    var contributorInfo: ContributorInfo,
    var filename: String
)

data class ContributorInfo(
    var profilePictureBaseUrl: String,
    var displayName: String
)

data class MediaMetadata(
    var creationTime: String,
    var width: String,
    var height: String,
    var photo: Photo,
    var video: Video
)

data class Photo(
    var cameraMake: String,
    var cameraModel: String,
    var focalLength: Long,
    var apertureFNumber: Long,
    var isoEquivalent: Long,
    var exposureTime: String
)

data class Video(
    var cameraMake: String,
    var cameraModel: String,
    var fps: Int,
    var status: String
)