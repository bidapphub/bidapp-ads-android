package com.bidapp.demo

import io.bidapp.sdk.AdInfo
import io.bidapp.sdk.BIDInterstitialDelegate
import io.bidapp.sdk.BIDRewardedDelegate

class FullscreenShowDelegate() : BIDRewardedDelegate, BIDInterstitialDelegate {
    var sessionId:String = ""
    var waterfallId:String = ""

    override fun didRewardUser() {
        print("Bidapp fullscreen didClickAd")
    }

    override fun didDisplayAd(adInfo: AdInfo?) {
        sessionId = adInfo?.getShowSessionId() ?: ""
        sessionId = sessionId.substring(0, 3)
        waterfallId = adInfo?.getWaterfallId() ?: ""
        print("Bidapp fullscreen $sessionId $waterfallId didDisplayAd: ${adInfo?.getNetworkId()}")
    }

    override fun didClickAd(adInfo: AdInfo?) {
        print("Bidapp fullscreen $sessionId $waterfallId didClickAd: ${adInfo?.getNetworkId()}")
    }

    override fun didHideAd(adInfo: AdInfo?) {
        print("Bidapp fullscreen $sessionId $waterfallId didHideAd: ${adInfo?.getNetworkId()}")
    }

    override fun didFailToDisplayAd(adInfo: AdInfo?, error: Error) {
        sessionId = adInfo?.getShowSessionId() ?: ""
        sessionId = sessionId.substring(0, 3)
        waterfallId = adInfo?.getWaterfallId() ?: ""
        print("Bidapp fullscreen $sessionId $waterfallId didHideAd: ${adInfo?.getNetworkId()} Error:${error.localizedMessage}")
    }

    override fun allNetworksDidFailToDisplayAd() {
        print("Bidapp fullscreen $sessionId $waterfallId allNetworksDidFailToDisplayAd")
    }


}