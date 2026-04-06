package com.hexgram.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.hexgram.android.ui.screens.MainScreen
import com.hexgram.android.ui.theme.HexgramTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HexgramTheme {
                MainScreen()
            }
        }
    }
}
