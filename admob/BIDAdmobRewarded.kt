package io.bidapp.networks.admob

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.protocols.BIDFullscreenAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDFullscreenAdapterProtocol

@PublishedApi
internal class BIDAdmobRewarded(
    val adapter: BIDFullscreenAdapterProtocol? = null,
    val adTag: String? = null,
    var isReward: Boolean
) : BIDFullscreenAdapterDelegateProtocol {
    val TAG = "Rewarded Admob"
    private var loadedAd: RewardedAd? = null


    val fullScreenContentCallback: FullScreenContentCallback =
        object : FullScreenContentCallback() {
            override fun onAdClicked() {
                super.onAdClicked()
                BIDLog.d(TAG, "ad clicked")
                adapter?.onClick()
            }

            override fun onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent()
                adapter?.onHide()
                BIDLog.d(TAG, "ad hidden")
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                super.onAdFailedToShowFullScreenContent(p0)
                BIDLog.d(TAG, "failed to display")
                adapter?.onFailedToDisplay(p0.message)
            }

            override fun onAdImpression() {
                super.onAdImpression()
                BIDLog.d(TAG, "on ad impression")
            }

            override fun onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent()
                adapter?.onDisplay()
                BIDLog.d(TAG, "on display")
            }
        }

    override fun load(activity: Activity) {
        val networkExtrasBundle = Bundle()
        var request = AdRequest.Builder().build()
        if (BIDAdmobSDK.getGDPR() != null) {
            if (BIDAdmobSDK.getGDPR() == true) {
                networkExtrasBundle.putInt("npa", 1)
                request = AdRequest.Builder()
                    .addNetworkExtrasBundle(AdMobAdapter::class.java, networkExtrasBundle)
                    .build()
            }
        }
        if (adTag != null) {
            RewardedAd.load(
                activity.applicationContext,
                adTag,
                request,
                object : RewardedAdLoadCallback() {
                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        super.onAdFailedToLoad(p0)
                        BIDLog.d(TAG, "Failed To Receive Ad error ${p0.message}")
                        adapter?.onAdFailedToLoadWithError(p0.message)
                    }

                    override fun onAdLoaded(p0: RewardedAd) {
                        super.onAdLoaded(p0)
                        loadedAd = p0
                        loadedAd?.fullScreenContentCallback =
                            this@BIDAdmobRewarded.fullScreenContentCallback
                        BIDLog.d(TAG, "loaded Ad")
                        adapter?.onAdLoaded()
                    }
                })
        }
    }

    override fun show(activity: Activity?) {
        val showing = runCatching {
            loadedAd?.show(
                activity!!
            ) { adapter?.onReward() } ?: { BIDLog.d(TAG, "The rewarded ad wasn't ready yet.") }
        }
        if (showing.isFailure) adapter?.onFailedToDisplay("Error Admob showing interstitial")
    }

    override fun activityNeededForShow(): Boolean {
        return false
    }

    override fun readyToShow(): Boolean {
        return loadedAd != null
    }

    override fun shouldWaitForAdToDisplay(): Boolean {
        return true
    }
}