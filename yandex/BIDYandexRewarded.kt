package io.bidapp.networks.yandex

import android.app.Activity
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequestConfiguration
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.rewarded.Reward
import com.yandex.mobile.ads.rewarded.RewardedAd
import com.yandex.mobile.ads.rewarded.RewardedAdEventListener
import com.yandex.mobile.ads.rewarded.RewardedAdLoadListener
import com.yandex.mobile.ads.rewarded.RewardedAdLoader
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.protocols.BIDFullscreenAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDFullscreenAdapterProtocol

class BIDYandexRewarded(
    val adapter: BIDFullscreenAdapterProtocol? = null,
    val adUnitId: String? = null,
    isReward: Boolean
) : BIDFullscreenAdapterDelegateProtocol {
    val TAG = "Rewarded Yandex"
    private var rewardedAdLoader : RewardedAdLoader? = null
    private var rewardedAdListener: RewardedAdListener? = null



    override fun load(context: Any) {
        if (context as? Activity == null) {
            adapter?.onAdFailedToLoadWithError("Yandex fullscreen loading error")
            return
        }
        if (adUnitId.isNullOrEmpty()) {
            adapter?.onAdFailedToLoadWithError("Yandex fullscreen adUnitId is null or incorrect format")
            return
        }
        rewardedAdListener = RewardedAdListener(TAG, adapter, adUnitId)
        val loadRewardedAdRunnable = Runnable {
            if (rewardedAdLoader == null) {
                rewardedAdLoader = RewardedAdLoader(context.applicationContext)
            }
            rewardedAdLoader?.setAdLoadListener(rewardedAdListener)
            val adRequestConfiguration = AdRequestConfiguration.Builder(adUnitId).build()
            rewardedAdLoader?.loadAd(adRequestConfiguration)
        }
        (context).runOnUiThread(loadRewardedAdRunnable)
    }

    override fun show(activity: Activity?) {
        if (rewardedAdListener?.rewardedAd == null || activity == null){
            adapter?.onAdFailedToLoadWithError("Yandex interstitial ad failed to load. $adUnitId")
            return
        }
        rewardedAdListener?.rewardedAd?.setAdEventListener(rewardedAdListener)
        rewardedAdListener?.rewardedAd?.show(activity)
    }

    override fun activityNeededForShow(): Boolean {
        return true
    }

    override fun activityNeededForLoad(): Boolean {
        return true
    }

    override fun readyToShow(): Boolean {
        return rewardedAdLoader != null
    }

    override fun shouldWaitForAdToDisplay(): Boolean {
        return true
    }

    override fun destroy() {
        rewardedAdListener?.rewardedAd?.setAdEventListener(null)
        rewardedAdListener = null
    }

    override fun revenue(): Double? {
        return null
    }

    private class RewardedAdListener (
        private val tag : String,
        private val adapter: BIDFullscreenAdapterProtocol?,
        private val adUnitId: String?
    ) : RewardedAdLoadListener, RewardedAdEventListener {
        var rewardedAd : RewardedAd? = null
        var isRewardGranted = false

        override fun onAdShown() {
            BIDLog.d(tag, "Ad shown. $adUnitId")
            if (BIDYandexSDK.debugOrTesting != null && BIDYandexSDK.debugOrTesting == true){
                adapter?.onDisplay()
            }
        }

        override fun onAdFailedToShow(adError: AdError) {
            BIDLog.d(tag, "Error $adUnitId exception: ${adError.description}")
            adapter?.onFailedToDisplay(adError.description)
        }

        override fun onAdDismissed() {
            BIDLog.d(tag, "Ad dismiss. $adUnitId")
            if (isRewardGranted) {
                adapter?.onReward()
                isRewardGranted = false
            }
            adapter?.onHide()
        }

        override fun onAdClicked() {
            BIDLog.d(tag, "Ad click. $adUnitId")
            adapter?.onClick()
        }

        override fun onAdImpression(impressionData: ImpressionData?) {
            BIDLog.d(tag, "Ad impression. $adUnitId")
            adapter?.onDisplay()
        }

        override fun onRewarded(reward: Reward) {
            BIDLog.d(tag, "Ad rewarded $adUnitId")
            isRewardGranted = true
        }

        override fun onAdLoaded(rewarded: RewardedAd) {
            rewardedAd = rewarded
            BIDLog.d(tag, "Ad load $adUnitId")
            adapter?.onAdLoaded()
        }

        override fun onAdFailedToLoad(error: AdRequestError) {
            rewardedAd = null
            BIDLog.d(tag, "Error $adUnitId exception: ${error.description}")
            adapter?.onAdFailedToLoadWithError(error.description)
        }


    }
}