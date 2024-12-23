package io.bidapp.networks.inmobi

import android.app.Activity
import android.os.Handler
import android.os.Looper
import com.inmobi.ads.AdMetaInfo
import com.inmobi.ads.InMobiAdRequestStatus
import com.inmobi.ads.InMobiInterstitial
import com.inmobi.ads.listeners.InterstitialAdEventListener
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.protocols.BIDFullscreenAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDFullscreenAdapterProtocol

@PublishedApi
internal class BIDInMobiFullscreen(
    val adapter: BIDFullscreenAdapterProtocol? = null,
    val placementId: String? = null,
    isReward: Boolean
) : BIDFullscreenAdapterDelegateProtocol {
    val TAG = "Interstitial IronSource"
    val adType = if (isReward) "Rewarded" else "Interstitial"
    private var interstitialAdListener: InterstitialAdListener? = null
    private var interstitialAd : InMobiInterstitial? = null


    override fun revenue(): Double? {
        return null
    }

    override fun load(context: Any) {
        if (context as? Activity == null){
            adapter?.onAdFailedToLoadWithError("InMobi $adType load is failed. Activity is null")
            return
        }
        if (placementId == null || placementId.toLongOrNull() == null){
            adapter?.onAdFailedToLoadWithError("InMobi $adType load is failed. Placement ID is null")
            return
        }

        val load = Runnable {
            interstitialAdListener = InterstitialAdListener(adType, TAG, adapter, placementId)
            interstitialAd = InMobiInterstitial(context, placementId.toLong(), interstitialAdListener!!)
            interstitialAd?.load()
        }
        (context as? Activity)?.runOnUiThread(load) ?: run {
            adapter?.onAdFailedToLoadWithError("InMobi $adType load is failed. Activity is null")
        }
    }

    override fun show(activity: Activity?) {
        if (interstitialAd?.isReady() == false){
            adapter?.onFailedToDisplay("InMobi $adType showing is failure is ad not ready")
            return
        }
        interstitialAd?.show()
    }

    override fun activityNeededForShow(): Boolean {
        return true
    }

    override fun activityNeededForLoad(): Boolean {
        return true
    }

    override fun readyToShow(): Boolean {
        return interstitialAd?.isReady() ?: false
    }

    override fun shouldWaitForAdToDisplay(): Boolean {
        return true
    }

    override fun destroy() {
        interstitialAd = null
        interstitialAdListener = null
    }

    private class InterstitialAdListener(
        private val adType : String,
        private val tag: String,
        private val adapter: BIDFullscreenAdapterProtocol?,
        private val placementId: String?
    ) : InterstitialAdEventListener() {
        var isRewardGranted = false

        override fun onRewardsUnlocked(p0: InMobiInterstitial, p1: MutableMap<Any, Any>?) {
            BIDLog.d(tag, "Ad rewarded $placementId")
            isRewardGranted = true
        }


        override fun onAdWillDisplay(p0: InMobiInterstitial) {
            BIDLog.d(tag, "$adType will show. $placementId")
        }

        override fun onAdDisplayFailed(p0: InMobiInterstitial) {
            val errorMessage = "$adType ad failed to display $placementId"
            BIDLog.d(tag, "$adType ad display failed $placementId Error: $errorMessage")
            adapter?.onFailedToDisplay(errorMessage)
        }

        override fun onAdDisplayed(p0: InMobiInterstitial, p1: AdMetaInfo) {
            BIDLog.d(tag, "$adType did show $placementId")
        }

        override fun onAdImpression(p0: InMobiInterstitial) {
            BIDLog.d(tag, "$adType ad impression $placementId")
            adapter?.onDisplay()
        }


        override fun onAdClicked(p0: InMobiInterstitial, p1: MutableMap<Any, Any>?) {
            BIDLog.d(tag, "$adType ad clicked $placementId")
            adapter?.onClick()
        }

        override fun onAdLoadFailed(p0: InMobiInterstitial, p1: InMobiAdRequestStatus) {
            val errorMessage = p1.message ?: "Unknown Error"
            BIDLog.d(tag, "Load $adType ad failed $placementId Error: $errorMessage")
            adapter?.onAdFailedToLoadWithError(errorMessage)
        }

        override fun onAdLoadSucceeded(p0: InMobiInterstitial, p1: AdMetaInfo) {
            BIDLog.d(tag, "$adType ad load $placementId")
            adapter?.onAdLoaded()
        }

        override fun onAdDismissed(p0: InMobiInterstitial) {
            BIDLog.d(tag, "$adType ad hide $placementId")
            if (isRewardGranted) {
                adapter?.onReward()
                isRewardGranted = false
            }
            adapter?.onHide()
        }

        override fun onUserLeftApplication(p0: InMobiInterstitial) {
            BIDLog.d(tag, "$adType will leave application $placementId")
        }
    }


}