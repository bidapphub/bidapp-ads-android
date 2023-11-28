package io.bidapp.networks.applovinmax

import android.app.Activity
import android.util.Log
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
    private var ad:MaxAd? = null

    private val rewardedListener = object : MaxRewardedAdListener {

        override fun onAdLoaded(maxAd: MaxAd) {
           BIDLog.d(TAG, "Reward max onAdLoaded")
           ad = maxAd
           adapter?.onAdLoaded()
        }

        override fun onAdDisplayed(maxAd: MaxAd) {
           BIDLog.d(TAG, "Reward max onAdDisplayed")
            adapter?.onDisplay()
               RewardedOnDisplay.isOnScreen = true
        }

        override fun onAdHidden(maxAd: MaxAd) {
            BIDLog.d(TAG, "Reward max onAdHidden")
            adapter?.onHide()
            RewardedOnDisplay.isOnScreen = false
        }

        override fun onAdClicked(maxAd: MaxAd) {
            BIDLog.d(TAG, "Reward max onAdClicked")
            adapter?.onClick()
        }

        override fun onAdLoadFailed(p0: String, p1: MaxError) {
             val errorDescription = p1.toString()
            BIDLog.d(TAG, "Reward max onAdLoadFailed error $errorDescription")
            adapter?.onAdFailedToLoadWithError(p1.toString())
        }

        override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
            val errorDescription = p1.toString()
            BIDLog.d(TAG, "Reward max onAdDisplayFailed error $errorDescription")
            adapter?.onFailedToDisplay(p1.toString())
        }

        override fun onUserRewarded(p0: MaxAd, p1: MaxReward) {
            BIDLog.d(TAG, "Reward max onUserRewarded")
            adapter?.onReward()
        }

        @Deprecated("Deprecated in Java")
        override fun onRewardedVideoStarted(p0: MaxAd) {
        }

        @Deprecated("Deprecated in Java")
        override fun onRewardedVideoCompleted(p0: MaxAd) {
        }
    }

    fun init () {
        rewardedAd?.setListener(rewardedListener)
    }


    override fun load(context: Any) {

        val load = runCatching {
            if (rewardedAd == null) {
                rewardedAd = MaxRewardedAd.getInstance(adTag, context as Activity)
                init()
            }
            if (RewardedOnDisplay.isOnScreen) adapter?.onAdFailedToLoadWithError("Max rewarded loading error")
            else rewardedAd?.loadAd()
        }
        if (load.isFailure) adapter?.onAdFailedToLoadWithError("Max rewarded loading error")
    }

    override fun show(activity: Activity?) {
        BIDLog.d(TAG, "Reward max show")
        if (rewardedAd?.isReady == true) rewardedAd?.showAd()
    }

    override fun activityNeededForShow(): Boolean {
        return false
    }

    override fun activityNeededForLoad(): Boolean {
        return true
    }

    override fun readyToShow(): Boolean {
        return true
    }

    override fun shouldWaitForAdToDisplay(): Boolean {
        return true
    }

    override fun revenue(): Double? {
        if (ad != null) {
           return ad?.revenue
        }
        return null
    }
}

object RewardedOnDisplay {
    var isOnScreen = false
}