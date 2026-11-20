package com.moviebox.app.ui

import android.app.DownloadManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.URLUtil
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.moviebox.app.data.SavedLinks

/** The page the in-app browser should open next. */
object BrowserTarget { var url by mutableStateOf("") }

private fun startDownload(ctx: Context, url: String, ua: String?, disposition: String?, mime: String?) {
    try {
        val name = URLUtil.guessFileName(url, disposition, mime)
        val req = DownloadManager.Request(Uri.parse(url))
            .addRequestHeader("Cookie", CookieManager.getInstance().getCookie(url) ?: "")
            .addRequestHeader("User-Agent", ua ?: "")
            .setTitle(name)
            .setMimeType(mime)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(ctx, Environment.DIRECTORY_DOWNLOADS, name)
        (ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager).enqueue(req)
        Toast.makeText(ctx, "Download started: $name", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(ctx, "Could not start download", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun BrowserScreen(links: SavedLinks, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val start = remember { BrowserTarget.url }
    var current by remember { mutableStateOf(start) }
    var web by remember { mutableStateOf<WebView?>(null) }
    BackHandler { if (web?.canGoBack() == true) web?.goBack() else onBack() }
    DisposableEffect(Unit) { onDispose { web?.destroy() } }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().height(56.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) }
            Text(
                Uri.parse(current).host ?: current, Modifier.weight(1f), color = Color.White,
                fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            IconButton({
                val ok = links.add(current)
                Toast.makeText(ctx, if (ok) "Link saved" else "Cannot save this link", Toast.LENGTH_SHORT).show()
            }) { Icon(Icons.Outlined.Bookmark, "Save link", tint = Accent) }
        }
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { c ->
                WebView(c).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            if (url != null) current = url
                        }
                    }
                    setDownloadListener(DownloadListener { url, ua, cd, mime, _ -> startDownload(c, url, ua, cd, mime) })
                    loadUrl(start)
                    web = this
                }
            }
        )
    }
}
