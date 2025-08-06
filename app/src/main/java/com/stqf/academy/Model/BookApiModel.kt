package com.stqf.academy.model

data class BookApiModel(
    val id: Int = 0, // <-- Default value added
    val title: String,
    val description: String,
    val pdf_url: String,
    val thumbnail_url: String
)