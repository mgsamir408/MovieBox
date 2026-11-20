package com.moviebox.app.data

import androidx.compose.runtime.mutableStateListOf

data class Title(val name: String, val meta: String, val rating: Double, val savedOn: String)

/**
 * In-memory lists for Watch History and My List (start empty).
 * Replace with a Room database later so they survive restarts.
 */
object Library {
    val watchHistory = mutableStateListOf<String>()
    val myList = mutableStateListOf<Title>()
}
