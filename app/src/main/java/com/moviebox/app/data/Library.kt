package com.moviebox.app.data

import androidx.compose.runtime.mutableStateListOf

data class Title(val name: String, val meta: String, val rating: Double, val savedOn: String)
data class DownloadItem(val name: String, val sizeMb: Float, val paused: Boolean)

/**
 * In-memory sample data so every screen has something to show.
 * Replace with a real source (Room database / your API) later.
 */
object Library {
    val watchHistory = mutableStateListOf("Good Boy [Hindi]", "SPY x FAMILY [Hindi]", "Gachiakuta [Hindi]", "Gintama")
    val myList = mutableStateListOf(
        Title("Mushoku Tensei: Jobless Reincarnation [Hindi]", "2024 | Anime | Action | Adventure", 8.2, "Nov. 15")
    )
    val downloading = mutableStateListOf(
        DownloadItem("Mushoku Tensei S03 E07", 161.5f, true),
        DownloadItem("Gachiakuta [Hindi] S01 E22", 105.8f, true)
    )
}
