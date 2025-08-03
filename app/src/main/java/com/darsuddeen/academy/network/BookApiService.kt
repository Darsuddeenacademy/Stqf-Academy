package com.darsuddeen.academy.network

import com.darsuddeen.academy.model.BookApiResponse
import retrofit2.Call
import retrofit2.http.GET

interface BookApiService {
    @GET("books_api.php")
    fun getBooks(): Call<BookApiResponse>
}
