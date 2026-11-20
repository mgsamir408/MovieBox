package com.moviebox.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.moviebox.app.data.Library
import com.moviebox.app.data.Prefs
import com.moviebox.app.data.SavedLinks
import com.moviebox.app.ui.MovieBoxApp
import com.moviebox.app.ui.MovieBoxTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val prefs = Prefs(applicationContext)
        val links = SavedLinks(applicationContext)
        Library.init(applicationContext)
        setContent { MovieBoxTheme { MovieBoxApp(prefs, links) } }
    }
}
