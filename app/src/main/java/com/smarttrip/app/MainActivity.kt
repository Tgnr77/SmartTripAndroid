package com.smarttrip.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.smarttrip.app.ui.navigation.NavGraph
import com.smarttrip.app.ui.theme.SmartTripTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val _newIntent = MutableSharedFlow<Intent>(extraBufferCapacity = 1)
    val newIntentFlow: SharedFlow<Intent> = _newIntent.asSharedFlow()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartTripTheme {
                NavGraph(newIntentFlow = newIntentFlow)
            }
        }
    }

    // Transmet le deep link au NavController quand l'app est déjà au premier plan
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        _newIntent.tryEmit(intent)
    }
}
