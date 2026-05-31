package com.ttjapan.kaimonomemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.ttjapan.kaimonomemo.ui.app.ShoppingMemoApp
import com.ttjapan.kaimonomemo.ui.theme.KaimonomemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KaimonomemoTheme {
                ShoppingMemoApp()
            }
        }
    }
}
