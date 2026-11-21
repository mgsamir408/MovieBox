package com.moviebox.app.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.moviebox.app.data.Archive
import com.moviebox.app.data.Library
import com.moviebox.app.data.Movie
import com.moviebox.app.data.MovieDetails
import com.moviebox.app.data.enqueueDownload

/** The movie the details / player screens should show. */
object Selected {
    var movie by mutableStateOf<Movie?>(null)
    var playUrl by mutableStateOf("")
}

private fun poster(id: String) = "https://archive.org/services/img/$id"

private data class Cat(val name: String, val q: String)

private const val BASE = "collection:feature_films AND mediatype:movies"
private val cats = listOf(
    Cat("Trending", BASE),
    Cat("Sci-Fi", "$BASE AND subject:(sci-fi OR \"science fiction\")"),
    Cat("Horror", "$BASE AND subject:horror"),
    Cat("Comedy", "$BASE AND subject:comedy"),
    Cat("Western", "$BASE AND subject:western"),
    Cat("Cartoons", "collection:animationandcartoons AND mediatype:movies"),
    Cat("Classic TV", "collection:classic_tv AND mediatype:movies")
)

@Composable
private fun PosterCard(m: Movie, modifier: Modifier, onOpen: (Movie) -> Unit) {
    Column(modifier.clickable { onOpen(m) }) {
        AsyncImage(
            model = poster(m.id), contentDescription = m.title, contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().aspectRatio(0.7f).clip(RoundedCornerShape(10.dp)).background(Field)
        )
        Text(m.title, color = Color.White, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 6.dp))
    }
}

@Composable
private fun CategoryRow(cat: Cat, onOpen: (Movie) -> Unit, onAll: () -> Unit) {
    var items by remember(cat) { mutableStateOf<List<Movie>>(emptyList()) }
    LaunchedEffect(cat) { try { items = Archive.query(cat.q, 15) } catch (e: Exception) { } }
    if (items.isEmpty()) return
    Column {
        SectionHeader(cat.name, "All", onRight = onAll)
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(items, key = { it.id }) { m -> PosterCard(m, Modifier.width(110.dp), onOpen) }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
fun HomeScreen(onOpen: (Movie) -> Unit) {
    var query by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf("") }
    var sel by remember { mutableStateOf(-1) } // -1 = All (rows for every category)
    var grid by remember { mutableStateOf<List<Movie>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }
    LaunchedEffect(submitted, sel) {
        if (submitted.isEmpty() && sel < 0) return@LaunchedEffect
        loading = true; error = false
        try { grid = if (submitted.isNotEmpty()) Archive.search(submitted) else Archive.query(cats[sel].q, 60) }
        catch (e: Exception) { error = true; grid = emptyList() }
        loading = false
    }
    fun search() { submitted = query.trim(); if (submitted.isNotEmpty()) sel = -1 }

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
                    textStyle = TextStyle(color = Color.White, fontSize = 14.sp), cursorBrush = SolidColor(Accent),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { search() }),
                    decorationBox = { inner ->
                        if (query.isEmpty()) Text("Search movies", color = Muted, fontSize = 14.sp)
                        inner()
                    }
                )
                Text("Search", color = Accent, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { search() }.padding(start = 8.dp))
            }
        }
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val tabs = listOf("All") + cats.map { it.name }
            tabs.forEachIndexed { i, name ->
                val on = submitted.isEmpty() && sel == i - 1
                Box(
                    Modifier.height(36.dp).clip(RoundedCornerShape(18.dp)).background(if (on) Accent else Field)
                        .clickable { sel = i - 1; submitted = ""; query = "" }.padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) { Text(name, color = if (on) OnAccent else Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium) }
            }
        }
        Spacer(Modifier.height(12.dp))
        when {
            submitted.isEmpty() && sel < 0 -> LazyColumn(Modifier.fillMaxSize()) {
                items(cats.size) { i -> CategoryRow(cats[i], onOpen, onAll = { sel = i }) }
            }
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Accent) }
            error -> Placeholder("Could not load movies. Check your internet.")
            grid.isEmpty() -> Placeholder("No movies found")
            else -> LazyVerticalGrid(
                GridCells.Fixed(3), contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(grid, key = { it.id }) { m -> PosterCard(m, Modifier, onOpen) }
            }
        }
    }
}

@Composable
fun DetailsScreen(onBack: () -> Unit, onPlay: (String) -> Unit) {
    val m = Selected.movie ?: return
    val ctx = LocalContext.current
    var d by remember(m.id) { mutableStateOf<MovieDetails?>(null) }
    var failed by remember(m.id) { mutableStateOf(false) }
    LaunchedEffect(m.id) { try { d = Archive.details(m.id) } catch (e: Exception) { failed = true } }
    val inList = Library.myList.any { it.id == m.id }
    val video = d?.videoUrl
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        TopBar(m.title, onBack)
        Row(Modifier.padding(16.dp)) {
            AsyncImage(
                model = poster(m.id), contentDescription = m.title, contentScale = ContentScale.Crop,
                modifier = Modifier.size(120.dp, 170.dp).clip(RoundedCornerShape(12.dp)).background(Field)
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(m.title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                if (m.year.isNotEmpty()) Text(m.year, color = Muted, modifier = Modifier.padding(top = 6.dp))
                Text("Public domain", color = Accent, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))
            }
        }
        GradientButton("Play") {
            if (video != null) onPlay(video) else Toast.makeText(ctx, if (failed) "Could not load" else "Please wait…", Toast.LENGTH_SHORT).show()
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Pill("Download", Modifier.weight(1f)) {
                if (video != null) enqueueDownload(ctx, video, m.title.replace(Regex("[^\\p{L}\\p{N} ]"), "").trim().ifEmpty { m.id } + ".mp4")
                else Toast.makeText(ctx, "Please wait…", Toast.LENGTH_SHORT).show()
            }
            Pill(if (inList) "In My List" else "Add to My List", Modifier.weight(1f)) { Library.toggle(m) }
        }
        if (d != null && video == null) Text("No playable video found for this title.", color = Muted, modifier = Modifier.padding(16.dp))
        val desc = d?.description.orEmpty()
        if (desc.isNotEmpty()) Text(desc, color = Muted, fontSize = 14.sp, lineHeight = 20.sp, modifier = Modifier.padding(16.dp))
    }
}

@Composable
private fun Pill(text: String, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier.height(48.dp).clip(RoundedCornerShape(14.dp)).background(Card).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) { Text(text, color = Color.White, fontWeight = FontWeight.Medium) }
}

@Composable
fun PlayerScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val url = Selected.playUrl
    val player = remember(url) {
        ExoPlayer.Builder(ctx).build().apply { setMediaItem(MediaItem.fromUri(url)); prepare(); playWhenReady = true }
    }
    DisposableEffect(player) { onDispose { player.release() } }
    Column(Modifier.fillMaxSize().background(Color.Black)) {
        TopBar(Selected.movie?.title ?: "Playing", onBack)
        AndroidView(factory = { c -> PlayerView(c).apply { this.player = player } }, modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f))
    }
}
