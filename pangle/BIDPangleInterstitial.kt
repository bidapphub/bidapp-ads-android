package io.bidapp.networks.pangle

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialAd
import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialAdInteractionListener
import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialAdLoadListener
import com.bytedance.sdk.openadsdk.api.interstitial.PAGInterstitialRequest
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
internal class BIDPangleInterstitial (
    private val adapter: BIDFullscreenAdapterProtocol? = null,
    private val placementId: String? = null
) : BIDFullscreenAdapterDelegateProtocol {
    val TAG = "Interstitial Pangle"
    var interstitialAdListener : InterstitialAdListener? = null
    var interstitialAd : PAGInterstitialAd? = null
    private var handler : Handler? = null
    private var task : Runnable? = null

    override fun load(context: Any, bidAppBid: BidappBid?) {
        interstitialAd = null
        if (bidAppBid != null){
            interstitialAd = (bidAppBid.nativeBid as? PAGInterstitialAd)
            if (interstitialAd == null){
                adapter?.onAdFailedToLoadWithError("Pangle bidding interstitial load is failed. Error cast is failed")
                return
            }
            if (handler == null) handler = Handler(Looper.getMainLooper())
            task = Runnable {
                adapter?.onAdLoaded()
            }
            handler?.postDelayed(task!!, 1000)
            return
        }
        if (placementId.isNullOrEmpty()) {
            adapter?.onAdFailedToLoadWithError("Pangle interstitial placementID is null or empty")
            return
        }
        val request = PAGInterstitialRequest()
        interstitialAdListener = InterstitialAdListener(TAG, adapter, placementId, this)
        if (interstitialAdListener != null) {
            PAGInterstitialAd.loadAd(placementId, request, interstitialAdListener!!)
        }
        else adapter?.onAdFailedToLoadWithError("Pangle interstitial load listener is null")
    }

    override fun load(context: Any) {
        load(context, null)
    }

    override fun revenue(): Double? {
        return null
    }

    override fun show(activity: Activity?) {
       if (interstitialAd == null || interstitialAdListener == null){
           adapter?.onFailedToDisplay("Failed to display")
           return
       }
        interstitialAd?.setAdInteractionListener(interstitialAdListener)
        interstitialAd?.show(activity)
    }

    override fun activityNeededForShow(): Boolean {
        return true
    }

    override fun activityNeededForLoad(): Boolean {
        return false
    }

    override fun readyToShow(): Boolean {
        return interstitialAd != null
    }

    override fun shouldWaitForAdToDisplay(): Boolean {
        return true
    }

    override fun destroy() {
        handler?.removeCallbacksAndMessages(null)
        handler = null
        task = null
        interstitialAdListener = null
        interstitialAd = null
    }

    class InterstitialAdListener(
        private val tag : String,
        private val adapter: BIDFullscreenAdapterProtocol?,
        private val placementId: String?,
        private val bidPangleInterstitial: BIDPangleInterstitial?
    ) : PAGInterstitialAdLoadListener, PAGInterstitialAdInteractionListener{
        override fun onError(p0: Int, p1: String?) {
            BIDLog.d(tag, "Load interstitial ad failed $placementId Error: $p1. Code: $p0")
            adapter?.onAdFailedToLoadWithError("Error: $p1. Code: $p0")
        }

        override fun onAdLoaded(p0: PAGInterstitialAd?) {
            BIDLog.d(tag, "Interstitial ad load $placementId")
            adapter?.onAdLoaded()
            bidPangleInterstitial?.interstitialAd = p0
        }

        override fun onAdShowed() {
            BIDLog.d(tag, "Interstitial ad impression $placementId")
            adapter?.onDisplay()
        }

        override fun onAdClicked() {
            BIDLog.d(tag, "Interstitial ad clicked $placementId")
            adapter?.onClick()
        }

        override fun onAdDismissed() {
            BIDLog.d(tag, "Interstitial ad hide $placementId")
            adapter?.onHide()
            bidPangleInterstitial?.interstitialAd = null
        }

    }

    companion object {
        fun bid(
            context: Context?,
            request: BidappBidRequester,
            placementId: String?,
            appId: String?,
            accountId: String?,
            adFormat: AdFormat?,
            bidCompletion: bid_completion
        ) {
            if (placementId == null){
                bidCompletion.invoke(null, Error("Pangle interstitial bidding is failed Error: placement ID is null"))
                return
            }
            val adLoadListener = object : PAGInterstitialAdLoadListener {
                    override fun onError(p0: Int, p1: String?) {
                        bidCompletion.invoke(null, Error("Error: $p1. Code: $p0"))
                    }

                    override fun onAdLoaded(p0: PAGInterstitialAd?) {
                        val notifyListener = object : BIDNativeBidNotify{
                            override fun winNotify(secPrice: Double?, secBidder: String?) {
                                p0?.win(secPrice)
                            }

                            override fun loseNotify(
                                firstPrice: Double?,
                                firstBidder: String?,
                                lossReason: Int?
                            ) {
                                p0?.loss(firstPrice, firstBidder, lossReason.toString() ?: "102")
                            }
                        }
                        if (p0 == null){
                            bidCompletion.invoke(null, Error("Pangle interstitial bid is null"))
                            return
                        }
                        if (p0.getExtraInfo("price") == null) {
                            bidCompletion.invoke(null, Error("Pangle interstitial bid failed"))
                            return
                        }
                        val price = p0.getExtraInfo("price").toString().toDoubleOrNull()
                        request.requestBidsWithCompletion(
                            BIDNativeBidData(p0, price ?: 0.0, notifyListener),
                            placementId,
                            appId,
                            accountId,
                            adFormat,
                            BIDPangleSDK.testMode,
                            BIDPangleSDK.COPPA,
                            bidCompletion
                        )

                    }

                }

            val requestInterstitial = PAGInterstitialRequest()
            PAGInterstitialAd.loadAd(placementId, requestInterstitial, adLoadListener)
        }
    }

}