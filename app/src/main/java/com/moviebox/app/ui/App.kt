package com.moviebox.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.moviebox.app.data.Movie
import com.moviebox.app.data.Prefs
import com.moviebox.app.data.SavedLinks

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("home", "Home", Icons.Outlined.Home),
    Tab("downloads", "Downloads", Icons.Outlined.Download),
    Tab("me", "Me", Icons.Outlined.Person)
)

@Composable
fun MovieBoxApp(prefs: Prefs, links: SavedLinks) {
    val nav = rememberNavController()
    val route = nav.currentBackStackEntryAsState().value?.destination?.route
    Scaffold(
        containerColor = Bg,
        bottomBar = {
            if (tabs.any { it.route == route }) {
                NavigationBar(containerColor = NavBg) {
                    tabs.forEach { t ->
                        NavigationBarItem(
                            selected = route == t.route,
                            onClick = {
                                nav.navigate(t.route) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(t.icon, t.label) },
                            label = { Text(t.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Accent, selectedTextColor = Accent,
                                unselectedIconColor = Muted, unselectedTextColor = Muted,
                                indicatorColor = NavBg
                            )
                        )
                    }
                }
            }
        }
    ) { pad ->
        NavHost(nav, startDestination = "home", modifier = Modifier.padding(pad)) {
            composable("home") { HomeScreen(onOpen = { Selected.movie = it; nav.navigate("details") }) }
            composable("details") { DetailsScreen(onBack = { nav.popBackStack() }, onPlay = { Selected.playUrl = it; nav.navigate("player") }) }
            composable("player") { PlayerScreen(onBack = { nav.popBackStack() }) }
            composable("downloads") { DownloadsScreen() }
            composable("me") { MeScreen(onOpen = { nav.navigate(it) }) }
            composable("mylist") { MyListScreen(onBack = { nav.popBackStack() }, onOpen = { Selected.movie = Movie(it.id, it.name, it.meta); nav.navigate("details") }) }
            composable("weblink") { WebLinkScreen(links, onBack = { nav.popBackStack() }, onOpenUrl = { BrowserTarget.url = it; nav.navigate("browser") }) }
            composable("browser") { BrowserScreen(links, onBack = { nav.popBackStack() }) }
            composable("settings") { SettingsScreen(prefs, onBack = { nav.popBackStack() }, onOpen = { nav.navigate(it) }) }
            composable("watchoptions") { WatchOptionsScreen(prefs, onBack = { nav.popBackStack() }) }
        }
    }
}
