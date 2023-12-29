package io.bidapp.networks.admob

import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.protocols.BIDFullscreenAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDFullscreenAdapterProtocol

@PublishedApi
internal class BIDAdmobInterstitial(
    val adapter: BIDFullscreenAdapterProtocol,
    val adTag: String? = null
) : BIDFullscreenAdapterDelegateProtocol {
    val TAG = "interstitial Admob"
    private var interstitialAd: InterstitialAd? = null

    val fullScreenContentCallback: FullScreenContentCallback =
        object : FullScreenContentCallback() {

            override fun onAdClicked() {
                BIDLog.d(TAG, "ad clicked. adtag: ($adTag)")
                adapter.onClick()
            }

            override fun onAdDismissedFullScreenContent() {
                adapter.onHide()
                BIDLog.d(TAG, "ad hidden. adtag: ($adTag)")
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                BIDLog.d(TAG, "ad failed to display. adtag: ($adTag)")
                adapter.onFailedToDisplay(p0.message)
            }

            override fun onAdImpression() {
                adapter.onDisplay()
                BIDLog.d(TAG, "ad display. adtag: ($adTag)")
            }

            override fun onAdShowedFullScreenContent() {
                BIDLog.d(TAG, "ad on showed fullscreen content. adtag: ($adTag)")
            }
        }

    override fun load(context: Any) {
        if (context as? Context == null) {
            adapter.onAdFailedToLoadWithError("Admob interstitial loading error")
            return
        }
        if (adTag == null) {
            adapter.onAdFailedToLoadWithError("Admob interstitial adtag is null")
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
        InterstitialAd.load(
            context,
            adTag,
            request,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    BIDLog.d(TAG, "failed to receive ad error ${p0.message} adtag: ($adTag)")
                    adapter.onAdFailedToLoadWithError(p0.message)
                }

                override fun onAdLoaded(p0: InterstitialAd) {
                    interstitialAd = p0
                    interstitialAd?.fullScreenContentCallback =
                        this@BIDAdmobInterstitial.fullScreenContentCallback
                    BIDLog.d(TAG, "ad loaded. adtag: ($adTag)")
                    adapter.onAdLoaded()
                }
            })
    }

    override fun show(activity: Activity?) {
        if (activity == null || interstitialAd == null){
            adapter.onFailedToDisplay("Error Admob showing interstitial. adtag: ($adTag)")
            return
        }
       interstitialAd?.show(activity)
    }

    override fun activityNeededForShow(): Boolean {
        return true
    }

    override fun activityNeededForLoad(): Boolean {
        return false
    }

    override fun readyToShow(): Boolean {
        return interstitialAd != null
    }

    override fun shouldWaitForAdToDisplay(): Boolean {
        return true
    }

    override fun revenue(): Double? {
        return null
    }

    override fun destroy() {
        interstitialAd?.fullScreenContentCallback = null
        interstitialAd = null
    }
}