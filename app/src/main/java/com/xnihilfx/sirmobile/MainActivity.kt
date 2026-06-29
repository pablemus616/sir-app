package com.xnihilfx.sirmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.xnihilfx.sirmobile.ui.theme.SirTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SirTheme {
                Placeholder()
            }
        }
    }
}

@Composable
private fun Placeholder() {
    Text(
        text = "SIR Reclutadores",
        modifier = Modifier.fillMaxSize().wrapContentSize(),
    )
}
