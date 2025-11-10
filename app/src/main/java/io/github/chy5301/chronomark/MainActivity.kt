package io.github.chy5301.chronomark

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.chy5301.chronomark.ui.screen.StopwatchScreen
import io.github.chy5301.chronomark.ui.theme.ChronoMarkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChronoMarkTheme {
                StopwatchScreen()
            }
        }
    }
}