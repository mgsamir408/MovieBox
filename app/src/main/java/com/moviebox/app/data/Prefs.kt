package com.moviebox.app.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Settings saved on the phone (SharedPreferences), observable by Compose. */
class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("moviebox", Context.MODE_PRIVATE)

    var familyMode by mutableStateOf(sp.getBoolean("family", false)); private set
    var backgroundDownload by mutableStateOf(sp.getBoolean("bg_dl", true)); private set
    var autoMiniplayer by mutableStateOf(sp.getBoolean("mini", true)); private set
    var watchMode by mutableStateOf(sp.getString("watch", "Streaming") ?: "Streaming"); private set

    fun updateFamilyMode(v: Boolean) { familyMode = v; sp.edit().putBoolean("family", v).apply() }
    fun updateBackgroundDownload(v: Boolean) { backgroundDownload = v; sp.edit().putBoolean("bg_dl", v).apply() }
    fun updateAutoMiniplayer(v: Boolean) { autoMiniplayer = v; sp.edit().putBoolean("mini", v).apply() }
    fun updateWatchMode(v: String) { watchMode = v; sp.edit().putString("watch", v).apply() }
}
