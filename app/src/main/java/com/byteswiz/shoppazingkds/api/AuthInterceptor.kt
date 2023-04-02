package com.byteswiz.shoppazingkds.api

import android.content.Context
import com.byteswiz.shoppazingkds.utils.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(context: Context) : Interceptor {
    private val sessionManager = SessionManager(context)

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()

        // If token has been saved, add it to the request
        sessionManager.fetchAuthToken()?.let {
            requestBuilder.addHeader("Authorizations", "Bearer $it")
        }
        requestBuilder.addHeader("Content-Type","application/x-www-form-urlencoded")
        return chain.proceed(requestBuilder.build())
    }
}