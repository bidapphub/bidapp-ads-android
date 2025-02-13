package io.bidapp.networks.pangle

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.bytedance.sdk.openadsdk.api.reward.PAGRewardItem
import com.bytedance.sdk.openadsdk.api.reward.PAGRewardedAd
import com.bytedance.sdk.openadsdk.api.reward.PAGRewardedAdInteractionListener
import com.bytedance.sdk.openadsdk.api.reward.PAGRewardedAdLoadListener
import com.bytedance.sdk.openadsdk.api.reward.PAGRewardedRequest
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
internal class BIDPangleRewarded(
    val adapter: BIDFullscreenAdapterProtocol? = null,
    val placementId: String? = null,
    isReward: Boolean
) : BIDFullscreenAdapterDelegateProtocol {
    val TAG = "Rewarded Pangle"
    var rewardedAdListener : RewardedAdListener? = null
    var rewardedAd : PAGRewardedAd? = null
    private var handler : Handler? = null
    private var task : Runnable? = null

    override fun load(context: Any, bidAppBid: BidappBid?) {
        rewardedAd = null
        if (bidAppBid != null){
            rewardedAd = (bidAppBid.nativeBid as? PAGRewardedAd)
            if (rewardedAd == null){
                adapter?.onAdFailedToLoadWithError("Pangle bidding rewarded load is failed. Error cast is failed")
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
            adapter?.onAdFailedToLoadWithError("Pangle rewarded placementID is null or empty")
            return
        }
        val request = PAGRewardedRequest()
        rewardedAdListener = RewardedAdListener(TAG, adapter, placementId, this)
        if (rewardedAdListener != null) {
            PAGRewardedAd.loadAd(placementId, request, rewardedAdListener!!)
        }
        else adapter?.onAdFailedToLoadWithError("Pangle rewarded load listener is null")
    }

    override fun load(context: Any) {
        load(context, null)
    }

    override fun revenue(): Double? {
        return null
    }

    override fun show(activity: Activity?) {
        if (rewardedAd == null || rewardedAdListener == null){
            adapter?.onFailedToDisplay("Failed to display")
            return
        }
        rewardedAd?.setAdInteractionListener(rewardedAdListener)
        rewardedAd?.show(activity)
    }

    override fun activityNeededForShow(): Boolean {
        return true
    }

    override fun activityNeededForLoad(): Boolean {
        return false
    }

    override fun readyToShow(): Boolean {
        return rewardedAd != null
    }

    override fun shouldWaitForAdToDisplay(): Boolean {
        return true
    }

    override fun destroy() {
        handler?.removeCallbacksAndMessages(null)
        handler = null
        task = null
        rewardedAdListener = null
        rewardedAd = null
    }

    class RewardedAdListener(
        private val tag : String,
        private val adapter: BIDFullscreenAdapterProtocol?,
        private val placementId: String?,
        private val bidPangleInterstitial: BIDPangleRewarded?
    ) : PAGRewardedAdLoadListener, PAGRewardedAdInteractionListener {
        var isRewarded = false
        override fun onError(p0: Int, p1: String?) {
            BIDLog.d(tag, "Load Rewarded ad failed $placementId Error: $p1. Code: $p0")
            adapter?.onAdFailedToLoadWithError("Error: $p1. Code: $p0")
            bidPangleInterstitial?.rewardedAd = null
        }

        override fun onAdLoaded(p0: PAGRewardedAd?) {
            BIDLog.d(tag, "Rewarded ad load $placementId")
            adapter?.onAdLoaded()
            bidPangleInterstitial?.rewardedAd = p0
        }

        override fun onAdShowed() {
            BIDLog.d(tag, "Rewarded ad impression $placementId")
            adapter?.onDisplay()
        }

        override fun onAdClicked() {
            BIDLog.d(tag, "Rewarded ad clicked $placementId")
            adapter?.onClick()
        }

        override fun onUserEarnedReward(p0: PAGRewardItem?) {
            BIDLog.d(tag, "Ad rewarded $placementId")
            isRewarded = true
        }

        override fun onUserEarnedRewardFail(p0: Int, p1: String?) {
            BIDLog.d(tag, "Ad rewarded $placementId failed")
            isRewarded = false
        }

        override fun onAdDismissed() {
            BIDLog.d(tag, "Rewarded ad hide $placementId")
            adapter?.onHide()
            if (isRewarded){
                adapter?.onReward()
                isRewarded = false
            }
            bidPangleInterstitial?.rewardedAd = null
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
                bidCompletion.invoke(null, Error("Pangle rewarded bidding is failed Error: placement ID is null"))
                return
            }
            val adLoadListener = object : PAGRewardedAdLoadListener {
                override fun onError(p0: Int, p1: String?) {
                    bidCompletion.invoke(null, Error("Error: $p1. Code: $p0"))
                }

                override fun onAdLoaded(p0: PAGRewardedAd?) {
                    val notifyListener = object : BIDNativeBidNotify {
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
                        bidCompletion.invoke(null, Error("Pangle rewarded bid is null"))
                        return
                    }
                    if (p0.getExtraInfo("price") == null) {
                        bidCompletion.invoke(null, Error("Pangle rewarded bid failed"))
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

            val requestRewarded = PAGRewardedRequest()
            PAGRewardedAd.loadAd(placementId, requestRewarded, adLoadListener)
        }
    }

}

