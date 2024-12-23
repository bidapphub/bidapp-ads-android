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
    private val TAG = "Interstitial MyTarget"
    private var ads : InterstitialAd? = null
    private var interstitialAdListener: InterstitialListenerAd? = null

    override fun load(context: Any) {
        if (context as? Context == null) {
            adapter?.onAdFailedToLoadWithError("MyTarget fullscreen loading error")
            return
        }
        if (slotIdToInt == null) {
            adapter?.onAdFailedToLoadWithError("MyTarget fullscreen slotId is null or incorrect format")
            return
        }
        interstitialAdListener = InterstitialListenerAd(TAG, adapter, slotId)
        if (ads == null){
            ads = InterstitialAd(slotIdToInt, context)
        }
        ads?.listener = interstitialAdListener
        ads?.load()
    }

    override fun show(activity: Activity?) {
        if (ads == null && (interstitialAdListener?.isAdsReady == false || interstitialAdListener?.isAdsReady == null)) {
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
        return interstitialAdListener?.isAdsReady ?: false
    }

    override fun shouldWaitForAdToDisplay(): Boolean {
        return true
    }

    override fun destroy() {
        ads?.destroy()
        ads = null
        interstitialAdListener = null
    }

    private class InterstitialListenerAd(
        private val tag : String,
        private val adapter: BIDFullscreenAdapterProtocol?,
        private val slotId: String?
    ) :  InterstitialAd.InterstitialAdListener {
        var isAdsReady = false

        override fun onLoad(p0: InterstitialAd) {
            isAdsReady = true
            BIDLog.d(tag, "Ad load $slotId")
            adapter?.onAdLoaded()
        }

        override fun onNoAd(p0: IAdLoadingError, p1: InterstitialAd) {
            BIDLog.d(tag, "Error $slotId exception: ${p0.message}")
            adapter?.onAdFailedToLoadWithError(p0.message)
        }

        override fun onClick(p0: InterstitialAd) {
            BIDLog.d(tag, "Ad click. $slotId")
            adapter?.onClick()
        }

        override fun onFailedToShow(p0: InterstitialAd) {
            BIDLog.d(tag, "Ad failed to display $slotId")
            adapter?.onFailedToDisplay("on ad failed to display")
        }

        override fun onDismiss(p0: InterstitialAd) {
            BIDLog.d(tag, "Ad dismiss. $slotId")
            adapter?.onHide()
        }

        override fun onVideoCompleted(p0: InterstitialAd) {
            BIDLog.d(tag, "Interstitial video completed. $slotId")
        }

        override fun onDisplay(p0: InterstitialAd) {
            BIDLog.d(tag, "Ad impression $slotId")
            adapter?.onDisplay()
        }
    }
}