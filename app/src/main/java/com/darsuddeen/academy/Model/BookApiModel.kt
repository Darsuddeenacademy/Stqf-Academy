package com.darsuddeen.academy.model

data class BookApiModel(
    val id: String,
    val title: String,
    val description: String,
    val pdf_url: String,
    val thumbnail_url: String
)