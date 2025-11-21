package com.stqf.academy.network


import com.stqf.academy.model.BookApiModel
import retrofit2.Call
import retrofit2.http.GET

interface HafeziApiService {

    @GET("get_hafezi_quran.php")
    fun getHafeziQuran(): Call<List<BookApiModel>>
}
