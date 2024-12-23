package io.bidapp.networks.bigoads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import io.bidapp.sdk.AdFormat
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.bid.BIDNativeBidData
import io.bidapp.sdk.bid.BIDNativeBidNotify
import io.bidapp.sdk.bid.BidappBid
import io.bidapp.sdk.bid.BidappBidRequester
import io.bidapp.sdk.mediation.bid_completion
import io.bidapp.sdk.protocols.BIDFullscreenAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDFullscreenAdapterProtocol
import sg.bigo.ads.api.AdError
import sg.bigo.ads.api.AdLoadListener
import sg.bigo.ads.api.RewardAdInteractionListener
import sg.bigo.ads.api.RewardVideoAd
import sg.bigo.ads.api.RewardVideoAdLoader
import sg.bigo.ads.api.RewardVideoAdRequest

@PublishedApi
internal class BIDBigoAdsRewarded(
    val adapter: BIDFullscreenAdapterProtocol? = null,
    val slotId: String? = null,
    isReward: Boolean
) : BIDFullscreenAdapterDelegateProtocol {
    val TAG = "Rewarded BigoAds"
    private var bigoRewarded: RewardVideoAd? = null
    private var handler : Handler? = null
    private var task : Runnable? = null
    var isAdsReady = false
    private var rewardedAdListener: RewardedAdListener? = null
    private var rewardedAdLoadListener : AdLoadListener<RewardVideoAd>? = null


    override fun show(activity: Activity?) {
        when{
            bigoRewarded == null -> {
                adapter?.onFailedToDisplay("Failed to display")
                return
            }
            bigoRewarded?.isExpired == true -> {
                adapter?.onFailedToDisplay("Ads is expired")
                return
            }
        }
        rewardedAdListener = RewardedAdListener(TAG, adapter, slotId)
        bigoRewarded?.setAdInteractionListener(rewardedAdListener)
        bigoRewarded?.show(activity)
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
        bigoRewarded = null
        if (bidAppBid?.nativeBid != null) {
            isAdsReady = true
            bigoRewarded = (bidAppBid.nativeBid as RewardVideoAd)
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
        rewardedAdLoadListener = object : AdLoadListener<RewardVideoAd> {
            override fun onError(p0: AdError) {
                BIDLog.d(TAG, "On error $slotId exception: ${p0.message}")
                adapter?.onAdFailedToLoadWithError(p0.message)
            }

            override fun onAdLoaded(p0: RewardVideoAd) {
                task?.let { handler?.removeCallbacks(it) }
                handler = null
                task = null
                isAdsReady = true
                BIDLog.d(TAG, "Ad load $slotId")
                adapter?.onAdLoaded()
                bigoRewarded = p0
            }


        }
        val rewardedAdRequest = RewardVideoAdRequest.Builder().withSlotId(slotId).build()
        val rewardedAdLoader = RewardVideoAdLoader.Builder().withAdLoadListener(rewardedAdLoadListener).build()
        rewardedAdLoader.loadAd(rewardedAdRequest)
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
        bigoRewarded?.destroy()
        bigoRewarded = null
        isAdsReady = false
        rewardedAdLoadListener = null
        rewardedAdListener = null
    }

    private class RewardedAdListener(
        private val tag : String,
        private val adapter: BIDFullscreenAdapterProtocol?,
        private val slotId: String?
    ) : RewardAdInteractionListener {
        var isRewardGranted = false
        override fun onAdError(p0: AdError) {
            BIDLog.d(tag, "onError $slotId exception: ${p0.message}")
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
            BIDLog.d(tag, "Ad end. $slotId")
            if (isRewardGranted) {
                adapter?.onReward()
                isRewardGranted = false
            }
            adapter?.onHide()
        }

        override fun onAdRewarded() {
            BIDLog.d(tag, "Ad rewarded $slotId")
            isRewardGranted = true
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
            val TAG = "BigoAds rewarded bid"
            val adLoadListener = object : AdLoadListener<RewardVideoAd> {
                override fun onError(p0: AdError) {
                    bidCompletion.invoke(null, Error(p0.message.toString()))
                }

                override fun onAdLoaded(bidAds: RewardVideoAd) {
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
            val rewardedAdRequest = RewardVideoAdRequest.Builder().withSlotId(slotId).build()
            val rewardedAdLoader = RewardVideoAdLoader.Builder().withAdLoadListener(adLoadListener).build()
            rewardedAdLoader.loadAd(rewardedAdRequest)
        }
    }
}