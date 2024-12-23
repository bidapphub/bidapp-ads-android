package io.bidapp.networks.bigoads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import io.bidapp.sdk.AdFormat
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.bid.BidappBid
import io.bidapp.sdk.bid.BidappBidRequester
import io.bidapp.sdk.bid.BIDNativeBidData
import io.bidapp.sdk.bid.BIDNativeBidNotify
import io.bidapp.sdk.mediation.bid_completion
import io.bidapp.sdk.protocols.BIDFullscreenAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDFullscreenAdapterProtocol
import sg.bigo.ads.api.AdError
import sg.bigo.ads.api.AdInteractionListener
import sg.bigo.ads.api.AdLoadListener
import sg.bigo.ads.api.InterstitialAd
import sg.bigo.ads.api.InterstitialAdLoader
import sg.bigo.ads.api.InterstitialAdRequest


@PublishedApi
internal class BIDBigoAdsInterstitial(
    val adapter: BIDFullscreenAdapterProtocol? = null,
    val slotId: String? = null
) : BIDFullscreenAdapterDelegateProtocol {
    val TAG = "Interstitial BigoAds"
    private var bigoInterstitial: InterstitialAd? = null
    private var handler : Handler? = null
    private var task : Runnable? = null
    var isAdsReady = false
    private var interstitialAdListener: InterstitialAdListener? = null
    private var interstitialAdLoadListener : AdLoadListener<InterstitialAd>? = null


    override fun show(activity: Activity?) {
        when {
            bigoInterstitial == null -> {
                adapter?.onFailedToDisplay("Failed to display")
                return
            }

            bigoInterstitial?.isExpired == true -> {
                adapter?.onFailedToDisplay("ads is expired")
                return
            }
        }
        interstitialAdListener = InterstitialAdListener(TAG, adapter, slotId)
        bigoInterstitial?.setAdInteractionListener(interstitialAdListener)
        bigoInterstitial?.show(activity)
    }

    override fun activityNeededForShow(): Boolean {
        return true
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

    override fun load(context: Any, bidAppBid: BidappBid?) {
        isAdsReady = false
        bigoInterstitial = null
        if (bidAppBid?.nativeBid != null) {
            isAdsReady = true
            bigoInterstitial = (bidAppBid.nativeBid as InterstitialAd)
            if (handler == null) handler = Handler(Looper.getMainLooper())
            task = Runnable {
                adapter?.onAdLoaded()
            }
            handler?.postDelayed(task!!, 1000)
            return
        }
        if (slotId.isNullOrEmpty()) {
            adapter?.onAdFailedToLoadWithError("BigoAds fullscreen slot id is null or empty")
            return
        }
        interstitialAdLoadListener = object : AdLoadListener<InterstitialAd> {
            override fun onError(p0: AdError) {
                BIDLog.d(TAG, "onError $slotId exception: ${p0.message}")
                adapter?.onAdFailedToLoadWithError(p0.message)
            }

            override fun onAdLoaded(p0: InterstitialAd) {
                task?.let { handler?.removeCallbacks(it) }
                handler = null
                task = null
                isAdsReady = true
                BIDLog.d(TAG, "Ad load $slotId")
                adapter?.onAdLoaded()
                bigoInterstitial = p0
            }
        }
        val interstitialAdRequest = InterstitialAdRequest.Builder().withSlotId(slotId).build()
        val interstitialAdLoader = InterstitialAdLoader.Builder().withAdLoadListener(interstitialAdLoadListener).build()
        interstitialAdLoader.loadAd(interstitialAdRequest)
    }

    override fun load(context: Any) {
        load(context, null)
    }

    override fun revenue(): Double? {
        return null
    }

    override fun destroy() {
        handler?.removeCallbacksAndMessages(null)
        handler = null
        task = null
        isAdsReady = false
        interstitialAdListener = null
        interstitialAdLoadListener = null
        bigoInterstitial?.destroy()
        bigoInterstitial = null
    }



    private class InterstitialAdListener(
        private val tag : String,
        private val adapter: BIDFullscreenAdapterProtocol?,
        private val slotId: String?
    ) : AdInteractionListener  {

        override fun onAdError(p0: AdError) {
            BIDLog.d(tag, "Ad on error $slotId exception: ${p0.message}")
            adapter?.onFailedToDisplay(p0.message)
        }

        override fun onAdImpression() {
            BIDLog.d(tag, "Ad impression $slotId")
            adapter?.onDisplay()
        }

        override fun onAdClicked() {
            BIDLog.d(tag, "Ad clicked $slotId")
            adapter?.onClick()
        }

        override fun onAdOpened() {
            BIDLog.d(tag, "Ad opened $slotId")
        }

        override fun onAdClosed() {
            BIDLog.d(tag, "Ad close $slotId")
            adapter?.onHide()
        }
    }

    companion object {

        fun bid(
            context: Context?,
            request: BidappBidRequester,
            slotId: String?,
            appId: String?,
            accountId: String?,
            adFormat: AdFormat?,
            bidCompletion: bid_completion
        ) {

            val adLoadListener = object : AdLoadListener<InterstitialAd> {
                override fun onError(p0: AdError) {
                    bidCompletion.invoke(null, Error(p0.message.toString()))
                }
                override fun onAdLoaded(bidAds: InterstitialAd) {
                    val notifyListener = object : BIDNativeBidNotify {
                        override fun winNotify(secPrice: Double?, secBidder: String?) {
                            bidAds.bid?.notifyWin(secPrice, secBidder)
                        }
                        override fun loseNotify(
                            firstPrice: Double?,
                            firstBidder: String?,
                            lossReason: Int?
                        ) {
                            bidAds.bid?.notifyLoss(firstPrice, firstBidder, lossReason ?: 102)
                        }
                    }
                    request.requestBidsWithCompletion(
                        BIDNativeBidData(bidAds, bidAds.bid?.price ?: 0.0, notifyListener),
                        slotId,
                        appId,
                        accountId,
                        adFormat,
                        BIDBigoAdsSDK.testMode,
                        BIDBigoAdsSDK.coppa,
                        bidCompletion
                    )
                }
            }
            val interstitialAdRequest = InterstitialAdRequest.Builder().withSlotId(slotId).build()
            val interstitialAdLoader = InterstitialAdLoader.Builder().withAdLoadListener(adLoadListener).build()
            interstitialAdLoader.loadAd(interstitialAdRequest)
        }
    }
}