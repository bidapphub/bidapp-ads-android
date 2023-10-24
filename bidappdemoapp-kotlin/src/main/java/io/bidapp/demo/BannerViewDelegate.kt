package io.bidapp.demo

import android.app.Activity
import java.lang.ref.WeakReference

class BannerViewDelegate(activity: Activity) : BIDBannerViewDelegate {
    val activityWeakReference = WeakReference(activity)

    override fun adViewReadyToRefresh(adView: BannerView, adInfo: AdInfo) {
        print("App - adViewReadyToRefresh. AdView: $adView, AdInfo: $adInfo")
        adView.startAutoRefresh(20.0)
    }

    override fun adViewDidDisplayAd(adView: BannerView, adInfo: AdInfo) {
        print("App - didDisplayAd. AdView: $adView, AdInfo: $adInfo")
    }

    override fun adViewDidFailToDisplayAd(adView: BannerView, adInfo: AdInfo, errors: Error) {
        print("App - didFailToDisplayAd. AdView: $adView, Error:${errors.localizedMessage}")
    }

    override fun adViewClicked(adView: BannerView, adInfo: AdInfo) {
        print("App - didClicked. AdView: $adView, AdInfo: $adInfo")
    }


    override fun activityForShowAd(): Activity? {
          return activityWeakReference.get()
    }
}