package com.youknow.everydayphoto.data.source.googlephoto

import com.youknow.everydayphoto.data.source.googlephoto.model.*
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*


interface GooglePhotoService {

    @GET("albums/{albumId}")
    fun getAlbum(@Header("Authorization") authorization: String, @Path("albumId") albumId: String): Observable<Album>

    @POST("albums")
    fun createAlbums(@Header("Authorization") authorization: String, @Body albumRequest: AlbumRequest): Observable<Album>

    @Headers(
        "Content-type: application/octet-stream",
        "X-Goog-Upload-Protocol: raw"
    )
    @POST("uploads")
    fun upload(@Header("Authorization") authorization: String, @Header("X-Goog-Upload-File-Name") fileName: String, @Body photoBinary: RequestBody): Observable<ResponseBody>

    @Headers("Content-type: application/json")
    @POST("./mediaItems:batchCreate")
    fun batchCreate(@Header("Authorization") authorization: String, @Body batchCreateRequest: BatchCreateRequest): Observable<BatchCreateResponse>

    @GET("mediaItems/{mediaItemId}")
    fun getMediaItem(@Header("Authorization") authorization: String, @Path("mediaItemId") mediaItemId: String): Observable<MediaItem>

    @POST("./mediaItems:search")
    fun searchMediaItems(@Header("Authorization") authorization: String, @Body searchMediaItemsRequest: SearchMediaItemsRequest): Observable<SearchMediaItemsResponse>

    companion object {
        fun create(): GooglePhotoService {

            val client = OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
                .build()

            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .baseUrl("https://photoslibrary.googleapis.com/v1/")
                .build()

            return retrofit.create(GooglePhotoService::class.java)
        }
    }
}