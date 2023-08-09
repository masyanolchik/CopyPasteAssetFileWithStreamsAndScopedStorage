package com.example.testfilepickercopyingandparsing.entity

data class Song(
    val id: Int,
    val prevSongId: Int,
    val nextSongId: Int,
    val artist: String,
    val title: String,
    val msOffset: Long,
    val link: String,
)
