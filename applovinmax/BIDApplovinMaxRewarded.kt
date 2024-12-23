package io.bidapp.networks.applovinmax

import android.app.Activity
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxError
import com.applovin.mediation.MaxReward
import com.applovin.mediation.MaxRewardedAdListener
import com.applovin.mediation.ads.MaxRewardedAd
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.protocols.BIDFullscreenAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDFullscreenAdapterProtocol
@PublishedApi
internal class BIDApplovinMaxRewarded(
    val adapter: BIDFullscreenAdapterProtocol? = null,
    val adTag: String?,
    var isReward: Boolean
) : BIDFullscreenAdapterDelegateProtocol {
    val TAG = "Reward Max"
    private var rewardedAd: MaxRewardedAd? = null
    private var rewardedAdListener: RewardedAdListener? = null


    private fun setListener () {
        rewardedAdListener = RewardedAdListener(TAG, adapter, adTag)
        rewardedAd?.setListener(rewardedAdListener)
    }


    override fun load(context: Any) {
        if (context as? Activity == null) {
            adapter?.onAdFailedToLoadWithError("Max rewarded loading error")
            return
        }
        if (adTag.isNullOrEmpty()) {
            adapter?.onAdFailedToLoadWithError("Max rewarded adtag is null or empty")
            return
        }
            if (rewardedAd == null) {
                rewardedAd = MaxRewardedAd.getInstance(adTag, context)
            }
            if (RewardedOnDisplay.isOnScreen) adapter?.onAdFailedToLoadWithError("Max rewarded loading error")
            else {
                setListener()
                rewardedAd?.loadAd()
            }
    }

    override fun show(activity: Activity?) {
        if (rewardedAd == null || rewardedAd?.isReady == false){
            adapter?.onFailedToDisplay("Max interstitial showing error")
            return
        }
        rewardedAd?.showAd(activity)
    }

    override fun activityNeededForShow(): Boolean {
        return true
    }

    override fun activityNeededForLoad(): Boolean {
        return true
    }

    override fun readyToShow(): Boolean {
        return rewardedAd?.isReady ?: false
    }

    override fun shouldWaitForAdToDisplay(): Boolean {
        return true
    }

    override fun revenue(): Double? {
        if (rewardedAdListener?.ad != null) {
           return rewardedAdListener?.ad?.revenue
        }
        return null
    }

    override fun destroy() {
        rewardedAd?.setListener(null)
        rewardedAd?.destroy()
        rewardedAd = null
        rewardedAdListener = null
    }


    private class RewardedAdListener(
        private val tag : String,
        private val adapter: BIDFullscreenAdapterProtocol?,
        private val adTag: String?
    ) : MaxRewardedAdListener {
        private var isRewardGranted = false
        var ad:MaxAd? = null

        override fun onAdLoaded(maxAd: MaxAd) {
            BIDLog.d(tag, "Ad loaded. adtag: ($adTag)")
            ad = maxAd
            adapter?.onAdLoaded()
        }

        override fun onAdDisplayed(maxAd: MaxAd) {
            BIDLog.d(tag, "Ad displayed. adtag: ($adTag)")
            adapter?.onDisplay()
            RewardedOnDisplay.isOnScreen = true
        }

        override fun onAdHidden(maxAd: MaxAd) {
            BIDLog.d(tag, "Ad hidden. adtag: ($adTag)")
            if (isRewardGranted){
                adapter?.onReward()
                isRewardGranted = false
            }
            adapter?.onHide()
            RewardedOnDisplay.isOnScreen = false
        }

        override fun onAdClicked(maxAd: MaxAd) {
            BIDLog.d(tag, "Ad clicked. adtag: ($adTag)")
            adapter?.onClick()
        }

        override fun onAdLoadFailed(p0: String, p1: MaxError) {
            val errorDescription = p1.toString()
            BIDLog.d(tag, "Ad load failed error $errorDescription adtag: ($adTag)")
            adapter?.onAdFailedToLoadWithError(p1.toString())
        }

        override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
            val errorDescription = p1.toString()
            BIDLog.d(tag, "Ad display failed error $errorDescription adtag: ($adTag)")
            adapter?.onFailedToDisplay(p1.toString())
        }

        override fun onUserRewarded(p0: MaxAd, p1: MaxReward) {
            BIDLog.d(tag, "On user rewarded adtag: ($adTag)")
            isRewardGranted = true
        }
    }
}

object RewardedOnDisplay {
    var isOnScreen = false
}