package io.bidapp.networks.inmobi

import android.app.Activity
import android.content.Context
import com.inmobi.ads.AdMetaInfo
import com.inmobi.ads.InMobiAdRequestStatus
import com.inmobi.ads.InMobiInterstitial
import com.inmobi.ads.listeners.InterstitialAdEventListener
import io.bidapp.sdk.AdFormat
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.bid.BIDNativeBidData
import io.bidapp.sdk.bid.BIDNativeBidNotify
import io.bidapp.sdk.bid.BidappBid
import io.bidapp.sdk.bid.BidappBidRequester
import io.bidapp.sdk.mediation.bid_completion
import io.bidapp.sdk.protocols.BIDFullscreenAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDFullscreenAdapterProtocol

@PublishedApi
internal class BIDInMobiFullscreen(
    val adapter: BIDFullscreenAdapterProtocol? = null,
    val placementId: String? = null,
    isReward: Boolean
) : BIDFullscreenAdapterDelegateProtocol {
    val TAG = "Interstitial InMobi"
    val adType = if (isReward) "Rewarded" else "Interstitial"
    private var interstitialAdListener: InterstitialAdListener? = null
    private var interstitialAd : InMobiInterstitial? = null


    override fun revenue(): Double? {
        return null
    }

    override fun load(context: Any) {
        load(context, null)
    }

    override fun load(context: Any, bidAppBid: BidappBid?) {
        interstitialAd = null
        if (bidAppBid != null){
            interstitialAd = bidAppBid.nativeBid as? InMobiInterstitial
            interstitialAdListener = InterstitialAdListener(adType, TAG, adapter, placementId)
            interstitialAd?.setListener(interstitialAdListener!!)
            interstitialAd?.preloadManager?.load()
            return
        }
        if (context as? Context == null){
            adapter?.onAdFailedToLoadWithError("InMobi $adType load is failed. Activity is null")
            return
        }
        if (placementId == null || placementId.toLongOrNull() == null){
            adapter?.onAdFailedToLoadWithError("InMobi $adType load is failed. Placement ID is null")
            return
        }
            interstitialAdListener = InterstitialAdListener(adType, TAG, adapter, placementId)
            interstitialAd = InMobiInterstitial(context, placementId.toLong(), interstitialAdListener!!)
            interstitialAd?.load()
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
        return false
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

    companion object {
        fun bid(
            context: Context?,
            request: BidappBidRequester,
            placementId : String?,
            appId: String?,
            accountId: String?,
            adFormat: AdFormat?,
            bidCompletion: bid_completion
        ) {
            var fullscreen : InMobiInterstitial? = null
            if (context == null || placementId == null || placementId.toLongOrNull() == null){
                bidCompletion.invoke(null, Error("InMobi bid is failed." + if (context == null) "Context" else "Placement ID" + "is null"))
                return
            }
            val fullScreenAdListener = object : InterstitialAdEventListener(){
                override fun onAdFetchSuccessful(p0: InMobiInterstitial, p1: AdMetaInfo) {
                    val notifyListener = object : BIDNativeBidNotify {
                        override fun winNotify(secPrice: Double?, secBidder: String?) {}
                        override fun loseNotify(
                            firstPrice: Double?,
                            firstBidder: String?,
                            lossReason: Int?
                        ) {}
                    }
                    if (fullscreen != null) {
                        request.requestBidsWithCompletion(
                            BIDNativeBidData(fullscreen!!, p1.bid, notifyListener),
                            placementId,
                            appId,
                            accountId,
                            adFormat,
                            BIDInMobiSDK.testMode,
                            BIDInMobiSDK.COPPA,
                            bidCompletion
                        )
                    }
                    else bidCompletion.invoke(null, Error("InMobi bid is failed. Fullscreen instance is null"))
                }

                override fun onAdFetchFailed(p0: InMobiInterstitial, p1: InMobiAdRequestStatus) {
                    bidCompletion.invoke(null, Error("InMobi bid is failed. ${p1.message}"))
                }
            }
            fullscreen = InMobiInterstitial(context, placementId.toLong(), fullScreenAdListener)
            fullscreen.preloadManager.preload()
        }
    }
}