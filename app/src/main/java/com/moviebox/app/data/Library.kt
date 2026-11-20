package com.moviebox.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateListOf
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Title(val id: String, val name: String, val meta: String, val savedOn: String)

/** Watch History (in memory) and My List (saved on the phone). */
object Library {
    val watchHistory = mutableStateListOf<String>()
    val myList = mutableStateListOf<Title>()
    private var sp: SharedPreferences? = null

    fun init(ctx: Context) {
        if (sp != null) return
        val prefs = ctx.getSharedPreferences("moviebox_lib", Context.MODE_PRIVATE)
        sp = prefs
        val arr = JSONArray(prefs.getString("list", "[]"))
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            myList.add(Title(o.getString("id"), o.getString("name"), o.optString("meta"), o.optString("on")))
        }
    }

    fun toggle(m: Movie) {
        val old = myList.find { it.id == m.id }
        if (old != null) myList.remove(old)
        else myList.add(0, Title(m.id, m.title, m.year, SimpleDateFormat("MMM. d", Locale.ENGLISH).format(Date())))
        val arr = JSONArray()
        myList.forEach { arr.put(JSONObject().put("id", it.id).put("name", it.name).put("meta", it.meta).put("on", it.savedOn)) }
        sp?.edit()?.putString("list", arr.toString())?.apply()
    }
}
