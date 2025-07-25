// network/ApiService.kt
package com.example.mysilgurae.network

import com.example.mysilgurae.data.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    // [수정!] 새로운 API 명세에 맞는 엔드포인트로 변경합니다.
    @GET("getRTMSDataSvcAptTrade")
    suspend fun getApartmentDeals(
        @Query("serviceKey", encoded = true) serviceKey: String,
        @Query("LAWD_CD") lawdCd: String, // 지역 코드
        @Query("DEAL_YMD") dealYmd: String, // 계약년월
        @Query("pageNo") pageNo: Int = 1,
        @Query("numOfRows") numOfRows: Int = 50
    ): ApiResponse
}
