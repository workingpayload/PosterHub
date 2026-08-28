# UI/UX Enhancements

Ideas for improving PostersHub, sorted by importance (highest-impact / most urgent first within
each tier).

## Critical

1. ~~**TMDB / fanart.tv attribution**~~ — ✅ Done. Added an About screen (info icon on the Home
   tab) with required TMDB + fanart.tv attribution text and links. (Logo asset still TODO — text
   attribution satisfies the ToS requirement but the TMDB logo isn't bundled yet.)
2. ~~**Granular error handling on Home**~~ — ✅ Done. Each Home row now loads and fails
   independently (`HomeSection` + `SectionState`), with its own shimmer/error/retry, instead of
   one failing endpoint blanking the whole feed.
3. ~~**Distinguish "no internet" from "API error"**~~ — ✅ Done (Home + Detail). Added
   `util/ErrorClassifier.kt`, which turns exceptions into network/auth/server-specific messages.
   Auto-retry-on-reconnect is not implemented — retry is still manual (tap Retry).

## High

4. **Pull-to-refresh** — Home and Search have no swipe-to-refresh; the only recovery path is a
   full-screen error's Retry button. Standard Compose `PullToRefreshBox` would fix this cheaply.
5. **Favorites are unusable offline in practice** — `FavoriteMovie.posterUrl` is stored, but
   there's no guarantee Coil's disk cache still has that exact URL cached; a saved favorite can
   fail to render with no network. Consider caching the poster bytes locally for favorites
   specifically, not just relying on the image loader's LRU cache.
6. **No swipe-to-delete / multi-select in Favorites** — the only way to remove a favorite today is
   opening its detail screen and re-tapping the heart. Swipe-to-dismiss on the grid item is the
   expected pattern here.
7. **Favorites empty state has no call to action** — it just shows a text hint. Add a button that
   jumps to the Search tab.
8. **Search needs a clear (×) button** — `OutlinedTextField` has no trailing clear icon; clearing
   a query requires manually deleting every character.
9. **Search needs a movie/TV filter** — `search/multi` mixes movies and TV with no way to narrow
   results, and no media-type badge on result cards to tell them apart at a glance.
10. **Pagination footer state is invisible** — `SearchScreen` handles `loadState.refresh` but never
    surfaces `loadState.append` (loading-more spinner or an error row at the bottom when the next
    page fails).
11. **Detail screen underuses available TMDB data** — only title/year/rating/overview are shown;
    TMDB also exposes genres, runtime, cast, and similar-titles, all of which would make the detail
    screen feel complete instead of poster-focused-only.
12. **No download/save loading state** — tapping Save or Wallpaper gives no immediate feedback
    while `ImageActions` decodes a full-res (potentially 4K) bitmap; on a slow connection the button
    just looks unresponsive until the Toast appears. Show a spinner/disabled state during the
    operation.
13. **No indicator of *how many* posters are in the fullscreen viewer** — `HorizontalPager` has no
    page-dot indicator, so users can't tell if there are 2 variants or 20 without swiping blind.

## Medium

14. **Warn before downloading original-resolution posters on metered/mobile data** — "4K" posters
    can be several MB; a lightweight size estimate or a Wi-Fi-only download preference would help.
15. **Source/language badge on variant thumbnails** — `PosterImage` already carries `source`
    (TMDB/Fanart) and `language`/`isTextless`, which drives the ranking, but none of it is shown on
    the variants strip — users can't tell why one poster is ranked above another.
16. **Snackbar instead of full-screen replace for transient errors** — Home's error state replaces
    the entire screen even for a background refresh failure; a Snackbar-with-Retry preserves
    whatever content already loaded.
17. **"Recent searches" on the Search tab** — currently the search field is always blank on
    revisit; even a small in-memory recent-queries list would reduce retyping.
18. **Reselecting a bottom-nav tab should scroll its content to top** — `NavigationBarItem` reuses
    `launchSingleTop`/`restoreState` but doesn't reset scroll position when a tab is tapped while
    already active, which is standard tab-bar behavior.
19. **Adaptive layout for tablets/landscape** — grid column counts (`GridCells.Fixed(3)`) and
    carousel height (`430.dp`) are hardcoded; on larger screens this wastes space and posters look
    small. Use `WindowSizeClass` to scale columns.
20. **Predictive back gesture support** — Android 13+ predictive back isn't wired up for the
    detail/fullscreen screens, so back navigation doesn't get the system's animated preview.
21. **Long-press quick actions on poster cards** — long-pressing a `PosterCard` in Home/Search
    could pop a small menu (Favorite / Share) without a full navigation round-trip to Detail.
22. **Share action** — there's no share sheet anywhere (share a poster image or a TMDB link);
    common ask for a poster-browsing app.
23. **Splash screen** — no use of `androidx.core.splashscreen`; cold start currently shows a blank
    frame before Compose draws.

## Low / polish

24. **Settings screen** — beyond attribution (see #1), a natural home for cache-clear, app version,
    and possibly a poster resolution preference (#14).
25. **"4K"/resolution badge on poster thumbnails** — a small badge on cards whose top-ranked poster
    is very high-res would make the app's core value prop (highest-res posters) visible before
    tapping in.
26. **Sort options in Favorites** — currently fixed to most-recently-added; alphabetical / by
    rating would be easy additions since the data's already local.
27. **Optional light theme or Material You dynamic color** — the cinematic dark theme is a
    deliberate, reasonable choice, but a light/system-following alternative behind a toggle would
    widen accessibility for users sensitive to dark UIs.
28. **Haptic feedback** — favoriting, successful save, and double-tap-zoom in the fullscreen viewer
    have no haptic tick; small but noticeable polish on modern devices.
29. **Accessibility contrast pass** — `Mist` (`#C9C9D6`) muted text sits on top of animated,
    poster-derived gradients in `DynamicBackground`; worth spot-checking contrast ratios since the
    background color isn't fixed.
30. **Large-font-scale layout check** — several dimensions (carousel `height(430.dp)`, fixed grid
    aspect ratios) haven't been verified against large system font-scale settings.
