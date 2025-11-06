package com.example.core_ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ConfirmationDialog(
    title: String,
    body: String,
    confirmText: String,
    cancelText: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onDismissRequest: () -> Unit = onCancel,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = title)
        },
        text = {
            Text(text = body)
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel
            ) {
                Text(cancelText)
            }
        },
        modifier = modifier
    )
}