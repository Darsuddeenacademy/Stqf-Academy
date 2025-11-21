package com.stqf.academy.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private const val BASE_URL_BOOK = "https://darsuddeenacademy.com/"   // তোমার পুরনো সাইট
    private const val BASE_URL_QURAN = "https://stqf.org/"               // নতুন Qur'an API

    // 🔹 বইয়ের জন্য
    val bookApi: BookApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_BOOK)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BookApiService::class.java)
    }

    // 🔹 কালার কুরআনের জন্য
    val quranApi: QuranApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_QURAN)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(QuranApiService::class.java)
    }

    // 🟢 নতুন Hafezi API
    val hafeziApi: HafeziApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_QURAN)         // stqf.org ব্যবহার করবো
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HafeziApiService::class.java)
    }
}
