package com.crazy_coder.everfit_wear.presentation.result

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ResultActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainContent()
        }
    }
}

@Composable
fun MainContent() {
    Scaffold(
        content = { MyContent() }
    )
}

@Composable
fun MyContent() {

    // Fetching the Local Context
    val mContext = LocalContext.current

    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // Creating a Button that on-click
        // implements an Intent to go to SecondActivity
        Text("REsult Activity", color = Color.White)
    }
}
