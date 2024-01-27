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
    var rewardedAd : RewardedAd? = null
    var isRewardGranted = false


    private var loadCallback : RewardedAdLoadListener? = object : RewardedAdLoadListener {
        override fun onAdLoaded(ad: RewardedAd) {
            rewardedAd = ad
            BIDLog.d(TAG, "on ad load $adUnitId")
            adapter?.onAdLoaded()
        }

        override fun onAdFailedToLoad(p0: AdRequestError) {
            rewardedAd = null
            BIDLog.d(TAG, "onError $adUnitId exception: ${p0.description}")
            adapter?.onAdFailedToLoadWithError(p0.description)
        }

    }

    private val showCallback = object : RewardedAdEventListener {
        override fun onAdShown() {
            BIDLog.d(TAG, "on ad shown. $adUnitId")
            if (BIDYandexSDK.debugOrTesting != null && BIDYandexSDK.debugOrTesting == true){
                adapter?.onDisplay()
            }
        }

        override fun onAdFailedToShow(p0: AdError) {
            BIDLog.d(TAG, "onError $adUnitId exception: ${p0.description}")
            adapter?.onFailedToDisplay(p0.description)
        }

        override fun onAdDismissed() {
            BIDLog.d(TAG, "on ad dismiss. $adUnitId")
            if (isRewardGranted) {
                adapter?.onReward()
                isRewardGranted = false
            }
            adapter?.onHide()
        }

        override fun onAdClicked() {
            BIDLog.d(TAG, "on ad click. $adUnitId")
            adapter?.onClick()
        }

        override fun onAdImpression(p0: ImpressionData?) {
            BIDLog.d(TAG, "on ad impression. $adUnitId")
            adapter?.onDisplay()
        }

        override fun onRewarded(p0: Reward) {
            BIDLog.d(TAG, "on ad rewarded $adUnitId")
            isRewardGranted = true
        }

    }


    override fun load(context: Any) {
        if (context as? Activity == null) {
            adapter?.onAdFailedToLoadWithError("Yandex fullscreen loading error")
            return
        }
        if (adUnitId == null) {
            adapter?.onAdFailedToLoadWithError("Yandex fullscreen adUnitId is null or incorrect format")
            return
        }
        rewardedAd = null
        val loadRewardedAdRunnable = Runnable {
            if (rewardedAdLoader == null) {
                rewardedAdLoader = RewardedAdLoader(context.applicationContext)
            }
            rewardedAdLoader?.setAdLoadListener(loadCallback)
            val adRequestConfiguration = AdRequestConfiguration.Builder(adUnitId).build()
            rewardedAdLoader?.loadAd(adRequestConfiguration)
        }
        (context as Activity).runOnUiThread(loadRewardedAdRunnable)
    }

    override fun show(activity: Activity?) {
        if (rewardedAd == null || activity == null){
            adapter?.onAdFailedToLoadWithError("Yandex interstitial ad failed to load. $adUnitId")
            return
        }
        rewardedAd?.setAdEventListener(showCallback)
        rewardedAd?.show(activity)
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
        if ( rewardedAd != null )
        {
            rewardedAd?.setAdEventListener(null)
            loadCallback = null
            rewardedAd = null
        }
    }

    override fun revenue(): Double? {
        return null
    }
}