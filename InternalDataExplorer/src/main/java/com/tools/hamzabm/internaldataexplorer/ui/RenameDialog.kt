package com.tools.hamzabm.internaldataexplorer.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.tools.hamzabm.internaldataexplorer.R

@Composable
fun RenameDialog(
    currentName: String,
    onRename: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newName by remember { mutableStateOf(currentName) }
    var error by remember { mutableStateOf<String?>(null) }
    var ctx = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.rename_title)) },
        text = {
            Column {
                TextField(
                    value = newName,
                    onValueChange = { 
                        newName = it
                        error = null
                    },
                    label = { Text(stringResource(R.string.new_name)) },
                    isError = error != null,
                    singleLine = true
                )
                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colors.error,
                        style = MaterialTheme.typography.caption
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                when {
                    newName.isBlank() -> {
                        error =  ctx.getString(R.string.name_cannot_be_empty)
                    }
                    newName.contains("/") || newName.contains("\\") -> {
                        error = ctx.getString(R.string.name_contains_invalid_chars)
                    }
                    else -> {
                        onRename(newName)
                        onDismiss()
                    }
                }
            }) {
                Text(stringResource(R.string.rename))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
