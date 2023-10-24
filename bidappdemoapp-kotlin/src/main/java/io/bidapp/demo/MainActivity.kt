package io.bidapp.demo

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.bidapp.ads.R


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val interstitialButton = findViewById<TextView>(R.id.inter)
        val rewardedButton = findViewById<TextView>(R.id.reward)
        val bannersButton = findViewById<TextView>(R.id.banners)
        val bannerMain = findViewById<ConstraintLayout>(R.id.bannerMain)

        val loadDelegate = FullscreenLoadDelegate(this)
        Interstitial.loadDelegate = loadDelegate
        Rewarded.loadDelegate = loadDelegate

        val showDelegate = FullscreenShowDelegate(this)
        val bannerShowDelegate = BannerViewDelegate(this)
        val banner = BannerView(this).banner(bannerShowDelegate, AdFormat.banner_320x50)

        val bidConfig = BIDConfiguration()
        bidConfig.enableTestMode()
        bidConfig.enableLogging()
        bidConfig.enableInterstitialAds()
        bidConfig.enableRewardedAds()
        bidConfig.bannerEnabled_300x250()
        bidConfig.bannerEnabled_320x50()

        val pubid = "15ddd248-7acc-46ce-a6fd-e6f6543d22cd"

        BidappAds.start(this, pubid, bidConfig)

        interstitialButton.setOnClickListener {
            Interstitial.show(showDelegate)
        }

        rewardedButton.setOnClickListener {
            Rewarded.show(showDelegate)
        }

        bannersButton.setOnClickListener {
            val intent = Intent(this, BannerActivity::class.java)
            startActivity(intent)
        }
        bannerMain.addView(banner)
    }
}