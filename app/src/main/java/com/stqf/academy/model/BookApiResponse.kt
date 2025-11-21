package com.stqf.academy.model

data class BookApiResponse(
    val status: String,
    val books: List<BookApiModel>
)
