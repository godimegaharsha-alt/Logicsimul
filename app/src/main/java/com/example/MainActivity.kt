package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.logiclab.data.OfflineAssetPackManager
import com.example.logiclab.ui.MainScreen
import com.example.ui.theme.LogicLabTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycleScope.launch {
            OfflineAssetPackManager.initialize(applicationContext)
        }
        setContent {
            LogicLabTheme {
                MainScreen()
            }
        }
    }
}
