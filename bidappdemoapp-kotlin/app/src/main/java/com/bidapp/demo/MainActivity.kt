package com.bidapp.demo

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import io.bidapp.sdk.AdFormat
import io.bidapp.sdk.BIDConfiguration
import io.bidapp.sdk.BannerView
import io.bidapp.sdk.BidappAds
import io.bidapp.sdk.Interstitial
import io.bidapp.sdk.Rewarded

class MainActivity : AppCompatActivity() {
    var banner : BannerView? = null
    val showDelegate = FullscreenShowDelegate()
    val loadDelegate = FullscreenLoadDelegate()
    val bannerShowDelegate = BannerViewDelegate()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val interstitialButton = findViewById<TextView>(R.id.inter)
        val rewardedButton = findViewById<TextView>(R.id.reward)
        val bannersButton = findViewById<TextView>(R.id.banners)
        val bannerMain = findViewById<ConstraintLayout>(R.id.bannerMain)

        val interstitial = Interstitial(this)
        val rewarded = Rewarded(this)
        interstitial.setLoadDelegate(loadDelegate)
        rewarded.setLoadDelegate(loadDelegate)

        banner = BannerView(this).banner(AdFormat.banner_320x50)
        banner?.setBannerViewDelegate(bannerShowDelegate)

        val bidConfig = BIDConfiguration()

        bidConfig.enableTestMode()
        bidConfig.enableLogging()
        bidConfig.enableInterstitialAds()
        bidConfig.enableRewardedAds()
        bidConfig.enableBannerAds_320x50()
        bidConfig.enableBannerAds_300x250()

        val pubid = "15ddd248-7acc-46ce-a6fd-e6f6543d22cd"

        BidappAds.start(pubid, bidConfig, this)

        interstitialButton.setOnClickListener {
            interstitial.showWithDelegate(this, showDelegate)
        }

        rewardedButton.setOnClickListener {
            rewarded.showWithDelegate(this, showDelegate)
        }

        bannersButton.setOnClickListener {
            val intent = Intent(this, BannerActivity::class.java)
            startActivity(intent)
        }
        bannerMain.addView(banner)
    }

    override fun onDestroy() {
        super.onDestroy()
        banner?.destroy()
    }
}