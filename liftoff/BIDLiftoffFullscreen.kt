package io.bidapp.networks.liftoff


import android.app.Activity
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
    var ads: WeakReference<BaseFullscreenAd>? = null
    val callBack = object : RewardedAdListener {
        override fun onAdClicked(baseAd: BaseAd) {
            BIDLog.d(TAG, "on ad click")
            adapter?.onClick()
        }

        override fun onAdEnd(baseAd: BaseAd) {
            BIDLog.d(TAG, "on ad end")
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
            BIDLog.d(TAG, "on ad impression")
        }

        override fun onAdLeftApplication(baseAd: BaseAd) {
            BIDLog.d(TAG, "on ad left application ${baseAd.placementId}")
            adapter?.onHide()
        }

        override fun onAdLoaded(baseAd: BaseAd) {
            BIDLog.d(TAG, "on dd load ${baseAd.placementId}")
            adapter?.onAdLoaded()
        }

        override fun onAdRewarded(baseAd: BaseAd) {
            BIDLog.d(TAG, "on ad rewarded ${baseAd.placementId}")
            adapter?.onReward()
        }

        override fun onAdStart(baseAd: BaseAd) {
            BIDLog.d(TAG, "on ad viewed ${baseAd.placementId}")
            adapter?.onDisplay()
        }

    }


    override fun load(activity: Activity) {
        val load = runCatching {
            if (isRewarded)
                adTag?.let {
                    ads =
                        WeakReference(RewardedAd(activity.applicationContext, it, AdConfig().apply {
                        }).apply {
                            adListener = callBack
                            load()
                        })
                }
            else
                adTag?.let {
                    ads = WeakReference(
                        InterstitialAd(
                            activity.applicationContext,
                            it,
                            AdConfig().apply {
                            }).apply {
                            adListener = callBack
                            load()
                        })
                }
        }
        if (load.isFailure) adapter?.onAdFailedToLoadWithError("Liftoff fullscreen loading error")
    }

    override fun show(activity: Activity?) {
        if ((ads?.get() as BaseFullscreenAd).canPlayAd()) {
            (ads?.get() as BaseFullscreenAd).play()
        }
    }

    override fun activityNeededForShow(): Boolean {
        return false
    }

    override fun readyToShow(): Boolean {
        return ads?.get()?.canPlayAd() ?: false
    }

    override fun shouldWaitForAdToDisplay(): Boolean {
        return true
    }
}