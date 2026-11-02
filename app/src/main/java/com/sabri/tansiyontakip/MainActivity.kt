package com.sabri.tansiyontakip

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import com.sabri.tansiyontakip.data.BloodPressureRepository
import com.sabri.tansiyontakip.ui.HomeScreen
import com.sabri.tansiyontakip.ui.theme.TansiyonTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Lacivert başlık durum çubuğunun altına uzandığı için açık renk ikonlar kullanılır.
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = true
        }

        setContent {
            TansiyonTheme {
                val repository = remember { BloodPressureRepository(applicationContext) }
                HomeScreen(repository = repository)
            }
        }
    }
}
