package com.moviebox.app.ui

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import kotlinx.coroutines.delay
import com.moviebox.app.data.DlItem
import com.moviebox.app.data.Library
import com.moviebox.app.data.SavedLinks
import com.moviebox.app.data.formatBytes
import com.moviebox.app.data.queryDownloads
import com.moviebox.app.data.Prefs

/* ---------------- Home ---------------- */
@Composable
fun HomeScreen() {
    var query by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // App logo (replace with your real logo: painterResource(R.drawable.logo))
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Gradient), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.PlayArrow, "MovieBox", tint = OnAccent, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(12.dp))
            Row(
                Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(12.dp)).background(Field).padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Search, null, tint = Muted, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                BasicTextField(
                    query, { query = it }, Modifier.weight(1f), singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp),
                    cursorBrush = SolidColor(Accent),
                    decorationBox = { inner ->
                        if (query.isEmpty()) Text("Search movies, series, anime", color = Muted, fontSize = 14.sp)
                        inner()
                    }
                )
                Text("Search", color = Accent, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { /* TODO: search the movie catalog */ }.padding(start = 8.dp))
            }
        }
        Placeholder("Movies will appear here", Modifier.weight(1f).padding(bottom = 16.dp))
    }
}

@Composable
private fun SiteRow(url: String, onClick: () -> Unit, trailing: (@Composable () -> Unit)? = null) {
    val host = Uri.parse(url).host?.removePrefix("www.") ?: url
    Row(Modifier.fillMaxWidth().clickable { onClick() }.heightIn(min = 64.dp).padding(start = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(Field), contentAlignment = Alignment.Center) {
            Text(host.take(1).uppercase(), color = Accent, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(host, color = Color.White, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(url, color = Muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (trailing != null) trailing() else Spacer(Modifier.width(16.dp))
    }
}

/* ---------------- Downloads ---------------- */
private fun freeSpace(): String {
    val s = StatFs(Environment.getDataDirectory().path)
    return "%.1f GB available".format(s.availableBytes / 1_073_741_824.0)
}

@Composable
fun DownloadsScreen() {
    val ctx = LocalContext.current
    val dm = remember { ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager }
    var items by remember { mutableStateOf(emptyList<DlItem>()) }
    var manage by remember { mutableStateOf(false) }
    val free = remember { freeSpace() }
    LaunchedEffect(Unit) { while (true) { items = queryDownloads(ctx); delay(1500) } }
    val active = items.filter { it.active }
    val done = items.filter { !it.active }

    @Composable
    fun Row2(d: DlItem) {
        Row(
            Modifier.fillMaxWidth().clickable(enabled = !d.active) {
                try {
                    ctx.startActivity(
                        Intent(Intent.ACTION_VIEW).setDataAndType(dm.getUriForDownloadedFile(d.id), d.mime ?: "*/*")
                            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                } catch (e: Exception) { Toast.makeText(ctx, "No app can open this file", Toast.LENGTH_SHORT).show() }
            }.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(d.title, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                val size = if (d.active && d.total > 0) "${formatBytes(d.done)} / ${formatBytes(d.total)}" else formatBytes(if (d.total > 0) d.total else d.done)
                Text(size, color = Muted, fontSize = 13.sp)
            }
            if (d.active) {
                val (label, col) = when (d.status) {
                    DownloadManager.STATUS_FAILED -> "Failed" to Color(0xFFE5484D)
                    DownloadManager.STATUS_PAUSED -> "Paused" to Color(0xFFE5484D)
                    DownloadManager.STATUS_PENDING -> "Waiting" to Muted
                    else -> "Downloading" to Accent
                }
                Text(label, color = col, fontSize = 13.sp)
            }
            if (manage) IconButton({ dm.remove(d.id) }) { Icon(Icons.Outlined.Delete, "Delete", tint = Color(0xFFE5484D)) }
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = 16.dp)) {
        SectionHeader("Downloading (${active.size})", if (manage) "Done" else "Manage", rightAccent = true, onRight = { manage = !manage })
        if (active.isEmpty()) Placeholder("No active downloads") else active.forEach { Row2(it) }
        Spacer(Modifier.height(24.dp))
        SectionHeader("Downloaded", free)
        if (done.isEmpty()) Placeholder("Finished downloads appear here") else done.forEach { Row2(it) }
    }
}

/* ---------------- Me ---------------- */
private val tileColors = listOf(
    listOf(Color(0xFFF5B73A), Color(0xFF3A6BD8)), listOf(Color(0xFF5AB0FF), Color(0xFF2A6A58)),
    listOf(Color(0xFFD94F3A), Color(0xFF7A2A1A)), listOf(Color(0xFFE86AA8), Color(0xFF6A5AD8))
)

@Composable
fun MeScreen(onOpen: (String) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = 16.dp)) {
        CardBox {
            NavRow({ Icon(Icons.Outlined.History, null, tint = Color.White) }, "Watch History") { /* TODO: history screen */ }
            if (Library.watchHistory.isNotEmpty()) Row(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Library.watchHistory.take(4).forEachIndexed { i, name ->
                    Column(Modifier.weight(1f)) {
                        Box(Modifier.fillMaxWidth().aspectRatio(0.75f).clip(RoundedCornerShape(10.dp)).background(Brush.linearGradient(tileColors[i % 4])))
                        Text(name, color = Muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }
            HorizontalDivider(color = Divider, modifier = Modifier.padding(horizontal = 16.dp))
            NavRow({ Icon(Icons.Outlined.Bookmark, null, tint = Color.White) }, "My List", "${Library.myList.size}") { onOpen("mylist") }
        }
        CardBox { NavRow({ Icon(Icons.Outlined.Link, null, tint = Color.White) }, "Web Link", badge = "NEW") { onOpen("weblink") } }
        CardBox { NavRow({ Icon(Icons.Outlined.Settings, null, tint = Color.White) }, "Settings") { onOpen("settings") } }
    }
}

/* ---------------- My List ---------------- */
@Composable
fun MyListScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        TopBar("My List", onBack)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
            Library.myList.groupBy { it.savedOn }.forEach { (date, titles) ->
                Text(date, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 12.dp))
                titles.forEach { t ->
                    Row(Modifier.padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(64.dp, 88.dp).clip(RoundedCornerShape(8.dp)).background(Brush.linearGradient(tileColors[1])))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(t.name, color = Color.White, fontWeight = FontWeight.Medium)
                            Row(Modifier.padding(top = 6.dp)) {
                                Text("★ ${t.rating}", color = Color(0xFFF5B73A), fontSize = 13.sp)
                                Text("  |  ${t.meta}", color = Muted, fontSize = 13.sp)
                            }
                        }
                        RoundIcon(Color(0xFFD9DBE0)) { Icon(Icons.Filled.PlayArrow, "Play", tint = Bg) }
                    }
                }
            }
            Text(
                if (Library.myList.isEmpty()) "Nothing saved yet" else "— No more content —",
                color = Muted, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            )
        }
    }
}

/* ---------------- Web Link ---------------- */
@Composable
fun WebLinkScreen(links: SavedLinks, onBack: () -> Unit, onOpenUrl: (String) -> Unit) {
    val ctx = LocalContext.current
    var link by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        TopBar("Web Link", onBack)
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Paste a website link to save it", color = Muted, fontSize = 14.sp)
            BasicTextField(
                link, { link = it }, Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Field).padding(16.dp),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp), cursorBrush = SolidColor(Accent),
                decorationBox = { inner -> if (link.isEmpty()) Text("https://", color = Muted, fontSize = 14.sp); inner() }
            )
            GradientButton("Save link") {
                if (links.add(link)) link = "" else Toast.makeText(ctx, "This is not a valid link", Toast.LENGTH_SHORT).show()
            }
        }
        if (links.items.isNotEmpty()) {
            SectionHeader("Saved links", "${links.items.size}")
            CardBox {
                links.items.toList().forEach { url ->
                    SiteRow(url, { onOpenUrl(url) }) {
                        IconButton({ links.remove(url) }) { Icon(Icons.Outlined.Delete, "Remove", tint = Muted) }
                    }
                }
            }
        }
    }
}

/* ---------------- Settings ---------------- */
@Composable
fun SettingsScreen(prefs: Prefs, onBack: () -> Unit, onOpen: (String) -> Unit) {
    val ctx = LocalContext.current
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        TopBar("Settings", onBack)
        Text("Manage your app and preferences", color = Muted, fontSize = 14.sp, modifier = Modifier.padding(16.dp))
        CardBox {
            NavRow({}, "Notifications") {
                ctx.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, ctx.packageName))
            }
            HorizontalDivider(color = Divider, modifier = Modifier.padding(horizontal = 16.dp))
            NavRow({}, "Language") { /* TODO: language picker */ }
            HorizontalDivider(color = Divider, modifier = Modifier.padding(horizontal = 16.dp))
            NavRow({}, "Watch Options", prefs.watchMode) { onOpen("watchoptions") }
            HorizontalDivider(color = Divider, modifier = Modifier.padding(horizontal = 16.dp))
            ToggleRow("Family Mode", prefs.familyMode, "This helps hide potentially mature videos. No filter is 100% accurate.") { prefs.updateFamilyMode(it) }
            HorizontalDivider(color = Divider, modifier = Modifier.padding(horizontal = 16.dp))
            ToggleRow("Download in background", prefs.backgroundDownload) { prefs.updateBackgroundDownload(it) }
            HorizontalDivider(color = Divider, modifier = Modifier.padding(horizontal = 16.dp))
            ToggleRow("Auto activate Miniplayer", prefs.autoMiniplayer, "When enabled, switching pages during playback will automatically enable Miniplayer.") { prefs.updateAutoMiniplayer(it) }
        }
    }
}

/* ---------------- Watch Options ---------------- */
@Composable
fun WatchOptionsScreen(prefs: Prefs, onBack: () -> Unit) {
    val ctx = LocalContext.current
    var sel by remember { mutableStateOf(prefs.watchMode) }
    Column(Modifier.fillMaxSize()) {
        TopBar("Watch Options", onBack)
        Text("Changes will take effect after restarting the app", color = Muted, fontSize = 14.sp, modifier = Modifier.padding(16.dp))
        CardBox {
            OptionRow("Streaming", "You can stream videos instantly", sel == "Streaming") { sel = "Streaming" }
            HorizontalDivider(color = Divider, modifier = Modifier.padding(horizontal = 16.dp))
            OptionRow("Download", "You need to download this video to watch it", sel == "Download") { sel = "Download" }
        }
        Spacer(Modifier.height(8.dp))
        GradientButton("Restart to Apply") {
            prefs.updateWatchMode(sel)
            val launch = ctx.packageManager.getLaunchIntentForPackage(ctx.packageName)
            if (launch != null) {
                ctx.startActivity(Intent.makeRestartActivityTask(launch.component))
                Runtime.getRuntime().exit(0)
            }
        }
    }
}

@Composable
private fun OptionRow(title: String, desc: String, selected: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Medium)
            Text(desc, color = Muted, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
        }
        if (selected) RoundIcon(Accent, 26) { Icon(Icons.Filled.Check, null, tint = OnAccent, modifier = Modifier.size(16.dp)) }
        else Box(Modifier.size(26.dp).clip(CircleShape).border(2.dp, Color(0xFF6A6D78), CircleShape))
    }
}
