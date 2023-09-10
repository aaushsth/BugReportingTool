package com.outcode.clickupapp.reportTool

import android.content.Context
import android.net.Uri
import android.util.Log
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.io.File
import java.io.IOException


object ClickUpTaskCreator {

    private const val API_KEY = "you personal token"
    private const val SPACE_ID = "sapce id"

    private fun File.imageToMultiPart(partName: String, fileName: String): MultipartBody.Part {
        val requestFile = this.asRequestBody("multipart/form-data;".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, fileName, requestFile)
    }
    @OptIn(DelicateCoroutinesApi::class)
    fun createTask(
        appContext: Context,
        taskName: String,
        taskDescription: String,
        tags: List<String>?,
        priority: Int? = 1,
        imageFile: Uri?
    ) {

        var imageMultipartBody: MultipartBody.Part? = null
        val deviceImageFile = File(imageFile?.path)
        imageMultipartBody = deviceImageFile.imageToMultiPart("attachment", "attachment")

        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor(ChuckerInterceptor.Builder(appContext).build())
        httpClient.addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .header("Authorization", API_KEY)
            val request = requestBuilder.build()
            chain.proceed(request)
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.clickup.com/api/v2/")
            .client(httpClient.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)


        GlobalScope.launch(Dispatchers.IO) {
            val model = CreateTaskRequest(
                tags = tags,
                name = taskName,
                description = taskDescription,
                priority = priority ?: 1
            )
            try {
                val response = apiService.createTask(spaceId = SPACE_ID, model)
                if (response.isSuccessful) {
                    val taskId = response.body()?.id
                    val attachmentResponse =
                        taskId?.let { apiService.uploadImage(it,imageMultipartBody) }

                    Log.e("attachmentResponse", "response:$attachmentResponse")


                } else {
                    Log.e("ClickUpTaskCreator", "error:$response")
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}

interface ApiService {
    companion object {
        private const val KEY_SPACE_ID = "KEY_SPACE_ID"
        private const val TASK_ID = "TASK_ID"

        private const val CREATE_TASK = "list/{$KEY_SPACE_ID}/task"
        private const val UPDATE_TASK = "task/{$TASK_ID}/attachment"

    }

    @Multipart
    @POST(UPDATE_TASK)
    suspend fun uploadImage(
        @Path(TASK_ID) taskId: String,
        @Part image: MultipartBody.Part?
    ): Response<BaseResponseEntity>

    @POST(CREATE_TASK)
    suspend fun createTask(
        @Path(KEY_SPACE_ID) spaceId: String,
        @Body request: CreateTaskRequest
    ): Response<BaseResponseEntity>
}

open class BaseResponseEntity(
    @SerializedName("id")
    open var id: String? = null,
    @SerializedName("message")
    open var message: String? = null,
    @SerializedName("detail")
    open var detail: String? = null,
    @SerializedName("error_code")
    var errorCode: String? = null,
    @SerializedName("is_hidden")
    var isHidden: Boolean = false,
)

open class CreateTaskRequest(
    @SerializedName("notify_all")
    open var notifyAll: Boolean? = true,
    @SerializedName("tags")
    open var tags: List<String>? = emptyList(),
    @SerializedName("description")
    open var description: String? = null,
    @SerializedName("name")
    var name: String? = null,
    @SerializedName("priority")
    var priority: Int = 1,
)
