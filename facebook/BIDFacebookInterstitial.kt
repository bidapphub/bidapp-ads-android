package io.bidapp.networks.facebook

import android.app.Activity
import android.content.Context
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.InterstitialAd
import com.facebook.ads.InterstitialAdListener
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.protocols.BIDFullscreenAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDFullscreenAdapterProtocol

class BIDFacebookInterstitial(
    val adapter: BIDFullscreenAdapterProtocol? = null,
    val adTag: String? = null
) :
    BIDFullscreenAdapterDelegateProtocol {

    val TAG = "Interstitial Facebook"
    private var interstitialAd: InterstitialAd? = null
    private var interstitialAdListener = object : InterstitialAdListener {
        override fun onError(p0: Ad?, p1: AdError?) {
            val error = p1?.errorMessage ?: "Unknown error"
            BIDLog.d(TAG, "onError $adTag exception: $error")
            adapter?.onAdFailedToLoadWithError(error)
        }

        override fun onAdLoaded(p0: Ad?) {
            BIDLog.d(TAG, "ad load $adTag")
            adapter?.onAdLoaded()
        }

        override fun onAdClicked(p0: Ad?) {
            BIDLog.d(TAG, "ad clicked $adTag")
            adapter?.onClick()
        }

        override fun onLoggingImpression(p0: Ad?) {
            BIDLog.d(TAG, "interstitial ad logging impression $adTag")
            adapter?.onDisplay()
        }

        override fun onInterstitialDisplayed(p0: Ad?) {
            BIDLog.d(TAG, "ad displayed $adTag")
        }

        override fun onInterstitialDismissed(p0: Ad?) {
            BIDLog.d(TAG, "ad hide $adTag")
            adapter?.onHide()
        }
    }

    override fun load(context: Any) {
        if (context as? Context == null) {
            adapter?.onAdFailedToLoadWithError("Facebook interstitial loading error")
            return
        }
        if (adTag.isNullOrEmpty()) {
            adapter?.onAdFailedToLoadWithError("Facebook interstitial load is failure. adtag is null or empty")
            return
        }
        if (interstitialAd == null) {
            interstitialAd = InterstitialAd(context, adTag)
        }
        interstitialAd?.loadAd(
            interstitialAd?.buildLoadAdConfig()?.withAdListener(interstitialAdListener)?.build()
        )
    }


    override fun show(activity: Activity?) {
        if (interstitialAd == null || interstitialAd?.isAdLoaded == false || interstitialAd?.isAdInvalidated == true) {
            adapter?.onFailedToDisplay("ad is not ready or invalidated")
            return
        }
        interstitialAd?.show()
    }

    override fun activityNeededForShow(): Boolean {
        return false
    }

    override fun activityNeededForLoad(): Boolean {
        return false
    }

    override fun readyToShow(): Boolean {
        return interstitialAd?.isAdLoaded ?: false
    }

    override fun shouldWaitForAdToDisplay(): Boolean {
        return true
    }

    override fun revenue(): Double? {
        return null
    }

    override fun destroy() {
        interstitialAd?.destroy()
        interstitialAd = null
    }


}