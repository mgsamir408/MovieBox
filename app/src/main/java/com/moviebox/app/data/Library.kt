package com.moviebox.app.data

import androidx.compose.runtime.mutableStateListOf

data class Title(val name: String, val meta: String, val rating: Double, val savedOn: String)

/**
 * In-memory sample data so every screen has something to show.
 * Replace with a real source (Room database / your API) later.
 */
object Library {
    val watchHistory = mutableStateListOf("Good Boy [Hindi]", "SPY x FAMILY [Hindi]", "Gachiakuta [Hindi]", "Gintama")
    val myList = mutableStateListOf(
        Title("Mushoku Tensei: Jobless Reincarnation [Hindi]", "2024 | Anime | Action | Adventure", 8.2, "Nov. 15")
    )
}
