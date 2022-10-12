package com.tools.hamzabm.dataexp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tools.hamzabm.dataexp.ui.theme.InternalDataExplorerTheme
import com.tools.hamzabm.internaldataexplorer.InternalDataExplorer
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InternalDataExplorerTheme {
                Scaffold(topBar = {
                    TopAppBar(
                        title = { Text(text = "Internal Explorer Test App") },
                        contentColor = MaterialTheme.colors.onPrimary,
                        backgroundColor = MaterialTheme.colors.primary,
                    )
                }){
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colors.background
                    ) {
                        Row() {
                            Button(onClick = {
                                InternalDataExplorer(this@MainActivity.filesDir.absolutePath,this@MainActivity).launch()

                            }) {
                                Text(text = "try")
                            }
                        }



                    }
                }

            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    InternalDataExplorerTheme {
        Greeting("Android")
    }
}