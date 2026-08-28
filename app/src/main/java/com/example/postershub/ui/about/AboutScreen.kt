package com.example.postershub.ui.about

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.SingletonImageLoader
import com.example.postershub.di.ServiceLocator
import com.example.postershub.ui.theme.Electric
import com.example.postershub.ui.theme.Ink
import com.example.postershub.ui.theme.Mist
import kotlinx.coroutines.launch

/** Settings + required TMDB/fanart.tv attribution. See TMDB API terms of use. */
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings = ServiceLocator.settingsStore
    val useSystemTheme by settings.useSystemTheme.collectAsStateWithLifecycle(false)
    val warnOnMetered by settings.warnOnMetered.collectAsStateWithLifecycle(true)

    fun openUrl(url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    val versionName = remember(context) {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }
            .getOrNull() ?: "unknown"
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
            Text("Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))

        Text("PostersHub", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
        Text(
            "Browse and save 4K movie & TV posters. Version $versionName",
            color = Mist,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
        )

        Text("Appearance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        SettingRow(
            title = "Match system theme",
            subtitle = "Use light/dark + your device's Material You colors instead of the cinematic dark theme",
            checked = useSystemTheme,
            onCheckedChange = { value -> scope.launch { settings.setUseSystemTheme(value) } },
        )

        Spacer(Modifier.height(20.dp))

        Text("Downloads", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        SettingRow(
            title = "Warn on mobile data",
            subtitle = "Confirm before downloading or setting a wallpaper on a metered connection",
            checked = warnOnMetered,
            onCheckedChange = { value -> scope.launch { settings.setWarnOnMetered(value) } },
        )
        TextButton(
            onClick = {
                val loader = SingletonImageLoader.get(context)
                loader.memoryCache?.clear()
                loader.diskCache?.clear()
                Toast.makeText(context, "Image cache cleared", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.padding(top = 4.dp),
        ) { Text("Clear image cache") }

        Spacer(Modifier.height(20.dp))

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

@Composable
private fun SettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
    ) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Mist, style = MaterialTheme.typography.bodySmall)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
