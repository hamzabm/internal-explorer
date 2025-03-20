package com.tools.hamzabm.internaldataexplorer.ui

import android.webkit.MimeTypeMap
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tools.hamzabm.internaldataexplorer.R
import com.tools.hamzabm.internaldataexplorer.utils.size
import com.tools.hamzabm.internaldataexplorer.utils.toyyyyMMddHHmmFromat
import java.io.File
import java.util.*

@Composable
fun FileDetailsDialog(
    file: File,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.file_details)) },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp)
            ) {
                DetailRow(
                    label = stringResource(R.string.folder_name),
                    value = file.name
                )
                DetailRow(
                    label = stringResource(R.string.path),
                    value = file.absolutePath
                )
                DetailRow(
                    label = stringResource(R.string.filesize),
                    value = file.size()
                )
                DetailRow(
                    label = stringResource(R.string.last_modified),
                    value = Date(file.lastModified()).toyyyyMMddHHmmFromat()
                )
                DetailRow(
                    label = stringResource(R.string.mime_type),
                    value = getMimeType(file)
                )
                DetailRow(
                    label = stringResource(R.string.permissions),
                    value = getPermissionsString(file)
                )
                DetailRow(
                    label = stringResource(R.string.hidden),
                    value = if (file.isHidden) stringResource(R.string.yes) else stringResource(R.string.no)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun getMimeType(file: File): String {
    val extension = file.extension
    return if (extension.isNotEmpty()) {
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/octet-stream"
    } else {
        if (file.isDirectory) "inode/directory" else "application/octet-stream"
    }
}

private fun getPermissionsString(file: File): String {
    val readable = if (file.canRead()) "r" else "-"
    val writable = if (file.canWrite()) "w" else "-"
    val executable = if (file.canExecute()) "x" else "-"
    return "$readable$writable$executable"
}
