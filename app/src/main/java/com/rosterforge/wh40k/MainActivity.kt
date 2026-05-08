package com.rosterforge.wh40k

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.rosterforge.wh40k.presentation.navigation.RosterForgeNavGraph
import com.rosterforge.wh40k.presentation.theme.RosterForgeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { RosterForgeApp() }
    }
}

@Composable
private fun RosterForgeApp() {
    RosterForgeTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val navController = rememberNavController()
            RosterForgeNavGraph(navController = navController)
        }
    }
}
