package com.example.postershub.ui.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.postershub.ui.theme.Electric
import com.example.postershub.ui.theme.Ink
import com.example.postershub.ui.theme.Mist

/** Required TMDB/fanart.tv attribution + basic app info. See TMDB API terms of use. */
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    fun openUrl(url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Ink)
            .statusBarsPadding()
            .padding(horizontal = 24.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("About", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))

        Text("PostersHub", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
        Text(
            "Browse and save 4K movie & TV posters.",
            color = Mist,
            modifier = Modifier.padding(top = 4.dp, bottom = 28.dp),
        )

        Text("Data & Artwork", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(10.dp))
        Text(
            "This product uses the TMDB API but is not endorsed or certified by TMDB.",
            color = Mist,
        )
        Text(
            "themoviedb.org",
            color = Electric,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .padding(top = 6.dp, bottom = 20.dp)
                .clickable { openUrl("https://www.themoviedb.org/") },
        )
        Text(
            "Poster artwork is also provided by fanart.tv.",
            color = Mist,
        )
        Text(
            "fanart.tv",
            color = Electric,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .padding(top = 6.dp)
                .clickable { openUrl("https://fanart.tv/") },
        )
    }
}
