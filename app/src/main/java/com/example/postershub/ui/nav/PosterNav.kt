package com.example.postershub.ui.nav

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.postershub.ui.about.AboutScreen
import com.example.postershub.ui.detail.DetailScreen
import com.example.postershub.ui.favorites.FavoritesScreen
import com.example.postershub.ui.fullscreen.FullscreenPosterScreen
import com.example.postershub.ui.home.HomeScreen
import com.example.postershub.ui.search.SearchScreen
import com.example.postershub.ui.theme.Ink
import com.example.postershub.ui.theme.InkElevated

private data class Tab(val label: String, val icon: ImageVector, val route: Any)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PosterApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDest = backStackEntry?.destination

    val tabs = listOf(
        Tab("Home", Icons.Filled.Home, HomeRoute),
        Tab("Search", Icons.Filled.Search, SearchRoute),
        Tab("Favorites", Icons.Filled.Favorite, FavoritesRoute),
    )
    val showBar = tabs.any { currentDest?.hasRoute(it.route::class) == true }

    Scaffold(
        containerColor = Ink,
        bottomBar = {
            if (showBar) {
                NavigationBar(containerColor = InkElevated) {
                    tabs.forEach { tab ->
                        val selected = currentDest?.hasRoute(tab.route::class) == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(HomeRoute) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        SharedTransitionLayout(Modifier.padding(padding)) {
            NavHost(navController = navController, startDestination = HomeRoute) {
                composable<HomeRoute> {
                    HomeScreen(
                        sharedScope = this@SharedTransitionLayout,
                        animatedScope = this,
                        onOpenMovie = { movie, key ->
                            navController.navigate(DetailRoute(movie.id, movie.posterPath, movie.title, key, movie.isTv))
                        },
                        onOpenAbout = { navController.navigate(AboutRoute) },
                    )
                }
                composable<AboutRoute> {
                    AboutScreen(onBack = { navController.popBackStack() })
                }
                composable<SearchRoute> {
                    SearchScreen(
                        sharedScope = this@SharedTransitionLayout,
                        animatedScope = this,
                        onOpenMovie = { movie, key ->
                            navController.navigate(DetailRoute(movie.id, movie.posterPath, movie.title, key, movie.isTv))
                        },
                    )
                }
                composable<FavoritesRoute> {
                    FavoritesScreen(
                        sharedScope = this@SharedTransitionLayout,
                        animatedScope = this,
                        onOpenMovie = { fav, key ->
                            navController.navigate(DetailRoute(fav.id, fav.posterPath, fav.title, key, fav.isTv))
                        },
                        onGoToSearch = {
                            navController.navigate(SearchRoute) {
                                popUpTo(HomeRoute) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
                composable<DetailRoute> { entry ->
                    val route = entry.toRoute<DetailRoute>()
                    DetailScreen(
                        route = route,
                        sharedScope = this@SharedTransitionLayout,
                        animatedScope = this,
                        onBack = { navController.popBackStack() },
                        onOpenMovie = { movie, key ->
                            navController.navigate(DetailRoute(movie.id, movie.posterPath, movie.title, key, movie.isTv))
                        },
                        onOpenFullscreen = { startIndex, startUrl ->
                            navController.navigate(
                                FullscreenRoute(
                                    movieId = route.movieId,
                                    posterPath = route.posterPath,
                                    title = route.title,
                                    sharedKey = route.sharedKey,
                                    isTv = route.isTv,
                                    startIndex = startIndex,
                                    startUrl = startUrl,
                                )
                            )
                        },
                    )
                }
                composable<FullscreenRoute> { entry ->
                    val route = entry.toRoute<FullscreenRoute>()
                    FullscreenPosterScreen(
                        route = route,
                        sharedScope = this@SharedTransitionLayout,
                        animatedScope = this,
                        onBack = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}
