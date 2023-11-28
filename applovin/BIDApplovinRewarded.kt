package io.bidapp.networks.applovin

import android.app.Activity
import android.content.Context
import android.util.Log
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


    private val appLovinAdLoadListener = object : AppLovinAdLoadListener {
        override fun adReceived(p0: AppLovinAd?) {
            BIDLog.d(TAG, "ad received")
            adapter?.onAdLoaded()
        }

        override fun failedToReceiveAd(p0: Int) {
            BIDLog.d(TAG, "Error Failed To ReceiveAd : $p0")
            adapter?.onAdFailedToLoadWithError("Error Failed To ReceiveAd : $p0")
        }
    }

    private val appLovinAdDisplayListener = object : AppLovinAdDisplayListener {
        override fun adDisplayed(p0: AppLovinAd?) {
            BIDLog.d(TAG, "adDisplayed")
            adapter?.onDisplay()
            RewardedOnDisplay.isOnScreen = true
        }

        override fun adHidden(p0: AppLovinAd?) {
            BIDLog.d(TAG, "adHidden")
            adapter?.onHide()
            RewardedOnDisplay.isOnScreen = false
        }
    }

    private val appLovinAdClickListener = AppLovinAdClickListener {
        BIDLog.d(TAG, "onAdClicked")
        adapter?.onClick()
    }

    private val appLovinAdVideoPlaybackListener = object : AppLovinAdVideoPlaybackListener {
        override fun videoPlaybackBegan(p0: AppLovinAd?) {
        BIDLog.d(TAG, "ad video playback listener")
        }

        override fun videoPlaybackEnded(p0: AppLovinAd?, p1: Double, p2: Boolean) {
        BIDLog.d(TAG, "ad video playback ended")
        }
    }

    private val appLovinAdRewardListener = object : AppLovinAdRewardListener {
        override fun userRewardVerified(p0: AppLovinAd?, p1: MutableMap<String, String>?) {
         BIDLog.d(TAG, "userRewardVerified")
         adapter?.onReward()
        }

        override fun userOverQuota(p0: AppLovinAd?, p1: MutableMap<String, String>?) {
        BIDLog.d(TAG, "user over quota")
        }

        override fun userRewardRejected(p0: AppLovinAd?, p1: MutableMap<String, String>?) {
        BIDLog.d(TAG, "user reward rejected")
        }

        override fun validationRequestFailed(p0: AppLovinAd?, p1: Int) {
        BIDLog.d(TAG, "Validation Request Failed : $p1")
        adapter?.onFailedToDisplay("Validation Request Failed : $p1")
        }
    }


    override fun load(context: Any) {
        val load = runCatching {
            if (incentivizedInterstitial == null) {
                incentivizedInterstitial = AppLovinIncentivizedInterstitial.create(context as Context)
            }
            if (RewardedOnDisplay.isOnScreen) appLovinAdLoadListener.failedToReceiveAd(0)
            else incentivizedInterstitial?.preload(appLovinAdLoadListener)
        }
        if (load.isFailure) adapter?.onAdFailedToLoadWithError("Error Failed To ReceiveAd")
    }

    override fun show(activity: Activity?) {
        val show = runCatching {
            if (incentivizedInterstitial == null) {
                incentivizedInterstitial = AppLovinIncentivizedInterstitial.create(activity?.applicationContext!!)
            }
            if (incentivizedInterstitial?.isAdReadyToDisplay == true)
                incentivizedInterstitial!!.show(
                    activity!!,
                    appLovinAdRewardListener,
                    appLovinAdVideoPlaybackListener,
                    appLovinAdDisplayListener,
                    appLovinAdClickListener
                )
        }
        if (show.isFailure) adapter?.onFailedToDisplay("Error show ad")
    }

    override fun activityNeededForShow(): Boolean {
        return true
    }

    override fun activityNeededForLoad(): Boolean {
        return false
    }

    override fun readyToShow(): Boolean {
        return true
    }

    override fun shouldWaitForAdToDisplay(): Boolean {
        return true
    }
}

object RewardedOnDisplay {
    var isOnScreen = false
}