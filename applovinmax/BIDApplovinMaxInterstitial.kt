package io.bidapp.networks.applovinmax

import android.app.Activity
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxInterstitialAd
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.protocols.BIDFullscreenAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDFullscreenAdapterProtocol

@PublishedApi
internal class BIDApplovinMaxInterstitial(
    val adapter: BIDFullscreenAdapterProtocol? = null,
    val adTag: String?
) : BIDFullscreenAdapterDelegateProtocol {

    val TAG = "interstitial Max"
    private var interstitialAd: MaxInterstitialAd? = null
    private var interstitialAdListener: InterstitialAdListener? = null

    private fun setListener() {
        interstitialAdListener = InterstitialAdListener(TAG, adapter, adTag)
        interstitialAd?.setListener(interstitialAdListener)
    }

    override fun load(context: Any) {
        if (context as? Activity == null) {
            adapter?.onAdFailedToLoadWithError("Max interstitial loading error")
            return
        }
        if (adTag.isNullOrEmpty()) {
            adapter?.onAdFailedToLoadWithError("Max interstitial adtag is null or empty")
            return
        }
            if (interstitialAd == null) {
                interstitialAd = MaxInterstitialAd(adTag, context)
            }
            setListener()
            interstitialAd?.loadAd()
    }

    override fun show(activity: Activity?) {
        if (interstitialAd == null || interstitialAd?.isReady == false){
            adapter?.onFailedToDisplay("Max interstitial showing error")
            return
        }
        interstitialAd?.showAd(activity)
    }

    override fun activityNeededForShow(): Boolean {
        return true
    }

    override fun activityNeededForLoad(): Boolean {
        return true
    }

    override fun readyToShow(): Boolean {
      return interstitialAd?.isReady ?: false
    }

    override fun shouldWaitForAdToDisplay(): Boolean {
       return true
    }

    override fun revenue(): Double? {
        if (interstitialAdListener?.ad != null) {
            return interstitialAdListener?.ad?.revenue
        }
        return null
    }

    override fun destroy() {
        interstitialAd?.setListener(null)
        interstitialAd?.destroy()
        interstitialAd = null
        interstitialAdListener = null
    }

    private class InterstitialAdListener(
        private val tag : String,
        private val adapter: BIDFullscreenAdapterProtocol?,
        private val adTag: String?
    ) : MaxAdListener  {
        var ad:MaxAd? = null

        override fun onAdLoaded(maxAd: MaxAd) {
            BIDLog.d(tag, "Ad loaded. adtag: ($adTag)")
            ad = maxAd
            adapter?.onAdLoaded()
        }

        override fun onAdDisplayed(maxAd: MaxAd) {
            BIDLog.d(tag, "Ad displayed. adtag: ($adTag)")
            adapter?.onDisplay()
        }

        override fun onAdHidden(maxAd: MaxAd) {
            BIDLog.d(tag, "Ad hidden. adtag: ($adTag)")
            adapter?.onHide()
        }

        override fun onAdClicked(maxAd: MaxAd) {
            BIDLog.d(tag, "Ad Clicked. adtag: ($adTag)")
            adapter?.onClick()
        }

        override fun onAdLoadFailed(p0: String, p1: MaxError) {
            val errorDescription = p1.toString()
            BIDLog.d(tag, "Ad load failed error $errorDescription adtag: ($adTag)")
            adapter?.onAdFailedToLoadWithError(errorDescription)
        }

        override fun onAdDisplayFailed(maxAd: MaxAd, p1: MaxError) {
            val errorDescription = p1.message.toString()
            BIDLog.d(tag, "Ad display failed error $errorDescription adtag: ($adTag)" )
            adapter?.onFailedToDisplay(p1.message.toString())
        }

    }

}