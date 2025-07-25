// network/RetrofitClient.kt
package com.example.mysilgurae.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory

object RetrofitClient {
    // [수정!] 공공데이터포털의 공식 URL로 변경합니다.
    private const val BASE_URL = "https://apis.data.go.kr/1613000/RTMSDataSvcAptTrade/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .build()
        retrofit.create(ApiService::class.java)
    }
}
