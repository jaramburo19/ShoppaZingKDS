package com.byteswiz.shoppazingkds.api

import android.content.Context
import com.byteswiz.shoppazingkds.utils.Constants

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Duration
import java.util.concurrent.TimeUnit


class ApiClient {
    private lateinit var apiService: ApiService


    fun getApiService(context: Context): ApiService {

        // Initialize ApiService if not initialized yet
        if (!::apiService.isInitialized) {
            val retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(okhttpClient(context))
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            apiService = retrofit.create(ApiService::class.java)

        }

        return apiService
    }



    /**
     * Initialize OkhttpClient with our interceptor
     */
    private fun okhttpClient(context: Context): OkHttpClient {

        return OkHttpClient.Builder()
            .readTimeout(120, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor(context))
            .build()
    }



}