package io.bidapp.networks.mytarget

import android.app.Activity
import android.content.Context
import com.my.target.ads.InterstitialAd
import com.my.target.common.models.IAdLoadingError
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.protocols.BIDFullscreenAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDFullscreenAdapterProtocol

class BIDMyTargetInterstitial(
    val adapter: BIDFullscreenAdapterProtocol? = null,
    val slotId: String? = null
) :
    BIDFullscreenAdapterDelegateProtocol {
    private val slotIdToInt = slotId?.toIntOrNull()
    private val TAG = "Full MyTarget"
    private var ads : InterstitialAd? = null
    var isAdsReady = false

    private val callback = object : InterstitialAd.InterstitialAdListener{
        override fun onLoad(p0: InterstitialAd) {
            isAdsReady = true
            BIDLog.d(TAG, "on ad load $slotId")
            adapter?.onAdLoaded()
        }

        override fun onNoAd(p0: IAdLoadingError, p1: InterstitialAd) {
            BIDLog.d(TAG, "onError $slotId exception: ${p0.message}")
            adapter?.onAdFailedToLoadWithError(p0.message)
        }

        override fun onClick(p0: InterstitialAd) {
            BIDLog.d(TAG, "on ad click. $slotId")
            adapter?.onClick()
        }

        override fun onDismiss(p0: InterstitialAd) {
            BIDLog.d(TAG, "on ad dismiss. $slotId")
            adapter?.onHide()
        }

        override fun onVideoCompleted(p0: InterstitialAd) {
            BIDLog.d(TAG, "Interstitial video completed. $slotId")
        }

        override fun onDisplay(p0: InterstitialAd) {
            BIDLog.d(TAG, "on ad impression $slotId")
            adapter?.onDisplay()
        }

    }


    override fun load(context: Any) {
        if (context as? Context == null) {
            adapter?.onAdFailedToLoadWithError("MyTarget fullscreen loading error")
            return
        }
        if (slotIdToInt == null) {
            adapter?.onAdFailedToLoadWithError("MyTarget fullscreen slotId is null or incorrect format")
            return
        }
        isAdsReady = false
        if (ads == null){
            ads = InterstitialAd(slotIdToInt, context)
        }
        ads?.listener = callback
        ads?.load()
    }

    override fun show(activity: Activity?) {
        if (ads == null && !isAdsReady) {
            adapter?.onFailedToDisplay("Failed to display")
            return
        }
        ads?.show()
    }

    override fun activityNeededForShow(): Boolean {
        return false
    }

    override fun activityNeededForLoad(): Boolean {
        return false
    }

    override fun readyToShow(): Boolean {
        return isAdsReady
    }

    override fun shouldWaitForAdToDisplay(): Boolean {
        return true
    }

    override fun destroy() {
        ads?.destroy()
        ads= null
    }
}