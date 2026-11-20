package com.moviebox.app.data

import android.content.Context
import android.util.Patterns
import androidx.compose.runtime.mutableStateListOf

/** Websites the user saved (Me > Web Link). Stored on the phone. */
class SavedLinks(context: Context) {
    private val sp = context.getSharedPreferences("moviebox_links", Context.MODE_PRIVATE)
    val items = mutableStateListOf<String>().apply {
        addAll((sp.getString("urls", "") ?: "").split("\n").filter { it.isNotBlank() })
    }

    /** Returns false if the text is not a valid link. */
    fun add(raw: String): Boolean {
        val url = normalize(raw) ?: return false
        if (url !in items) { items.add(0, url); persist() }
        return true
    }

    fun remove(url: String) { items.remove(url); persist() }

    private fun persist() { sp.edit().putString("urls", items.joinToString("\n")).apply() }

    companion object {
        fun normalize(raw: String): String? {
            val t = raw.trim()
            if (t.isEmpty() || t.contains(' ')) return null
            val u = if (t.startsWith("http://") || t.startsWith("https://")) t else "https://$t"
            return if (Patterns.WEB_URL.matcher(u).matches()) u else null
        }
    }
}
