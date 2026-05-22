package com.example.bitirmeapp.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

interface MoodApiService {

    // EMA + pasif feature'lar. Eksik pasif veriyi backend medyanla doldurur.
    @POST("predict/mobile")
    suspend fun mobilTahmin(@Body istek: TahminIstegi): TahminCevabi
}
