package com.darsuddeen.academy.Model

data class BookModel(
    val title: String,
    val assetFileName: String,
    val thumbnailResId: Int // Drawable resource ID (e.g. R.drawable.book1_thumb)
)
