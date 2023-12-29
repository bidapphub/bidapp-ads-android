package com.bidapp.demo

import android.util.Log
import io.bidapp.sdk.AdInfo
import io.bidapp.sdk.BIDBannerViewDelegate
import io.bidapp.sdk.BannerView


class BannerViewDelegate : BIDBannerViewDelegate {



    override fun adViewDidDisplayAd(adView: BannerView, adInfo: AdInfo?) {
        print("App - didDisplayAd. AdView: $adView, AdInfo: $adInfo")
        Log.d("mylog", "display")
    }

    override fun adViewDidFailToDisplayAd(adView: BannerView, adInfo: AdInfo?, errors: Error) {
        print("App - didFailToDisplayAd. AdView: $adView, Error:${errors.localizedMessage}")
        Log.d("mylog", "failed display")
    }

    override fun allNetworksFailedToDisplayAd(adView: BannerView) {
        print("App - didClicked. AdView: $adView")
    }

    override fun adViewClicked(adView: BannerView, adInfo: AdInfo?) {
        print("App - didClicked. AdView: $adView, AdInfo: $adInfo")
    }



}