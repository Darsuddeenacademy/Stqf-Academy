package com.stqf.academy.network

import com.stqf.academy.model.BookApiModel
import retrofit2.Call
import retrofit2.http.GET

interface QuranApiService {

    // Color Qur'an API (stqf.org)
    @GET("get_quran.php")
    fun getQuran(): Call<List<BookApiModel>>
}