package io.bidapp.networks.admob

import android.app.Activity
import android.content.Context
import android.os.Bundle
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
    val adapter: BIDFullscreenAdapterProtocol,
    val adTag: String?,
    val isRewarded: Boolean
) : BIDFullscreenAdapterDelegateProtocol {
    val TAG = "Rewarded Admob"
    private var rewardedAd: RewardedAd? = null
    private var isRewardGranted = false


    val fullScreenContentCallback: FullScreenContentCallback =
        object : FullScreenContentCallback() {
            override fun onAdClicked() {
                BIDLog.d(TAG, "ad clicked. adtag: ($adTag)")
                adapter.onClick()
            }

            override fun onAdDismissedFullScreenContent() {
                if (isRewardGranted){
                    adapter.onReward()
                    isRewardGranted = false
                }
                adapter.onHide()
                BIDLog.d(TAG, "ad hidden. adtag: ($adTag)")
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                BIDLog.d(TAG, "ad failed to display. adtag: ($adTag)")
                adapter.onFailedToDisplay(p0.message)
            }

            override fun onAdImpression() {
                adapter.onDisplay()
                BIDLog.d(TAG, "ad show. adtag: ($adTag)")
            }

            override fun onAdShowedFullScreenContent() {
                BIDLog.d(TAG, "ad on display. adtag: ($adTag)")
            }
        }

    override fun load(context: Any) {
        if (context as? Context == null) {
            adapter.onAdFailedToLoadWithError("Admob rewarded loading error")
            return
        }
        if (adTag == null) {
            adapter.onAdFailedToLoadWithError("Admob rewarded adtag is null")
            return
        }
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
            RewardedAd.load(
                context,
                adTag,
                request,
                object : RewardedAdLoadCallback() {
                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        rewardedAd = null
                        BIDLog.d(TAG, "Failed To Receive Ad error ${p0.message} adtag: ($adTag)")
                        adapter.onAdFailedToLoadWithError(p0.message)
                    }

                    override fun onAdLoaded(p0: RewardedAd) {
                        rewardedAd = p0
                        rewardedAd?.fullScreenContentCallback =
                            this@BIDAdmobRewarded.fullScreenContentCallback
                        BIDLog.d(TAG, "ad loaded. adtag: ($adTag)")
                        adapter.onAdLoaded()
                    }
                })
    }

    override fun show(activity: Activity?) {
        if (activity == null || rewardedAd == null){
            adapter.onFailedToDisplay("Error Admob showing rewarded. adtag: ($adTag)")
            return
        }
            rewardedAd?.show(activity) {
                isRewardGranted = true
            } ?: {
                BIDLog.d(TAG, "The rewarded ad wasn't ready yet.")
            }
    }

    override fun activityNeededForShow(): Boolean {
        return true
    }

    override fun activityNeededForLoad(): Boolean {
        return false
    }

    override fun readyToShow(): Boolean {
        return rewardedAd != null
    }

    override fun shouldWaitForAdToDisplay(): Boolean {
        return true
    }

    override fun revenue(): Double? {
        return null
    }

    override fun destroy() {
        rewardedAd?.fullScreenContentCallback = null
        rewardedAd = null
    }
}