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
    private var ad:MaxAd? = null
    var isGrantedReward = false
    private val rewardedListener = object : MaxRewardedAdListener {

        override fun onAdLoaded(maxAd: MaxAd) {
           BIDLog.d(TAG, "ad loaded. adtag: ($adTag)")
           ad = maxAd
           adapter?.onAdLoaded()
        }

        override fun onAdDisplayed(maxAd: MaxAd) {
           BIDLog.d(TAG, "ad displayed. adtag: ($adTag)")
            adapter?.onDisplay()
               RewardedOnDisplay.isOnScreen = true
        }

        override fun onAdHidden(maxAd: MaxAd) {
            BIDLog.d(TAG, "ad hidden. adtag: ($adTag)")
            if (isGrantedReward){
                adapter?.onReward()
                isGrantedReward = false
            }
            adapter?.onHide()
            RewardedOnDisplay.isOnScreen = false
        }

        override fun onAdClicked(maxAd: MaxAd) {
            BIDLog.d(TAG, "ad clicked. adtag: ($adTag)")
            adapter?.onClick()
        }

        override fun onAdLoadFailed(p0: String, p1: MaxError) {
             val errorDescription = p1.toString()
            BIDLog.d(TAG, "ad load failed error $errorDescription adtag: ($adTag)")
            adapter?.onAdFailedToLoadWithError(p1.toString())
        }

        override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
            val errorDescription = p1.toString()
            BIDLog.d(TAG, "ad display failed error $errorDescription adtag: ($adTag)")
            adapter?.onFailedToDisplay(p1.toString())
        }

        override fun onUserRewarded(p0: MaxAd, p1: MaxReward) {
            BIDLog.d(TAG, "on user rewarded adtag: ($adTag)")
            isGrantedReward = true
        }

        @Deprecated("Deprecated in Java")
        override fun onRewardedVideoStarted(p0: MaxAd) {
        }

        @Deprecated("Deprecated in Java")
        override fun onRewardedVideoCompleted(p0: MaxAd) {
        }
    }

    fun setListener () {
        rewardedAd?.setListener(rewardedListener)
    }


    override fun load(context: Any) {
        if (context as? Activity == null) {
            adapter?.onAdFailedToLoadWithError("Max rewarded loading error")
            return
        }
        if (adTag == null) {
            adapter?.onAdFailedToLoadWithError("Max rewarded adtag is null")
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
        rewardedAd?.showAd()
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

    override fun destroy() {
        rewardedAd?.setListener(null)
        rewardedAd?.destroy()
        rewardedAd = null
        ad = null
    }
}

object RewardedOnDisplay {
    var isOnScreen = false
}