package com.example.postershub.util

import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ImageActions {

    /** Decode the full-resolution image to a software Bitmap (needed for save/wallpaper/palette). */
    suspend fun loadBitmap(context: Context, url: String): Bitmap? = withContext(Dispatchers.IO) {
        val request = ImageRequest.Builder(context)
            .data(url)
            .allowHardware(false)
            .build()
        val result = SingletonImageLoader.get(context).execute(request)
        (result as? SuccessResult)?.image?.toBitmap()
    }

    /** Saves the poster to the gallery (Pictures/PostersHub). Returns true on success. */
    suspend fun savePoster(context: Context, url: String, displayName: String): Boolean =
        withContext(Dispatchers.IO) {
            val bmp = loadBitmap(context, url) ?: return@withContext false
            val safeName = displayName.replace(Regex("[^A-Za-z0-9-_ ]"), "_").ifBlank { "poster" }
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$safeName.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/PostersHub"
                )
                values.put(MediaStore.Images.Media.IS_PENDING, 1)
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    ?: return@withContext false
                resolver.openOutputStream(uri)?.use { out ->
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, out)
                } ?: return@withContext false
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
                true
            } else {
                // API 24-28: write into public Pictures dir (needs WRITE_EXTERNAL_STORAGE) + register.
                val dir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "PostersHub"
                )
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, "$safeName.jpg")
                FileOutputStream(file).use { out ->
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, out)
                }
                @Suppress("DEPRECATION")
                values.put(MediaStore.Images.Media.DATA, file.absolutePath)
                resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                true
            }
        }

    /** which: WallpaperManager.FLAG_SYSTEM, FLAG_LOCK, or their bitwise-or. */
    suspend fun applyWallpaper(context: Context, url: String, which: Int): Boolean =
        withContext(Dispatchers.IO) {
            val bmp = loadBitmap(context, url) ?: return@withContext false
            runCatching {
                WallpaperManager.getInstance(context).setBitmap(bmp, null, true, which)
            }.isSuccess
        }
}
