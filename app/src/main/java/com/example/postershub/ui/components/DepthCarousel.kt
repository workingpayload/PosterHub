package com.example.postershub.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import coil3.compose.AsyncImage
import com.example.postershub.data.ImageUrl
import com.example.postershub.domain.model.Movie
import kotlin.math.absoluteValue

/** Featured carousel with a depth effect: neighbouring pages scale down and fade. */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.DepthCarousel(
    movies: List<Movie>,
    animatedScope: AnimatedVisibilityScope,
    onClick: (Movie, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (movies.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { movies.size })

    HorizontalPager(
        state = pagerState,
        contentPadding = PaddingValues(horizontal = 56.dp),
        pageSpacing = 14.dp,
        modifier = modifier,
    ) { page ->
        val movie = movies[page]
        val key = "carousel-${movie.id}"
        val offset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
            .absoluteValue
            .coerceIn(0f, 1f)
        val scale = lerp(0.84f, 1f, 1f - offset)
        val fade = lerp(0.5f, 1f, 1f - offset)

        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    alpha = fade
                }
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(20.dp))
                .sharedElement(
                    rememberSharedContentState(key = key),
                    animatedVisibilityScope = animatedScope,
                )
                .clickable { onClick(movie, key) }
        ) {
            ShimmerBox(Modifier.fillMaxSize(), RoundedCornerShape(20.dp))
            AsyncImage(
                model = ImageUrl.tmdb(movie.posterPath, "w780"),
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.55f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.75f),
                        )
                    )
            )
            Text(
                text = movie.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
            )
        }
    }
}
