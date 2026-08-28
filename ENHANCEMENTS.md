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

4. ~~**Pull-to-refresh**~~ — ✅ Done. `PullToRefreshBox` on Home (drives `HomeViewModel.loadAll()`)
   and Search (calls `results.refresh()`).
5. ~~**Favorites are unusable offline in practice**~~ — ✅ Done. `FavoritesStore` now downloads and
   stores a local JPEG per favorite (`filesDir/favorite_posters/<id>.jpg`); the grid renders from
   that file instead of a remote URL, and the file is deleted when unfavorited.
6. ~~**No swipe-to-delete in Favorites**~~ — ✅ Done via `SwipeToDismissBox` per grid item.
   (Multi-select wasn't added — swipe-per-item covers the common case.)
7. ~~**Favorites empty state has no call to action**~~ — ✅ Done. "Browse & Search" button jumps to
   the Search tab.
8. ~~**Search needs a clear (×) button**~~ — ✅ Done.
9. ~~**Search needs a movie/TV filter**~~ — ✅ Done. All/Movies/TV `FilterChip`s filter the paged
   results client-side (`PagingData.filter`); a type badge shows on cards when filter = All.
10. ~~**Pagination footer state is invisible**~~ — ✅ Done. `results.loadState.append` now renders a
    spinner or an error + Retry row at the bottom of the grid.
11. ~~**Detail screen underuses available TMDB data**~~ — ✅ Done. Added genres, runtime
    (`{h}h {m}m` / `{m}m/ep`), a cast row (`append_to_response=credits,similar`), and a "More Like
    This" row that navigates to another Detail screen.
12. ~~**No download/save loading state**~~ — ✅ Done on Detail and Fullscreen: the action button
    shows a small spinner and disables itself while `ImageActions` runs.
13. ~~**No indicator of how many posters are in the fullscreen viewer**~~ — ✅ Done. Dot indicator up
    to 12 variants, "n / total" pill beyond that.

## Medium

14. ~~**Warn before downloading original-resolution posters on metered/mobile data**~~ — ✅ Done.
    `Context.isMeteredConnection()` + a reusable `MeteredConfirmDialog`, wired into Detail's
    Save/Wallpaper and Fullscreen's Save/Wallpaper. (No exact size estimate or Wi-Fi-only
    preference — just a confirm prompt.)
15. ~~**Source/language badge on variant thumbnails**~~ — ✅ Done on the Detail variants strip
    ("TMDB · EN", "Fanart · No text", etc.).
16. ~~**Snackbar instead of full-screen replace for transient errors**~~ — ✅ Done. Home was already
    fixed by the per-row rework (Critical #2). Detail's load error was previously never shown at
    all (`DetailUiState.error` was dead state) — now surfaced via a Snackbar with a Retry action.
    Search's refresh-error hint also gained a Retry button.
17. ~~**"Recent searches" on the Search tab**~~ — ✅ Done. In-memory, last 8 queries, shown as
    suggestions below 2 characters, with a Clear action.
18. ~~**Reselecting a bottom-nav tab should scroll its content to top**~~ — ✅ Done via a
    per-tab signal counter in `PosterNav` + `animateScrollToItem(0)` in each screen.
19. ~~**Adaptive layout for tablets/landscape**~~ — ✅ Done for Search and Favorites grids
    (`GridCells.Adaptive(minSize = 110.dp)` instead of `Fixed(3)`). Home's carousel height and
    Detail's hero margins are still fixed — left as-is since `LazyRow`/scrolling content doesn't
    waste space the way a capped grid does.
20. ~~**Predictive back gesture support**~~ — ✅ Done. `android:enableOnBackInvokedCallback="true"`
    in the manifest; Navigation Compose 2.8's `NavHost` handles the rest automatically.
21. ~~**Long-press quick actions on poster cards**~~ — ✅ Done. Long-press on any `PosterCard`
    (Home, Search, Detail's "More Like This") opens a Favorite/Share menu.
22. ~~**Share action**~~ — ✅ Done: the PosterCard quick menu (#21) and a dedicated Share button on
    Detail both share a TMDB link via `ACTION_SEND`. Sharing the actual poster image (not just a
    link) would need a `FileProvider` — left as a follow-up.
23. ~~**Splash screen**~~ — ✅ Done via `androidx.core.splashscreen`.

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
