package com.example.testfilepickercopyingandparsing.entity

import com.google.gson.annotations.SerializedName

data class Station (
    val id: Int,
    val game: Game,
    val name: String,
    val genre: String,
    val tags: List<Tag>,
    val picLink: String,
    val msTotalLength: Long,
    val songs: List<Song>,
)