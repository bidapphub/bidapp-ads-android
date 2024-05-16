package com.bidapp.demo

import io.bidapp.sdk.AdInfo
import io.bidapp.sdk.BIDFullscreenLoadDelegate

class FullscreenLoadDelegate() : BIDFullscreenLoadDelegate {
    override fun didLoad(adInfo: AdInfo?) {
        val waterfallId = adInfo?.getWaterfallId() ?: ""
        val isRewardedString = adInfo?.adFormat?.isRewarded().toString() ?: "null";
        print("Bidapp fullscreen $waterfallId didLoadAd: ${adInfo?.getNetworkId()} $adInfo. IsRewarded: $isRewardedString")
    }

    override fun didFailToLoad(adInfo: AdInfo?, error: Error) {
        val waterfallId = adInfo?.getWaterfallId() ?: ""
        val descr = error.localizedMessage
        print("Bidapp fullscreen $waterfallId didFailToLoadAd: ${adInfo?.getNetworkId()} $adInfo. ERROR: $descr");
    }

}