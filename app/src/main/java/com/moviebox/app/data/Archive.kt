package com.moviebox.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class Movie(val id: String, val title: String, val year: String)
data class MovieDetails(val description: String, val videoUrl: String?)

/** Free public-domain feature films from archive.org (no API key needed). */
object Archive {
    private fun get(url: String): String {
        val c = URL(url).openConnection() as HttpURLConnection
        c.connectTimeout = 15000
        c.readTimeout = 20000
        c.setRequestProperty("User-Agent", "MovieBox/1.0")
        return c.inputStream.bufferedReader().use { it.readText() }
    }

    private fun enc(s: String) = URLEncoder.encode(s, "UTF-8")

    /** Raw archive.org query, most downloaded first. */
    suspend fun query(q: String, rows: Int = 48): List<Movie> = withContext(Dispatchers.IO) {
        val url = "https://archive.org/advancedsearch.php?q=${enc(q)}" +
            "&fl%5B%5D=identifier&fl%5B%5D=title&fl%5B%5D=year&sort%5B%5D=downloads+desc&rows=$rows&page=1&output=json"
        val docs = JSONObject(get(url)).getJSONObject("response").getJSONArray("docs")
        List(docs.length()) {
            val d = docs.getJSONObject(it)
            Movie(d.getString("identifier"), d.optString("title", "Untitled"), d.optString("year", ""))
        }
    }

    suspend fun search(text: String): List<Movie> {
        val clean = text.replace(Regex("[^\\p{L}\\p{N} ]"), " ").trim()
        val base = "collection:feature_films AND mediatype:movies"
        return query(if (clean.isEmpty()) base else "$base AND title:($clean)")
    }

    suspend fun details(id: String): MovieDetails = withContext(Dispatchers.IO) {
        val j = JSONObject(get("https://archive.org/metadata/$id"))
        val raw = j.optJSONObject("metadata")?.opt("description")?.toString() ?: ""
        val desc = raw.replace(Regex("<[^>]*>"), " ").replace("&nbsp;", " ").replace(Regex("\\s+"), " ").trim().take(700)
        var file: String? = null
        val files = j.optJSONArray("files")
        if (files != null) for (i in 0 until files.length()) {
            val n = files.getJSONObject(i).optString("name")
            if (file == null && n.endsWith(".mp4", ignoreCase = true)) file = n
        }
        val url = file?.let { f -> "https://archive.org/download/$id/" + f.split("/").joinToString("/") { enc(it).replace("+", "%20") } }
        MovieDetails(desc, url)
    }
}
