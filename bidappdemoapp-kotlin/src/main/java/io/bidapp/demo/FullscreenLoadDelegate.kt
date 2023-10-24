package io.bidapp.demo

import android.app.Activity
import java.lang.ref.WeakReference

class FullscreenLoadDelegate(activity: Activity) : BIDFullscreenLoadDelegate {
    val activityWeakReference = WeakReference(activity)
    override fun didLoad(adInfo: AdInfo?) {
        val waterfallId = adInfo?.waterfallId ?: ""
        val isRewardedString = adInfo?.format?.isRewarded().toString() ?: "null";
        print("Bidapp fullscreen $waterfallId didLoadAd: ${adInfo?.networkId} $adInfo. IsRewarded: $isRewardedString")
    }

    override fun didFailToLoad(adInfo: AdInfo?, error: Error) {
        val waterfallId = adInfo?.waterfallId ?: ""
        val descr = error.localizedMessage
        print("Bidapp fullscreen $waterfallId didFailToLoadAd: ${adInfo?.networkId} $adInfo. ERROR: $descr");
    }

    override fun activityForLoadAd(): Activity? {
      return activityWeakReference.get()
    }
}