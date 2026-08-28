package com.example.postershub.ui.nav

import kotlinx.serialization.Serializable

/**
 * Shared-element key for a poster the fullscreen viewer shows at [index].
 * Index 0 (the primary) reuses the base key so it morphs to/from the detail hero;
 * every other variant gets its own key that matches its thumbnail in the variants strip.
 */
fun heroOrVariantKey(base: String, index: Int): String =
    if (index == 0) base else "$base-v$index"

/** Shared-element key for a variant thumbnail in the detail strip (distinct from the hero). */
fun variantThumbKey(base: String, index: Int): String = "$base-v$index"

// Top-level tabs
@Serializable
object HomeRoute

@Serializable
object SearchRoute

@Serializable
object FavoritesRoute

// Detail: carries just enough to render the shared-element hero instantly, before details load.
@Serializable
data class DetailRoute(
    val movieId: Int,
    val posterPath: String?,
    val title: String,
    val sharedKey: String,
    val isTv: Boolean = false,
)

// Fullscreen zoomable viewer.
@Serializable
data class FullscreenRoute(
    val movieId: Int,
    val posterPath: String?,
    val title: String,
    val sharedKey: String,
    val isTv: Boolean = false,
    val startIndex: Int = 0,
    val startUrl: String? = null, // exact image tapped, used as the loading placeholder
)
