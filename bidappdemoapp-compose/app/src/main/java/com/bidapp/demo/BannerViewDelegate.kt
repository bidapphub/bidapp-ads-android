package com.bidapp.demo


import android.util.Log
import android.widget.FrameLayout
import io.bidapp.sdk.AdInfo
import io.bidapp.sdk.BIDBannerViewDelegate
import io.bidapp.sdk.BannerView
import java.lang.ref.WeakReference


class BannerViewDelegate : BIDBannerViewDelegate {

   private var weakView : WeakReference<FrameLayout>? = null

    fun setView(view : FrameLayout){
        weakView = WeakReference(view)
    }


    override fun adViewDidDisplayAd(adView: BannerView, adInfo: AdInfo?) {
        print("App - didDisplayAd. AdView: $adView, AdInfo: $adInfo")
    }

    override fun adViewDidFailToDisplayAd(adView: BannerView, adInfo: AdInfo?, errors: Error) {
        print("App - didFailToDisplayAd. AdView: $adView, Error:${errors.localizedMessage}")
    }

    override fun adViewDidLoadAd(adView: BannerView, adInfo: AdInfo?) {
        weakView?.get()?.removeAllViews()
        weakView?.get()?.addView(adView)
        print("App - adViewDidLoadAd. AdView: $adView, AdInfo: $adInfo")
    }

    override fun allNetworksFailedToDisplayAd(adView: BannerView) {
        print("App - didFailToDisplayAd. AdView: $adView")
    }

    override fun adViewClicked(adView: BannerView, adInfo: AdInfo?) {
        print("App - didClicked. AdView: $adView, AdInfo: $adInfo")
    }



}