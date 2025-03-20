package com.tools.hamzabm.internaldataexplorer

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tools.hamzabm.dataexp.ui.theme.InternalDataExplorerTheme
import com.tools.hamzabm.internaldataexplorer.ui.FileDetailsDialog
import com.tools.hamzabm.internaldataexplorer.ui.RenameDialog
import com.tools.hamzabm.internaldataexplorer.utils.Copy
import com.tools.hamzabm.internaldataexplorer.utils.copyInputStreamToFile
import com.tools.hamzabm.internaldataexplorer.utils.size
import com.tools.hamzabm.internaldataexplorer.utils.toyyyyMMddHHmmFromat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

fun deleteRecursively(file: File): Boolean {
    if (file.isDirectory) {
        file.listFiles()?.forEach { child ->
            if (!deleteRecursively(child)) {
                return false
            }
        }
    }
    return file.delete()
}
class DataExplorerActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var currentImportPath = ""
        var currentExportPath = ""

        setContent {
            InternalDataExplorerTheme {
            var currentPath: String by rememberSaveable { mutableStateOf("") }
            var isSelectionMode by rememberSaveable { mutableStateOf(false) }
            var selectedItems by rememberSaveable { mutableStateOf(setOf<String>()) }
            var filePath: String? = intent.getStringExtra("path")
            filePath?.let {
                currentPath = it
            }
            fun manageBackToParent() {
                var parentFolder = File(File(currentPath).parent)

                currentPath =
                    if (parentFolder.exists() && File(File(currentPath).parent).canRead()) parentFolder.absolutePath else filePath!!

            }

            onBackPressedDispatcher.addCallback {
                if (filePath.equals(currentPath)) {
                    this.isEnabled = false
                    onBackPressedDispatcher.onBackPressed()

                } else {
                    manageBackToParent()
                }

            }

            var exportFile =
                rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { uri: Uri? ->
                    uri?.let { uri ->
                        Copy.with(applicationContext)
                            .sourcePath(currentExportPath)
                            .destinationPath(uri)
                            .copytoExternel(applicationContext) {
                            }
                    }
                }

            val importFile =
                rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                    uri?.let { fileUri ->
                        var fileToImport = File(currentImportPath)

                        this.contentResolver.query(uri, null, null, null, null)?.use {

                            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                            it.moveToFirst()
                            val name = it.getString(nameIndex)
                            if (File(currentImportPath).isDirectory) {
                                fileToImport = File(currentImportPath + File.separator + name)
                                if (fileToImport.exists()) {
                                    do {
                                        fileToImport =
                                            File(currentImportPath + File.separator + "Copy " + fileToImport.name)
                                    } while (fileToImport.exists())
                                }
                            }
                            it.close()
                        }
                        val data = this.contentResolver.openInputStream(uri)
                        data?.let {
                            fileToImport.copyInputStreamToFile(data) { totalBytesCopied, bytesJustCopied, finished ->
                                if (finished) {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        withContext(Dispatchers.Main) {
                                            var tmpPath = currentPath
                                            currentPath = ""
                                            currentPath = tmpPath
                                        }

                                    }
                                }

                            }
                        }
                    }
                }



            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(text = File(currentPath).name) },
                        navigationIcon = {
                            if (!filePath.equals(currentPath)) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "back",
                                    modifier = Modifier.clickable {
                                        manageBackToParent()
                                    }
                                )
                            }
                        },
                        actions = {
                            var createFolderMenuOpen by remember { mutableStateOf(false) }

                            // Selection mode toggle
                            IconButton(onClick = { 
                                isSelectionMode = !isSelectionMode
                                if (!isSelectionMode) {
                                    selectedItems = emptySet()
                                }
                            }) {
                                Icon(
                                    imageVector = if (isSelectionMode) Icons.Default.Close else Icons.Default.SelectAll,
                                    contentDescription = if (isSelectionMode) "Exit selection" else "Enter selection"
                                )
                            }
                            Spacer(modifier = Modifier.width(20.dp))

                            Image(
                            painterResource(id = R.drawable.ic_baseline_file_open_24),
                            contentDescription = "add",

                            modifier = Modifier.clickable {
                                currentImportPath = currentPath
                                importFile.launch("*/*")
                            })
                        Spacer(modifier = Modifier.width(20.dp))
                        Image(
                            painterResource(id = R.drawable.ic_baseline_create_new_folder_24),
                            contentDescription = "add folder",
                            modifier = Modifier.clickable {
                                createFolderMenuOpen = true
                            })
                        Spacer(modifier = Modifier.width(20.dp))
                        DropdownMenu(
                            expanded = createFolderMenuOpen,
                            onDismissRequest = { createFolderMenuOpen = false }) {
                            DropdownMenuItem(onClick = { }) {
                                Column {
                                    var currentValue by remember {
                                        mutableStateOf("")
                                    }
                                    TextField(
                                        value = currentValue,
                                        onValueChange = { currentValue = it },
                                        label = {
                                            Text(
                                                text = stringResource(
                                                    id = R.string.folder_name
                                                )
                                            )
                                        })
                                    Button(onClick = {
                                        var newDir =
                                            File(File(currentPath).absolutePath + File.separator + currentValue)
                                        if (newDir.exists()) {
                                            Toast.makeText(
                                                applicationContext,
                                                getString(R.string.already_exist, currentValue),
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } else {
                                            newDir.mkdirs()
                                            var tmpPath = currentPath
                                            currentPath = ""
                                            currentPath = tmpPath
                                            createFolderMenuOpen = false
                                        }

                                    }) {
                                        Text(text = stringResource(id = R.string.ok))

                                    }
                                }
                            }

                        }

                    }
                )
            }, content = { contentPadding ->
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colors.surface
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(contentPadding)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize()
                            ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(color = MaterialTheme.colors.primaryVariant)
                            ) {
                                Icon(imageVector = Icons.Default.Home, contentDescription = "home", tint = MaterialTheme.colors.onPrimary)
                                Text(text = currentPath.replace(filePath!!, ""),color = MaterialTheme.colors.onPrimary)

                            }
                            getFolderChilds(
                                path = currentPath,
                                onPathChange = { currentPath = it },
                                onExportCurrentPath = {
                                    currentExportPath = it
                                    exportFile.launch(File(it).name)
                                },
                                onImportCurrentPath = {
                                    currentImportPath = it
                                    importFile.launch("*/*")
                                },
                                isSelectionMode = isSelectionMode,
                                selectedItems = selectedItems,
                                onSelectionChange = { path, selected ->
                                    selectedItems = if (selected) {
                                        selectedItems + path
                                    } else {
                                        selectedItems - path
                                    }
                                }
                            )

                            }
                            
                            // Bottom bar for batch operations
                            if (isSelectionMode && selectedItems.isNotEmpty()) {
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    elevation = 8.dp,
                                    color = MaterialTheme.colors.surface
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        Text(
                                            text = stringResource(
                                                id = R.string.selected_count,
                                                selectedItems.size
                                            ),
                                            modifier = Modifier.padding(16.dp)
                                        )
                                        IconButton(onClick = {
                                            var successCount = 0
                                            selectedItems.forEach { path ->
                                                val file = File(path)
                                                if (file.exists() && deleteRecursively(file)) {
                                                    successCount++
                                                }
                                            }
                                            Toast.makeText(
                                                applicationContext,
                                                getString(R.string.deleted_count, successCount, selectedItems.size),
                                                Toast.LENGTH_LONG
                                            ).show()
                                            selectedItems = emptySet()
                                            // Refresh current path
                                            val tmpPath = currentPath
                                            currentPath = ""
                                            currentPath = tmpPath
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete selected"
                                            )
                                        }
                                    }
                                }
                            }}
                        }
            })
            }

        }
    }


}


@Composable
fun getFolderChilds(
    path: String?,
    onPathChange: (String) -> Unit,
    onExportCurrentPath: (String) -> Unit,
    onImportCurrentPath: (String) -> Unit,
    isSelectionMode: Boolean,
    selectedItems: Set<String>,
    onSelectionChange: (String, Boolean) -> Unit
) {
    path?.let {
        LazyColumn(
            modifier = Modifier.padding(bottom = if (isSelectionMode && selectedItems.isNotEmpty()) 80.dp else 0.dp)
        ) {
            var folder = File(it)
            if (folder.listFiles() != null) {
                items(folder.listFiles().asList()) { file ->
                    Childitem(
                        file = file,
                        onPathChange = onPathChange,
                        onExportCurrentPath = onExportCurrentPath,
                        onImportCurrentPath = onImportCurrentPath,
                        isSelectionMode = isSelectionMode,
                        isSelected = selectedItems.contains(file.absolutePath),
                        onSelectionChange = onSelectionChange
                    )
                }
                if (folder.listFiles().isEmpty()) {
                    item {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(15.dp)
                                    .alpha(0.5f), horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = stringResource(id = R.string.empty_folder),
                                    color = Color.Gray
                                )
                            }

                        }
                    }


                }
            }


        }

    }
}


@Composable
fun Childitem(
    file: File,
    onPathChange: (String) -> Unit,
    onExportCurrentPath: (String) -> Unit,
    onImportCurrentPath: (String) -> Unit,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onSelectionChange: (String, Boolean) -> Unit
) {
    Column {

        if (file.isDirectory) {
            FolderItem(
                file = file,
                onPathChange = onPathChange,
                isSelectionMode = isSelectionMode,
                isSelected = isSelected,
                onSelectionChange = { selected -> onSelectionChange(file.absolutePath, selected) }
            )
        } else {
            FileItem(
                file = file,
                onPathChange = onPathChange,
                onExportCurrentPath = onExportCurrentPath,
                onImportCurrentPath = onImportCurrentPath,
                isSelectionMode = isSelectionMode,
                isSelected = isSelected,
                onSelectionChange = { selected -> onSelectionChange(file.absolutePath, selected) }
            )
        }
        Divider()
    }
}

@Composable
fun FileItem(
    file: File,
    onPathChange: (String) -> Unit,
    onExportCurrentPath: (String) -> Unit,
    onImportCurrentPath: (String) -> Unit,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit
) {
    var isDropDownMenuOpen by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var ctx = LocalContext.current
    if (showRenameDialog) {
        RenameDialog(
            currentName = file.name,
            onRename = { newName ->
                val parent = file.parentFile
                val newFile = File(parent, newName)
                if (newFile.exists()) {
                    Toast.makeText(
                        ctx,
                        ctx.getString(R.string.file_already_exists),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    if (file.renameTo(newFile)) {
                        onPathChange("")
                        onPathChange(parent.absolutePath)
                    } else {
                        Toast.makeText(
                            ctx,
                            ctx.getString(R.string.rename_failed),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            },
            onDismiss = { showRenameDialog = false }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
            .background(
                if (isSelected) MaterialTheme.colors.primary.copy(alpha = 0.1f)
                else Color.Transparent
            )
            .clickable(enabled = isSelectionMode) {
                if (isSelectionMode) {
                    onSelectionChange(!isSelected)
                }
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onSelectionChange(it) },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Image(
                painter = painterResource(id = R.drawable.ic_baseline_file_24),
                modifier = Modifier.size(30.dp),
                contentDescription = "folder",
                colorFilter = ColorFilter.tint(MaterialTheme.colors.secondary)
            )
            Column(modifier = Modifier.fillMaxWidth(0.9f)) {
                Text(text = file.name)
                Row {
                    val lastModified = file.lastModified()
                    val date = Date(lastModified)
                    Text(
                        text = date.toyyyyMMddHHmmFromat(),
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                    Text(
                        text = " (" + file.size() + ")",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }
        }
        Row {
            IconButton(onClick = { isDropDownMenuOpen = true }) {
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "menu")
                DropdownMenu(
                    expanded = isDropDownMenuOpen,
                    onDismissRequest = { isDropDownMenuOpen = false }
                ) {
                    DropdownMenuItem(onClick = {
                        showDetailsDialog = true
                        isDropDownMenuOpen = false
                    }) {
                        Text(text = stringResource(id = R.string.file_details))
                    }
                    DropdownMenuItem(onClick = {
                        showRenameDialog = true
                        isDropDownMenuOpen = false
                    }) {
                        Text(text = stringResource(id = R.string.rename))
                    }
                    DropdownMenuItem(onClick = {
                        onImportCurrentPath(file.absolutePath)
                        isDropDownMenuOpen = false
                    }) {
                        Text(text = stringResource(id = R.string.import_file))
                    }
                    DropdownMenuItem(onClick = {
                        onExportCurrentPath(file.absolutePath)
                        isDropDownMenuOpen = false
                    }) {
                        Text(text = stringResource(id = R.string.export))
                    }
                    DropdownMenuItem(onClick = {
                        file.delete()
                        onPathChange("")
                        onPathChange(file.parent)
                        isDropDownMenuOpen = false
                    }) {
                        Text(text = stringResource(id = R.string.delete))
                    }
                }
            }
        }
    }

    if (showDetailsDialog) {
        FileDetailsDialog(
            file = file,
            onDismiss = { showDetailsDialog = false }
        )
    }
}

@Composable
fun FolderItem(
    file: File,
    onPathChange: (String) -> Unit,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
var ctx = LocalContext.current
    if (showRenameDialog) {
        RenameDialog(
            currentName = file.name,
            onRename = { newName ->
                val parent = file.parentFile
                val newFile = File(parent, newName)
                if (newFile.exists()) {
                    Toast.makeText(
                        ctx,
                        ctx.getString(R.string.file_already_exists),
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    if (file.renameTo(newFile)) {
                        onPathChange("")
                        onPathChange(parent.absolutePath)
                    } else {
                        Toast.makeText(
                            ctx,
                            ctx.getString(R.string.rename_failed),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            },
            onDismiss = { showRenameDialog = false }
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
            .background(
                if (isSelected) MaterialTheme.colors.primary.copy(alpha = 0.1f)
                else Color.Transparent
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
            .clickable { 
                if (isSelectionMode) {
                    onSelectionChange(!isSelected)
                } else {
                    onPathChange(file.absolutePath)
                }
            }
        ) {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onSelectionChange(it) },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Image(
                painter = painterResource(id = R.drawable.ic_baseline_folder_24),
                colorFilter = ColorFilter.tint(MaterialTheme.colors.secondary),
                contentDescription = "folder",
                modifier = Modifier.size(50.dp)
            )
            Text(text = file.name)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = stringResource(id = R.string.items, file.listFiles().size.toString()),
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
            IconButton(onClick = { showMenu = true }) {
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "menu")
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(onClick = {
                        showDetailsDialog = true
                        showMenu = false
                    }) {
                        Text(text = stringResource(id = R.string.file_details))
                    }
                    DropdownMenuItem(onClick = {
                        showRenameDialog = true
                        showMenu = false
                    }) {
                        Text(text = stringResource(id = R.string.rename))
                    }
                    DropdownMenuItem(onClick = {
                        if (deleteRecursively(file)) {
                            onPathChange("")
                            onPathChange(file.parent)
                        } else {
                            Toast.makeText(
                                ctx,
                                ctx.getString(R.string.rename_failed),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        showMenu = false
                    }) {
                        Text(text = stringResource(id = R.string.delete))
                    }
    }

    if (showDetailsDialog) {
        FileDetailsDialog(
            file = file,
            onDismiss = { showDetailsDialog = false }
        )
    }
}
        }
    }

}





@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MaterialTheme {
        Text(text = "Hello !")
    }
}
