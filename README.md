# PostersHub

Android app for browsing 4K movie & TV posters, built with Jetpack Compose. Merges poster
candidates from [TMDB](https://www.themoviedb.org/) and [fanart.tv](https://fanart.tv/), ranks
them by resolution, and lets you save them to your gallery or set them as wallpaper.

## Features

- Home feed: trending, popular, top-rated, and now-playing movies + TV series
- Search across movies and TV (paginated)
- Detail screen with poster variants ranked by resolution, textless-preferred
- Full-screen pinch-zoom poster viewer
- Save poster to gallery, set as wallpaper (home/lock/both)
- Favorites, persisted locally
- Shared-element transitions between grid → detail → fullscreen

## Tech stack

- Kotlin, Jetpack Compose (Material 3)
- Retrofit + OkHttp + kotlinx.serialization
- Coil 3 for image loading
- Paging 3 for search results
- Jetpack DataStore (Preferences) for favorites
- Manual DI via `ServiceLocator` (no Hilt/Koin)

## Setup

1. Get API keys:
   - **TMDB**: [themoviedb.org](https://www.themoviedb.org/settings/api) — the v4 "API Read
     Access Token" (starts with `eyJ...`) is preferred; a v3 API key also works.
   - **fanart.tv**: [fanart.tv/get-an-api-key](https://fanart.tv/get-an-api-key/)
2. Add them to `local.properties` (gitignored) at the project root:
   ```properties
   TMDB_API_KEY=your_tmdb_key_here
   FANART_API_KEY=your_fanart_key_here
   ```
3. Open in Android Studio and run, or build from the CLI:
   ```
   ./gradlew assembleDebug
   ```

Missing keys don't break the build — network calls just return 401 until real keys are supplied.

## Project layout

See [`.codemap.md`](.codemap.md) for a full module-by-module breakdown of the codebase.

```
app/src/main/java/com/example/postershub/
  di/             manual DI container (OkHttp/Retrofit/repositories)
  domain/model/   Movie, PosterImage, etc.
  data/           remote APIs (TMDB, fanart.tv), DTOs, repositories, local favorites store
  ui/             screens (home, search, favorites, detail, fullscreen), shared components, theme
  util/           image save/wallpaper actions, ViewModel factory helper
```

## Requirements

- Android Studio (latest stable)
- minSdk 24, targetSdk/compileSdk 37
