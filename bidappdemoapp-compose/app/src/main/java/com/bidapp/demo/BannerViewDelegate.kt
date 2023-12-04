package com.bidapp.demo


import io.bidapp.sdk.AdInfo
import io.bidapp.sdk.BIDBannerViewDelegate
import io.bidapp.sdk.BannerView


class BannerViewDelegate() : BIDBannerViewDelegate {

    override fun adViewReadyToRefresh(adView: BannerView, adInfo: AdInfo?) {
        print("App - adViewReadyToRefresh. AdView: $adView, AdInfo: $adInfo")
        adView.startAutoRefresh(20.0)
    }

    override fun adViewDidDisplayAd(adView: BannerView, adInfo: AdInfo?) {
        print("App - didDisplayAd. AdView: $adView, AdInfo: $adInfo")
    }

    override fun adViewDidFailToDisplayAd(adView: BannerView, adInfo: AdInfo?, errors: Error) {
        print("App - didFailToDisplayAd. AdView: $adView, Error:${errors.localizedMessage}")
    }

    override fun adViewClicked(adView: BannerView, adInfo: AdInfo?) {
        print("App - didClicked. AdView: $adView, AdInfo: $adInfo")
    }



}