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
internal class BIDAdmobInterstitial(val adapter: BIDFullscreenAdapterProtocol? = null, val adTag: String? = null) : BIDFullscreenAdapterDelegateProtocol {
    val TAG = "interstitial Admob"
    private var loadedAd: InterstitialAd? = null

    val fullScreenContentCallback: FullScreenContentCallback = object: FullScreenContentCallback(){

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

    override fun load(context: Any) {
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
                InterstitialAd.load(context as Context, adTag, request, object : InterstitialAdLoadCallback(){
                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        super.onAdFailedToLoad(p0)
                        BIDLog.d(TAG, "Failed To Receive Ad error ${p0.message}")
                        adapter?.onAdFailedToLoadWithError(p0.message)
                    }

                    override fun onAdLoaded(p0: InterstitialAd) {
                        super.onAdLoaded(p0)
                        loadedAd = p0
                        loadedAd?.fullScreenContentCallback = this@BIDAdmobInterstitial.fullScreenContentCallback
                        BIDLog.d(TAG, "loaded Ad")
                        adapter?.onAdLoaded()
                    }
                })
            }
    }

    override fun show(activity: Activity?) {
        val showing = runCatching {  loadedAd?.show(activity!!)  }
        if (showing.isFailure) adapter?.onFailedToDisplay("Error Admob showing interstitial")
    }

    override fun activityNeededForShow(): Boolean {
        return true
    }

    override fun activityNeededForLoad(): Boolean {
        return false
    }

    override fun readyToShow(): Boolean {
        return loadedAd != null
    }

    override fun shouldWaitForAdToDisplay(): Boolean {
        return true
    }
}