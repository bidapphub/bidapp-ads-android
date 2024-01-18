package com.bidapp.demo

import android.util.Log
import androidx.constraintlayout.widget.ConstraintLayout
import io.bidapp.sdk.AdInfo
import io.bidapp.sdk.BIDBannerViewDelegate
import io.bidapp.sdk.BannerView
import java.lang.ref.WeakReference


class BannerViewDelegate(view: ConstraintLayout) : BIDBannerViewDelegate {
private val container = WeakReference(view)


    override fun adViewDidDisplayAd(adView: BannerView, adInfo: AdInfo?) {
        print("App - didDisplayAd. AdView: $adView, AdInfo: $adInfo")
    }

    override fun adViewDidFailToDisplayAd(adView: BannerView, adInfo: AdInfo?, errors: Error) {
        print("App - didFailToDisplayAd. AdView: $adView, Error:${errors.localizedMessage}")
    }

    override fun adViewDidLoadAd(adView: BannerView, adInfo: AdInfo?) {
        container.get()?.addView(adView)
        print("App - adViewDidLoadAd. AdView: $adView, AdInfo: $adInfo")

    }

    override fun allNetworksFailedToDisplayAd(adView: BannerView) {
        print("App - didFailToDisplayAd. AdView: $adView")
    }

    override fun adViewClicked(adView: BannerView, adInfo: AdInfo?) {
        print("App - didClicked. AdView: $adView, AdInfo: $adInfo")
    }



}