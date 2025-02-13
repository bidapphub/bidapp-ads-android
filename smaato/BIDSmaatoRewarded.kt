package io.bidapp.networks.smaato

import android.app.Activity
import android.content.Context
import com.smaato.sdk.core.SmaatoSdk
import com.smaato.sdk.core.ad.AdRequestParams
import com.smaato.sdk.rewarded.RewardedError
import com.smaato.sdk.rewarded.RewardedInterstitial
import com.smaato.sdk.rewarded.RewardedInterstitialAd
import com.smaato.sdk.rewarded.RewardedRequestError
import com.smaato.sdk.ub.UBBid
import com.smaato.sdk.ub.UnifiedBidding.PrebidListener
import com.smaato.sdk.ub.UnifiedBidding.prebidRewardedInterstitial
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
internal class BIDSmaatoRewarded(
    private val adapter: BIDFullscreenAdapterProtocol? = null,
    private val adSpaceId: String? = null,
    isReward: Boolean
) : BIDFullscreenAdapterDelegateProtocol {
    private val TAG = "Rewarded Smaato"
    private var rewardedAdListener : RewardedAdListener? = null


    override fun show(activity: Activity?) {
        if (rewardedAdListener?.rewardedAd == null || rewardedAdListener?.rewardedAd?.isAvailableForPresentation != true){
            adapter?.onFailedToDisplay("Error Smaato showing interstitial ads not ready. adSpaceId: ($adSpaceId)")
            return
        }
        rewardedAdListener?.rewardedAd?.showAd()
    }

    override fun load(context: Any, bidAppBid: BidappBid?) {
        if (adSpaceId.isNullOrEmpty()) {
            adapter?.onAdFailedToLoadWithError("Smaato interstitial adSpaceId is null or empty")
            return
        }
        rewardedAdListener = RewardedAdListener(TAG, adapter, adSpaceId)
        if (bidAppBid != null){
            val ubBID = (bidAppBid.nativeBid as? UBBid)
            if (ubBID == null){
                adapter?.onAdFailedToLoadWithError("Smaato interstitial ubBid is null or empty")
                return
            }
            val uniqueID = ubBID.metadata[UBBid.MetadataKeys.UNIQUE_ID].toString()
            val adRequestParam = AdRequestParams.builder().setUBUniqueId(uniqueID).build()
            RewardedInterstitial.loadAd(adSpaceId, rewardedAdListener!!, adRequestParam)
        }
        else {
            RewardedInterstitial.loadAd(adSpaceId, rewardedAdListener!!)
        }
    }

    override fun load(context: Any) {
        load(context, null)
    }

    override fun activityNeededForShow(): Boolean {
        return false
    }

    override fun activityNeededForLoad(): Boolean {
        return false
    }

    override fun readyToShow(): Boolean {
        return rewardedAdListener?.rewardedAd?.isAvailableForPresentation ?: false
    }

    override fun shouldWaitForAdToDisplay(): Boolean {
        return true
    }

    override fun destroy() {
        rewardedAdListener = null
    }

    override fun revenue(): Double? {
        return null
    }

    class RewardedAdListener(
        private val tag : String,
        private val adapter: BIDFullscreenAdapterProtocol?,
        private val adSpaceId: String?
    ) : com.smaato.sdk.rewarded.EventListener {
        var rewardedAd: RewardedInterstitialAd? = null
        var isLoadSuccess = false
        var isRewardGranted = false
        override fun onAdLoaded(p0: RewardedInterstitialAd) {
            rewardedAd = p0
            BIDLog.d(tag, "Rewarded ad load $adSpaceId")
            adapter?.onAdLoaded()
            isLoadSuccess = true
        }

        override fun onAdFailedToLoad(p0: RewardedRequestError) {
            BIDLog.d(tag, "Load Rewarded ad failed $adSpaceId Error: ${p0.rewardedError.name}")
            adapter?.onAdFailedToLoadWithError(p0.rewardedError.name)
            isLoadSuccess = false
            rewardedAd = null
        }

        override fun onAdError(p0: RewardedInterstitialAd, p1: RewardedError) {
            if (isLoadSuccess){
                BIDLog.d(tag, "Load Rewarded ad failed $adSpaceId Error: ${p1.name}")
                adapter?.onFailedToDisplay(p1.name)
            }
            else {
                BIDLog.d(tag, "Rewarded ad display failed $adSpaceId Error: ${p1.name}")
                BIDLog.d(tag, "Load Rewarded ad failed $adSpaceId Error: ${p1.name}")
                adapter?.onAdFailedToLoadWithError(p1.name)
                adapter?.onFailedToDisplay(p1.name)
            }
            rewardedAd = null
        }

        override fun onAdClosed(p0: RewardedInterstitialAd) {
            BIDLog.d(tag, "Rewarded ad hide $adSpaceId")
            if (isRewardGranted){
                adapter?.onReward()
                isRewardGranted = false
            }
            adapter?.onHide()
            rewardedAd = p0
        }

        override fun onAdClicked(p0: RewardedInterstitialAd) {
            BIDLog.d(tag, "Rewarded ad clicked $adSpaceId")
            adapter?.onClick()
            rewardedAd = p0
        }

        override fun onAdStarted(p0: RewardedInterstitialAd) {
            BIDLog.d(tag, "Rewarded ad impression $adSpaceId")
            adapter?.onDisplay()
            rewardedAd = p0
        }

        override fun onAdReward(p0: RewardedInterstitialAd) {
            BIDLog.d(tag, "Ad rewarded $adSpaceId")
            isRewardGranted = true
            rewardedAd = p0
        }

        override fun onAdTTLExpired(p0: RewardedInterstitialAd) {
            BIDLog.d(tag, "Rewarded ad TTLExpired $adSpaceId")
            rewardedAd = null
        }

    }
    companion object {
        fun bid(
            context: Context?,
            request: BidappBidRequester,
            adSpaceId: String?,
            appId: String?,
            accountId: String?,
            adFormat: AdFormat?,
            bidCompletion: bid_completion
        ) {
            BIDSmaatoSDK.COPPA?.let { SmaatoSdk.setCoppa(it) }
            val listener = PrebidListener { p0, p1 ->
                if (p1 != null) {
                    bidCompletion.invoke(null, Error(p1.error.name))
                    return@PrebidListener
                }
                val notifyListener = object : BIDNativeBidNotify {
                    override fun winNotify(secPrice: Double?, secBidder: String?) {}

                    override fun loseNotify(
                        firstPrice: Double?,
                        firstBidder: String?,
                        lossReason: Int?
                    ) {}
                }
                if (p0 == null){
                    bidCompletion.invoke(null, Error("Smaatto rewarded bid is null"))
                    return@PrebidListener
                }
                request.requestBidsWithCompletion(BIDNativeBidData(p0,p0.bidPrice ?: 0.0, notifyListener), adSpaceId, appId, accountId, adFormat, BIDSmaatoSDK.testMode, BIDSmaatoSDK.COPPA, bidCompletion)
            }
            if (adSpaceId == null){
                bidCompletion.invoke(null, Error("Smaatto rewarded adSpaceId is null"))
                return
            }
            prebidRewardedInterstitial(adSpaceId, listener)
        }
    }

}

