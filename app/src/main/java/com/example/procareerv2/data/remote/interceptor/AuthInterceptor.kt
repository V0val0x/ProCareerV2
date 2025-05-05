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
        
        // u041du0435 u0434u043eu0431u0430u0432u043bu044fu0435u043c u0437u0430u0433u043eu043bu043eu0432u043au0438 u0434u043bu044f u044du043du0434u043fu043eu0438u043du0442u043eu0432 u0430u0432u0442u043eu0440u0438u0437u0430u0446u0438u0438
        if (path.startsWith("/auth/")) {
            Log.d("AuthInterceptor", "Skipping auth headers for $path")
            return chain.proceed(originalRequest)
        }

        val token = preferencesManager.getAuthToken()
        Log.d("AuthInterceptor", "Adding auth headers for $path: token=${token?.take(15)}...")
        
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
