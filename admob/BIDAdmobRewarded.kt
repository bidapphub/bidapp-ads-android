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
    private val TAG = "Rewarded Admob"
    private var rewardedAd: RewardedAd? = null
    private var rewardedAdListener: RewardedAdListener? = null

    override fun load(context: Any) {
        if (context as? Context == null) {
            adapter.onAdFailedToLoadWithError("Admob rewarded loading error")
            return
        }
        if (adTag.isNullOrEmpty()) {
            adapter.onAdFailedToLoadWithError("Admob rewarded adTag is null or empty")
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
                        rewardedAdListener = RewardedAdListener(TAG, adapter, adTag)
                        rewardedAd?.fullScreenContentCallback = rewardedAdListener
                        BIDLog.d(TAG, "Ad loaded. adtag: ($adTag)")
                        adapter.onAdLoaded()
                    }
                })
    }

    override fun show(activity: Activity?) {
        if (activity == null || rewardedAd == null){
            adapter.onFailedToDisplay("Error showing rewarded. adtag: ($adTag)")
            return
        }
            rewardedAd?.show(activity) {
                rewardedAdListener?.isRewardGranted = true
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
        rewardedAdListener = null
    }

    private class RewardedAdListener(
        private val tag : String,
        private val adapter: BIDFullscreenAdapterProtocol?,
        private val adTag: String?
    ) : FullScreenContentCallback() {
        var isRewardGranted = false
        override fun onAdClicked() {
            BIDLog.d(tag, "Ad clicked. adtag: ($adTag)")
            adapter?.onClick()
        }


        override fun onAdDismissedFullScreenContent() {
            if (isRewardGranted){
                adapter?.onReward()
                isRewardGranted = false
            }
            adapter?.onHide()
            BIDLog.d(tag, "Ad hidden. adtag: ($adTag)")
        }

        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
            BIDLog.d(tag, "Ad failed to display. adtag: ($adTag)")
            adapter?.onFailedToDisplay(p0.message)
        }

        override fun onAdImpression() {
            adapter?.onDisplay()
            BIDLog.d(tag, "Ad show. adtag: ($adTag)")
        }

        override fun onAdShowedFullScreenContent() {
            BIDLog.d(tag, "Ad on display. adtag: ($adTag)")
        }

    }
}