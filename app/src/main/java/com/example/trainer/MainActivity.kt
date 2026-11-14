package com.example.trainer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.example.trainer.presentation.navigation.AppNavHost
import com.example.trainer.presentation.theme.*
import dagger.hilt.android.AndroidEntryPoint

/** Single Compose activity hosting authentication and trainer flows. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val theme: ThemeViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT))
        setContent {
            val dark by theme.dark.collectAsStateWithLifecycle()
            TrainerTheme(dark) { AppNavHost(dark, theme::setDark) }
        }
    }
}

