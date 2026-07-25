package com.rodzina.wyjazdy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rodzina.wyjazdy.ui.WyjazdyApp
import com.rodzina.wyjazdy.ui.theme.WyjazdyTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as WyjazdyApplication).container
        setContent {
            WyjazdyTheme {
                WyjazdyApp(container)
            }
        }
    }
}
