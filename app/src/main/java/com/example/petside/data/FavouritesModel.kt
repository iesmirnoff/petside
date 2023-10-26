package com.example.petside.data

import com.google.gson.annotations.SerializedName

data class FavouriteItem (
    val id: Number,
    @SerializedName("image_id")
    val imageId: String,
    @SerializedName("sub_id")
    val subId: Any,
    @SerializedName("created_at")
    val createdAt: String,
    val image: FavouriteImage,
)

data class FavouriteImage (
    val id: String,
    val url: String,
)

