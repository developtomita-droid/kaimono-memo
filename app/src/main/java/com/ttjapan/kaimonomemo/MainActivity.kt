package com.ttjapan.kaimonomemo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.MobileAds
import com.ttjapan.kaimonomemo.ads.GoogleMobileAdsConsentManager
import com.ttjapan.kaimonomemo.ui.app.ShoppingMemoApp
import com.ttjapan.kaimonomemo.ui.theme.KaimonomemoTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : ComponentActivity() {
    private lateinit var consentManager: GoogleMobileAdsConsentManager
    private val canRequestAds = mutableStateOf(false)
    private val privacyOptionsRequired = mutableStateOf(false)
    private val mobileAdsInitializationStarted = AtomicBoolean(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        consentManager = GoogleMobileAdsConsentManager(applicationContext)
        setContent {
            KaimonomemoTheme {
                ShoppingMemoApp(
                    canRequestAds = canRequestAds.value,
                    privacyOptionsRequired = privacyOptionsRequired.value,
                    onShowPrivacyOptions = ::showPrivacyOptions
                )
            }
        }

        consentManager.gatherConsent(this) { error ->
            if (error != null) {
                Log.w(ConsentLogTag, "Consent gathering failed: ${error.errorCode} ${error.message}")
            }
            refreshConsentState()
        }
        refreshConsentState()
    }

    private fun refreshConsentState() {
        runOnUiThread {
            canRequestAds.value = consentManager.canRequestAds
            privacyOptionsRequired.value = consentManager.isPrivacyOptionsRequired
            if (canRequestAds.value) initializeMobileAds()
        }
    }

    private fun initializeMobileAds() {
        if (!mobileAdsInitializationStarted.compareAndSet(false, true)) return
        lifecycleScope.launch(Dispatchers.IO) {
            MobileAds.initialize(this@MainActivity) {}
        }
    }

    private fun showPrivacyOptions() {
        consentManager.showPrivacyOptionsForm(this) { error ->
            if (error != null) {
                Log.w(ConsentLogTag, "Privacy options form failed: ${error.errorCode} ${error.message}")
            }
            refreshConsentState()
        }
    }

    private companion object {
        const val ConsentLogTag = "AdMobConsent"
    }
}
