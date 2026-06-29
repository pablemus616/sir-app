package com.xnihilfx.sirmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.xnihilfx.sirmobile.MainViewModel.StartState
import com.xnihilfx.sirmobile.ui.navigation.SirNavGraph
import com.xnihilfx.sirmobile.ui.theme.SirTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition {
            vm.state.value is StartState.Loading
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val startState by vm.state.collectAsStateWithLifecycle()
            if (startState is StartState.Ready) {
                val route = (startState as StartState.Ready).route
                SirTheme {
                    SirNavGraph(startRoute = route)
                }
            }
        }
    }
}
