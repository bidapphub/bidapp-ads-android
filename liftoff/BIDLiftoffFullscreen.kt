package io.bidapp.networks.liftoff


import android.app.Activity
import android.content.Context
import android.util.Log
import com.vungle.ads.AdConfig
import com.vungle.ads.BaseAd
import com.vungle.ads.BaseFullscreenAd
import com.vungle.ads.InterstitialAd
import com.vungle.ads.RewardedAd
import com.vungle.ads.RewardedAdListener
import com.vungle.ads.VungleError
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.protocols.BIDFullscreenAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDFullscreenAdapterProtocol
import java.lang.ref.WeakReference


@PublishedApi
internal class BIDLiftoffFullscreen(
    val adapter: BIDFullscreenAdapterProtocol? = null,
    val adTag: String?,
    val isRewarded: Boolean
) : BIDFullscreenAdapterDelegateProtocol {


    val TAG = if (isRewarded) "Reward Liftoff" else "Full Liftoff"
    var ads: BaseFullscreenAd? = null
    var isRewardGranted = false
    val callBack = object : RewardedAdListener {
        override fun onAdClicked(baseAd: BaseAd) {
            BIDLog.d(TAG, "on ad click. ${baseAd.placementId}")
            adapter?.onClick()
        }

        override fun onAdEnd(baseAd: BaseAd) {
            BIDLog.d(TAG, "on ad end. ${baseAd.placementId}")
            if (isRewarded) {
                if (isRewardGranted) {
                    adapter?.onReward()
                    isRewardGranted = false
                }
            }
            adapter?.onHide()
        }

        override fun onAdFailedToLoad(baseAd: BaseAd, adError: VungleError) {
            BIDLog.d(TAG, "onError ${baseAd.placementId} exception: ${adError.message}")
            adapter?.onAdFailedToLoadWithError(adError.message.toString())
        }

        override fun onAdFailedToPlay(baseAd: BaseAd, adError: VungleError) {
            BIDLog.d(TAG, "onError ${baseAd.placementId} exception: ${adError.message}")
            adapter?.onFailedToDisplay(adError.errorMessage)
        }

        override fun onAdImpression(baseAd: BaseAd) {
            BIDLog.d(TAG, "on ad impression ${baseAd.placementId}")
            adapter?.onDisplay()
        }

        override fun onAdLeftApplication(baseAd: BaseAd) {
            BIDLog.d(TAG, "on ad left application ${baseAd.placementId}")
        }

        override fun onAdLoaded(baseAd: BaseAd) {
            BIDLog.d(TAG, "on ad load ${baseAd.placementId}")
            adapter?.onAdLoaded()
        }

        override fun onAdRewarded(baseAd: BaseAd) {
            BIDLog.d(TAG, "on ad rewarded ${baseAd.placementId}")
            if(isRewarded) isRewardGranted = true
        }

        override fun onAdStart(baseAd: BaseAd) {
            BIDLog.d(TAG, "on ad viewed ${baseAd.placementId}")
        }

    }

    override fun load(context: Any) {
        if (context as? Context == null) {
            adapter?.onAdFailedToLoadWithError("Liftoff fullscreen loading error")
            return
        }
        if (adTag == null) {
            adapter?.onAdFailedToLoadWithError("Liftoff fullscreen adtag is null")
            return
        }
        if (isRewarded)
            ads =
                RewardedAd(context, adTag, AdConfig()).apply {
                    adListener = callBack
                    load()
                }
        else
            ads = InterstitialAd(
                    context,
                    adTag,
                    AdConfig()).apply {
                    adListener = callBack
                    load()
                }
    }

    override fun show(activity: Activity?) {
        Log.d("mylog", "show")
        if (ads == null || ads?.canPlayAd() == false) {
            adapter?.onFailedToDisplay("Failed to display")
            return
        }
        (ads as? BaseFullscreenAd)?.play()
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
        ads = null
    }
}