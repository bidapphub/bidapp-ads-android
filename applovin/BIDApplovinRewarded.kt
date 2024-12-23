package io.bidapp.networks.applovin

import android.app.Activity
import android.content.Context
import com.applovin.adview.AppLovinIncentivizedInterstitial
import com.applovin.sdk.*
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.protocols.BIDFullscreenAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDFullscreenAdapterProtocol

@PublishedApi
internal class BIDApplovinRewarded(
    val adapter: BIDFullscreenAdapterProtocol? = null,
    var adTag: String? = null,
    var isReward: Boolean
) : BIDFullscreenAdapterDelegateProtocol {
    val TAG = "Reward Applovin"
    private var incentivizedInterstitial: AppLovinIncentivizedInterstitial? = null
    private var rewardedAdListener: RewardedAdListener? = null




    override fun load(context: Any) {
        if (context as? Context == null) {
            adapter?.onAdFailedToLoadWithError("Error Failed To ReceiveAd")
            return
        }
        if (incentivizedInterstitial == null) {
            incentivizedInterstitial = AppLovinIncentivizedInterstitial.create(context)
        }
        rewardedAdListener = RewardedAdListener(TAG, adapter)
        if (RewardedOnDisplay.isOnScreen) rewardedAdListener?.failedToReceiveAd(0)
        else incentivizedInterstitial?.preload(rewardedAdListener)
    }


    override fun show(activity: Activity?) {
        if (activity == null || incentivizedInterstitial == null || incentivizedInterstitial?.isAdReadyToDisplay == false) {
            adapter?.onFailedToDisplay("Error show ad")
            return
        }
        incentivizedInterstitial?.show(
            activity.applicationContext,
            rewardedAdListener,
            rewardedAdListener,
            rewardedAdListener,
            rewardedAdListener
        )
    }

    override fun activityNeededForShow(): Boolean {
        return true
    }

    override fun activityNeededForLoad(): Boolean {
        return false
    }

    override fun readyToShow(): Boolean {
        return incentivizedInterstitial?.isAdReadyToDisplay ?: false
    }

    override fun shouldWaitForAdToDisplay(): Boolean {
        return true
    }

    override fun revenue(): Double? {
        return null
    }

    override fun destroy() {
         incentivizedInterstitial = null
        rewardedAdListener = null
    }

    private class RewardedAdListener(
        private val tag : String,
        private val adapter: BIDFullscreenAdapterProtocol?
    ) : AppLovinAdDisplayListener, AppLovinAdClickListener, AppLovinAdVideoPlaybackListener, AppLovinAdRewardListener, AppLovinAdLoadListener {
        var isGrantedReward = false
        override fun adReceived(p0: AppLovinAd?) {
            BIDLog.d(tag, "Ad received")
            adapter?.onAdLoaded()
        }

        override fun failedToReceiveAd(p0: Int) {
            BIDLog.d(tag, "Error Failed To ReceiveAd : $p0")
            adapter?.onAdFailedToLoadWithError("Error Failed To ReceiveAd : $p0")
        }

        override fun adDisplayed(p0: AppLovinAd?) {
            BIDLog.d(tag, "Ad displayed")
            adapter?.onDisplay()
            RewardedOnDisplay.isOnScreen = true
        }

        override fun adHidden(p0: AppLovinAd?) {
            BIDLog.d(tag, "Ad hidden")
            if (isGrantedReward){
                adapter?.onReward()
                isGrantedReward = false
            }
            adapter?.onHide()
            RewardedOnDisplay.isOnScreen = false
        }

        override fun videoPlaybackBegan(p0: AppLovinAd?) {
            BIDLog.d(tag, "Ad video playback listener")
        }

        override fun videoPlaybackEnded(p0: AppLovinAd?, p1: Double, p2: Boolean) {
            BIDLog.d(tag, "Ad video playback ended")
        }

        override fun userRewardVerified(p0: AppLovinAd?, p1: MutableMap<String, String>?) {
            BIDLog.d(tag, "User reward verified")
            isGrantedReward = true
        }

        override fun userOverQuota(p0: AppLovinAd?, p1: MutableMap<String, String>?) {
            BIDLog.d(tag, "User over quota")
        }

        override fun userRewardRejected(p0: AppLovinAd?, p1: MutableMap<String, String>?) {
            BIDLog.d(tag, "User reward rejected")
        }

        override fun validationRequestFailed(p0: AppLovinAd?, p1: Int) {
            BIDLog.d(tag, "Validation Request Failed : $p1")
            adapter?.onFailedToDisplay("Validation Request Failed : $p1")
        }

        override fun adClicked(p0: AppLovinAd?) {
            BIDLog.d(tag, "Ad clicked")
            adapter?.onClick()
        }
    }

}

object RewardedOnDisplay {
    var isOnScreen = false
}