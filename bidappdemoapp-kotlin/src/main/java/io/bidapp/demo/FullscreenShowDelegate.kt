package io.bidapp.demo

import android.app.Activity
import java.lang.ref.WeakReference

class FullscreenShowDelegate(activity: Activity) : BIDRewardedDelegate, BIDInterstitialDelegate {
    var sessionId:String = ""
    var waterfallId:String = ""
    val activityWeakReference = WeakReference(activity)

    override fun didRewardUser() {
        print("Bidapp fullscreen didClickAd")
    }

    override fun didDisplayAd(adInfo: AdInfo?) {
        sessionId = adInfo?.showSessionId ?: ""
        sessionId = sessionId.substring(0, 3)
        waterfallId = adInfo?.waterfallId ?: ""
        print("Bidapp fullscreen $sessionId $waterfallId didDisplayAd: ${adInfo?.networkId}")
    }

    override fun didClickAd(adInfo: AdInfo?) {
        print("Bidapp fullscreen $sessionId $waterfallId didClickAd: ${adInfo?.networkId}")
    }

    override fun didHideAd(adInfo: AdInfo?) {
        print("Bidapp fullscreen $sessionId $waterfallId didHideAd: ${adInfo?.networkId}")
    }

    override fun didFailToDisplayAd(adInfo: AdInfo?, error: Error) {
        sessionId = adInfo?.showSessionId ?: ""
        sessionId = sessionId.substring(0, 3)
        waterfallId = adInfo?.waterfallId ?: ""
        print("Bidapp fullscreen $sessionId $waterfallId didHideAd: ${adInfo?.networkId} Error:${error.localizedMessage}")
    }

    override fun allNetworksDidFailToDisplayAd() {
        print("Bidapp fullscreen $sessionId $waterfallId allNetworksDidFailToDisplayAd")
    }

    override fun activityForShowAd(): Activity? {
      return activityWeakReference.get()
    }
}