package com.bidapp.demo

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import io.bidapp.sdk.AdFormat
import io.bidapp.sdk.BIDConfiguration
import io.bidapp.sdk.BannerView
import io.bidapp.sdk.BidappAds
import io.bidapp.sdk.Interstitial
import io.bidapp.sdk.Rewarded
import kotlinx.coroutines.Runnable

class MainActivity : ComponentActivity() {
    private val handler = Handler(Looper.getMainLooper())
    private var runnable : Runnable? = null
    lateinit var interstitial: Interstitial
    lateinit var rewarded: Rewarded
    val loadDelegate = FullscreenLoadDelegate()
    val showDelegate = FullscreenShowDelegate()
    val bannerShowDelegate = BannerViewDelegate()
    var banner: BannerView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = BIDConfiguration()
        config.enableLogging()
        config.enableTestMode()
        config.enableInterstitialAds()
        config.enableRewardedAds()
        config.enableBannerAds_300x250()
        config.enableBannerAds_320x50()

        interstitial = Interstitial(this)
        interstitial.setLoadDelegate(loadDelegate)

        rewarded = Rewarded(this)
        rewarded.setLoadDelegate(loadDelegate)

        banner = addBanner_320x50(bannerShowDelegate)



        val pubid = "15ddd248-7acc-46ce-a6fd-e6f6543d22cd"
        BidappAds.start(pubid, config, this)

        setContent {
              Main()
        }
    }

    @Composable
    fun Main(){
        val context = LocalContext.current
        DisposableEffect(Unit) {
            onDispose {
                runnable?.let { handler.removeCallbacks(it) }
            }
        }

        Column(
            modifier = Modifier
                .height(300.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextButton(text = "Show Interstitial") {
                interstitial.showWithDelegate(this@MainActivity, showDelegate)
            }
            TextButton(text = "Show Rewarded") {
                rewarded.showWithDelegate(this@MainActivity, showDelegate)
            }
            TextButton(text = "Show Banners") {
                val intent = Intent(context, BannersActivity::class.java)
                context.startActivity(intent)
            }

        }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .width(320.dp)
                    .fillMaxWidth()
            ) {
                AndroidView(
                    factory = {
                        val view = FrameLayout(this@MainActivity)
                        view.removeAllViews()
                        view.addView(banner)
                        view
                    },
                    modifier = Modifier.matchParentSize()
                )
            }
        }
    }
    @Composable
    fun TextButton(text: String, onAction: () -> Unit) {
        var color by remember { mutableStateOf(Color.Blue) }
        Text(
            text = text,
            color = color,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(0.dp, 8.dp)
                .clickable {
                    runnable = Runnable {
                        color = Color.Blue
                        onAction()
                    }
                    color = Color.Red
                    handler.postDelayed(runnable!!, 100)
                }



        )

    }
    fun addBanner_320x50(bannerShowDelegate: BannerViewDelegate): BannerView {
        val banner = BannerView(this).banner(AdFormat.banner_320x50)
        banner.setBannerViewDelegate(bannerShowDelegate)
        return banner
    }

}






