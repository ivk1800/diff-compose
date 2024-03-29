package ru.ivk1800.diff.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.ivk1800.diff.compose.LocalDiffTheme
import ru.ivk1800.diff.presentation.DialogRouter

@Composable
fun DiffAlertDialog(dialog: DialogRouter.Dialog, onConfirmClick: () -> Unit) =
    Box(
        modifier = Modifier.fillMaxSize().background(LocalDiffTheme.current.colors.dialogBarrierColor)
    ) {
        when (dialog) {
            is DialogRouter.Dialog.Error -> ErrorDialog(dialog, onConfirmClick)
            is DialogRouter.Dialog.Confirmation -> ConfirmationDialog(dialog, onConfirmClick)
        }
    }

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ErrorDialog(dialog: DialogRouter.Dialog.Error, onConfirmClick: () -> Unit) {
    AlertDialog(
        modifier = Modifier.widthIn(min = 400.dp),
        onDismissRequest = { },
        title = { Text(text = dialog.title) },
        text = { Text(dialog.text) },
        confirmButton = {
            Button(
                onClick = onConfirmClick,
            ) {
                Text("OK")
            }
        }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ConfirmationDialog(dialog: DialogRouter.Dialog.Confirmation, onConfirmClick: () -> Unit) {
    AlertDialog(
        modifier = Modifier.widthIn(min = 400.dp),
        onDismissRequest = { },
        title = { Text(text = dialog.title) },
        text = { Text(dialog.text) },
        dismissButton = {
            Button(
                onClick = onConfirmClick,
            ) {
                Text(dialog.negativeText)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmClick.invoke()
                    dialog.positiveCallback.invoke()
                },
            ) {
                Text(dialog.positiveText)
            }
        }
    )
}
