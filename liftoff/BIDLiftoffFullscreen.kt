package io.bidapp.networks.liftoff


import android.app.Activity
import android.content.Context
import com.vungle.ads.AdConfig
import com.vungle.ads.BaseAd
import com.vungle.ads.BaseFullscreenAd
import com.vungle.ads.BidTokenCallback
import com.vungle.ads.InterstitialAd
import com.vungle.ads.RewardedAd
import com.vungle.ads.RewardedAdListener
import com.vungle.ads.VungleAds
import com.vungle.ads.VungleError
import io.bidapp.sdk.AdFormat
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.bid.BidappBid
import io.bidapp.sdk.bid.BidappBidRequester
import io.bidapp.sdk.mediation.bid_completion
import io.bidapp.sdk.protocols.BIDFullscreenAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDFullscreenAdapterProtocol


@PublishedApi
internal class BIDLiftoffFullscreen(
    val adapter: BIDFullscreenAdapterProtocol? = null,
    private val adTag: String?,
    val isRewarded: Boolean
) : BIDFullscreenAdapterDelegateProtocol {
    val TAG = if (isRewarded) "Reward Liftoff" else "Interstitial Liftoff"
    private var ads: BaseFullscreenAd? = null
    private var fullscreenAdListener : FullscreenAdListener? = null


    override fun load(context: Any) {
        load(context, null)
    }

    override fun load(context: Any, bidAppBid: BidappBid?) {
        if (context as? Context == null) {
            adapter?.onAdFailedToLoadWithError("Liftoff fullscreen loading error")
            return
        }
        if (adTag.isNullOrEmpty()) {
            adapter?.onAdFailedToLoadWithError("Liftoff fullscreen adTag is null or empty")
            return
        }
        fullscreenAdListener = FullscreenAdListener(TAG, adapter, isRewarded)
        if (isRewarded){
            ads = RewardedAd(context, adTag, AdConfig()).apply {
                adListener = fullscreenAdListener
                if (bidAppBid == null) load()
                else load(bidAppBid.nativeBid.toString())
            }
        }
        else
            ads = InterstitialAd(context, adTag, AdConfig()).apply {
                adListener = fullscreenAdListener
                if (bidAppBid == null) load()
                else load(bidAppBid.nativeBid.toString())
            }
    }

    override fun show(activity: Activity?) {
        if (ads == null || ads?.canPlayAd() == false) {
            adapter?.onFailedToDisplay("Failed to display")
            return
        }
        (ads)?.play()

    }

    override fun activityNeededForShow(): Boolean {
        return false
    }

    override fun activityNeededForLoad(): Boolean {
        return false
    }


    override fun readyToShow(): Boolean {
        return ads?.canPlayAd() ?: false
    }

    override fun shouldWaitForAdToDisplay(): Boolean {
        return true
    }

    override fun revenue(): Double? {
        return null
    }

    override fun destroy() {
        ads?.adListener = null
        fullscreenAdListener = null
        ads = null
    }

    private class FullscreenAdListener (
        private val tag : String,
        private val adapter: BIDFullscreenAdapterProtocol?,
        private val isRewarded : Boolean
    ) :  RewardedAdListener {
        var isRewardGranted = false
        override fun onAdClicked(baseAd: BaseAd) {
            BIDLog.d(tag, "Ad click. ${baseAd.placementId}")
            adapter?.onClick()
        }

        override fun onAdEnd(baseAd: BaseAd) {
            BIDLog.d(tag, "Ad end. ${baseAd.placementId}")
            if (isRewarded) {
                if (isRewardGranted) {
                    adapter?.onReward()
                    isRewardGranted = false
                }
            }
            adapter?.onHide()
        }

        override fun onAdFailedToLoad(baseAd: BaseAd, adError: VungleError) {
            BIDLog.d(tag, "On error ${baseAd.placementId} exception: ${adError.message}")
            adapter?.onAdFailedToLoadWithError(adError.message.toString())
        }

        override fun onAdFailedToPlay(baseAd: BaseAd, adError: VungleError) {
            BIDLog.d(tag, "On error ${baseAd.placementId} exception: ${adError.message}")
            adapter?.onFailedToDisplay(adError.errorMessage)
        }

        override fun onAdImpression(baseAd: BaseAd) {
            BIDLog.d(tag, "Ad impression ${baseAd.placementId}")
            adapter?.onDisplay()
        }

        override fun onAdLeftApplication(baseAd: BaseAd) {
            BIDLog.d(tag, "Ad left application ${baseAd.placementId}")
        }

        override fun onAdLoaded(baseAd: BaseAd) {
            BIDLog.d(tag, "Ad load ${baseAd.placementId}")
            adapter?.onAdLoaded()
        }

        override fun onAdRewarded(baseAd: BaseAd) {
            BIDLog.d(tag, "Ad rewarded ${baseAd.placementId}")
            if(isRewarded) isRewardGranted = true
        }

        override fun onAdStart(baseAd: BaseAd) {
            BIDLog.d(tag, "Ad viewed ${baseAd.placementId}")
        }

    }

    companion object {
        fun bid (context: Context?, request:BidappBidRequester, adTag: String?, appId : String?, accountId : String?, adFormat : AdFormat?, bidCompletion : bid_completion){
            val TAG = "Liftoff bid"
            if (context != null) {
                val tokenCallback = object : BidTokenCallback {
                    override fun onBidTokenCollected(bidToken: String) {
                        request.requestBidsWithCompletion(
                            bidToken,
                            adTag,
                            appId,
                            accountId,
                            adFormat,
                            BIDLiftoffSDK.testMode,
                            BIDLiftoffSDK.coppa,
                            bidCompletion
                        )
                    }
                    override fun onBidTokenError(errorMessage: String) {
                        bidCompletion.invoke(null, Error(errorMessage))
                    }
                }
                VungleAds.getBiddingToken(context, tokenCallback)
            }
            else bidCompletion.invoke(null, Error("Error : Context is null"))
        }
    }



}