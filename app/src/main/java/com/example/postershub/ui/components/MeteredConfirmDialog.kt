package com.example.postershub.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/** Shown before a poster download/wallpaper-apply when the active connection is metered. */
@Composable
fun MeteredConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Use mobile data?") },
        text = { Text("You're on a metered connection. This poster may be a few MB. Continue?") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Continue") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
