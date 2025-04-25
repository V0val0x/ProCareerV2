package com.example.procareerv2.data.remote.interceptor

import com.example.procareerv2.data.local.PreferencesManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val preferencesManager: PreferencesManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Don't add auth headers for auth endpoints
        if (originalRequest.url.encodedPath.startsWith("/auth/")) {
            return chain.proceed(originalRequest)
        }

        val modifiedRequest = originalRequest.newBuilder().apply {
            val userId = preferencesManager.getUserId()
            if (userId != null) {
                addHeader("Id", userId)
            }
            preferencesManager.getAuthToken()?.let { token ->
                addHeader("Token", token)
            }
        }.build()

        return chain.proceed(modifiedRequest)
    }
}
