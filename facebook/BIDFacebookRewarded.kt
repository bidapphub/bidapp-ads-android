package io.bidapp.networks.facebook

import android.app.Activity
import android.content.Context
import android.media.tv.AdRequest
import android.util.Log
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.RewardedAdListener
import com.facebook.ads.RewardedInterstitialAd
import com.facebook.ads.RewardedInterstitialAdListener
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.protocols.BIDFullscreenAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDFullscreenAdapterProtocol

@PublishedApi
internal class BIDFacebookRewarded(
    val adapter: BIDFullscreenAdapterProtocol? = null,
    val adTag: String? = null,
    isReward: Boolean
) : BIDFullscreenAdapterDelegateProtocol {

    val TAG = "Rewarded Facebook"
    private var rewardedAd: RewardedInterstitialAd? = null
    private var rewardedAdListener : RewardedListenerAd? = null


    override fun load(context: Any) {
        if (context as? Context == null) {
            adapter?.onAdFailedToLoadWithError("Facebook rewarded loading error")
            return
        }
        if (adTag.isNullOrEmpty()) {
            adapter?.onAdFailedToLoadWithError("Facebook rewarded load is failure. adTag is null or empty")
            return
        }
        if (rewardedAd == null) {
            rewardedAd = RewardedInterstitialAd(context, adTag)
        }
        rewardedAdListener = RewardedListenerAd(TAG, adapter, adTag)
        rewardedAd?.loadAd(
            rewardedAd?.buildLoadAdConfig()?.withAdListener(rewardedAdListener)?.build()
        )
    }


    override fun show(activity: Activity?) {
        if (rewardedAd == null || rewardedAd?.isAdLoaded == false || rewardedAd?.isAdInvalidated == true) {
            adapter?.onFailedToDisplay("Ad is not ready or invalidated. $adTag")
            return
        }
        rewardedAd?.show()
    }

    override fun activityNeededForShow(): Boolean {
        return false
    }

    override fun activityNeededForLoad(): Boolean {
        return false
    }

    override fun readyToShow(): Boolean {
        return rewardedAd?.isAdLoaded ?: false
    }

    override fun shouldWaitForAdToDisplay(): Boolean {
        return true
    }

    override fun revenue(): Double? {
        return null
    }

    override fun destroy() {
        rewardedAd?.destroy()
        rewardedAdListener = null
        rewardedAd = null
    }

    private class RewardedListenerAd (
        private val tag : String,
        private val adapter: BIDFullscreenAdapterProtocol?,
        private val adTag: String?
    ) : RewardedInterstitialAdListener {
        var isGrantedReward = false
        override fun onError(p0: Ad?, p1: AdError?) {
            val error = p1?.errorMessage ?: "Unknown error"
            BIDLog.d(tag, "On error $adTag exception: $error")
            adapter?.onAdFailedToLoadWithError(error)
        }

        override fun onAdLoaded(p0: Ad?) {
            BIDLog.d(tag, "Ad load $adTag")
            adapter?.onAdLoaded()
        }

        override fun onAdClicked(p0: Ad?) {
            BIDLog.d(tag, "Ad clicked $adTag")
            adapter?.onClick()
        }

        override fun onLoggingImpression(p0: Ad?) {
            BIDLog.d(tag, "Ad logging impression $adTag")
            adapter?.onDisplay()
        }

        override fun onRewardedInterstitialCompleted() {
            BIDLog.d(tag, "Ad complete $adTag")
            isGrantedReward = true
        }

        override fun onRewardedInterstitialClosed() {
            if (!isGrantedReward) {
                BIDLog.d(tag, "Ad hide $adTag")
                adapter?.onHide()
            } else {
                BIDLog.d(tag, "Ad rewarded $adTag")
                adapter?.onReward()
                isGrantedReward = false
                BIDLog.d(tag, "Ad hide $adTag")
                adapter?.onHide()
            }
        }
    }
}