package com.example.procareerv2.data.remote.interceptor

import android.util.Log
import com.example.procareerv2.data.local.PreferencesManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val preferencesManager: PreferencesManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath
        
        // Don't add headers for auth endpoints
        if (path.startsWith("/auth/")) {
            Log.d("AuthInterceptor", "Skipping auth headers for $path")
            return chain.proceed(originalRequest)
        }

        val token = preferencesManager.getAuthToken()
        Log.d("AuthInterceptor", "Adding auth headers for $path: token=${token?.take(15)}..., full URL: ${originalRequest.url}")
        
        val modifiedRequest = if (!token.isNullOrBlank()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            Log.e("AuthInterceptor", "No auth token available for $path")
            originalRequest
        }
        
        return chain.proceed(modifiedRequest)
    }
}
