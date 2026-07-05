package com.ttjapan.kaimonomemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.MobileAds
import com.ttjapan.kaimonomemo.ui.app.ShoppingMemoApp
import com.ttjapan.kaimonomemo.ui.theme.KaimonomemoTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycleScope.launch(Dispatchers.IO) {
            MobileAds.initialize(this@MainActivity) {}
        }
        setContent {
            KaimonomemoTheme {
                ShoppingMemoApp()
            }
        }
    }
}
