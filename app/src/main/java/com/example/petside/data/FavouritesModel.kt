package com.example.petside.data

data class FavouriteItem (
    val id: Number,
    val image_id: String,
    val sub_id: Any,
    val created_at: String,
    val image: FavouriteImage,
)

data class FavouriteImage (
    val id: String,
    val url: String,
)

