package io.bidapp.networks.facebook

import android.app.Activity
import android.content.Context
import android.media.tv.AdRequest
import android.util.Log
import com.facebook.ads.Ad
import com.facebook.ads.AdError
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
    var isGrantedReward = false
    private var rewardedAd: RewardedInterstitialAd? = null


    private val rewardedAdListener = object : RewardedInterstitialAdListener {
        override fun onError(p0: Ad?, p1: AdError?) {
            val error = p1?.errorMessage ?: "Unknown error"
            BIDLog.d(TAG, "onError $adTag exception: $error")
            adapter?.onAdFailedToLoadWithError(error)
        }

        override fun onAdLoaded(p0: Ad?) {
            BIDLog.d(TAG, "ad load $adTag")
            adapter?.onAdLoaded()
        }

        override fun onAdClicked(p0: Ad?) {
            BIDLog.d(TAG, "ad clicked $adTag")
            adapter?.onAdLoaded()
        }

        override fun onLoggingImpression(p0: Ad?) {
            BIDLog.d(TAG, "rewarded ad logging impression $adTag")
            adapter?.onDisplay()
        }

        override fun onRewardedInterstitialCompleted() {
            BIDLog.d(TAG, "ad complete $adTag")
            isGrantedReward = true
        }

        override fun onRewardedInterstitialClosed() {
            if (!isGrantedReward) {
                BIDLog.d(TAG, "ad hide $adTag")
                adapter?.onHide()
            } else {
                BIDLog.d(TAG, "ad rewarded $adTag")
                adapter?.onReward()
                isGrantedReward = false
                BIDLog.d(TAG, "ad hide $adTag")
                adapter?.onHide()
            }
        }

    }

    override fun load(context: Any) {
        if (context as? Context == null) {
            adapter?.onAdFailedToLoadWithError("Facebook rewarded loading error")
            return
        }
        if (adTag == null) {
            adapter?.onAdFailedToLoadWithError("Facebook rewarded load is failure. adtag is null")
            return
        }
        if (rewardedAd == null) {
            rewardedAd = RewardedInterstitialAd(context, adTag)
        }
        rewardedAd?.loadAd(
            rewardedAd?.buildLoadAdConfig()?.withAdListener(rewardedAdListener)?.build()
        )
    }


    override fun show(activity: Activity?) {
        if (rewardedAd == null || rewardedAd?.isAdLoaded == false || rewardedAd?.isAdInvalidated == true) {
            adapter?.onFailedToDisplay("ad is not ready or invalidated. $adTag")
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
        rewardedAd = null
    }
}