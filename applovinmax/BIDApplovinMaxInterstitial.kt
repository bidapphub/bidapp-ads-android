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
    private var ad:MaxAd? = null
    private val interstitialListener = object : MaxAdListener {
        override fun onAdLoaded(maxAd: MaxAd) {
            BIDLog.d(TAG, "onAdLoaded. adtag: ($adTag)")
            ad = maxAd
            adapter?.onAdLoaded()
        }

        override fun onAdDisplayed(maxAd: MaxAd) {
            BIDLog.d(TAG, "onAdDisplayed. adtag: ($adTag)")
            adapter?.onDisplay()
        }

        override fun onAdHidden(maxAd: MaxAd) {
            BIDLog.d(TAG, "onAdHidden. adtag: ($adTag)")
            adapter?.onHide()
        }

        override fun onAdClicked(maxAd: MaxAd) {
            BIDLog.d(TAG, "onAdClicked. adtag: ($adTag)")
            adapter?.onClick()
        }

        override fun onAdLoadFailed(p0: String, p1: MaxError) {
            val errorDescription = p1.toString()
            BIDLog.d(TAG, "onAdLoadFailed error $errorDescription adtag: ($adTag)")
            adapter?.onAdFailedToLoadWithError(errorDescription)
        }

        override fun onAdDisplayFailed(maxAd: MaxAd, p1: MaxError) {
            val errorDescription = p1.message.toString()
            BIDLog.d(TAG, "onAdDisplayFailed error $errorDescription adtag: ($adTag)" )
            adapter?.onFailedToDisplay(p1.message.toString())
        }
    }

    private fun setListener() {
        interstitialAd?.setListener(interstitialListener)
    }

    override fun load(context: Any) {
        if (context as? Activity == null) {
            adapter?.onAdFailedToLoadWithError("Max interstitial loading error")
            return
        }
        if (adTag == null) {
            adapter?.onAdFailedToLoadWithError("Max interstitial adtag is null")
            return
        }
            if (interstitialAd == null) {
                interstitialAd = MaxInterstitialAd(adTag, BIDApplovinMaxSDK.appLovinGetMaxInstanceSDK((context as Activity).applicationContext), context)
            }
            setListener()
            interstitialAd?.loadAd()
    }

    override fun show(activity: Activity?) {
        if (interstitialAd == null || interstitialAd?.isReady == false){
            adapter?.onFailedToDisplay("Max interstitial showing error")
            return
        }
        interstitialAd?.showAd()
    }

    override fun activityNeededForShow(): Boolean {
        return false
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
        if (ad != null) {
            return ad?.revenue
        }
        return null
    }

    override fun destroy() {
        interstitialAd?.setListener(null)
        interstitialAd?.destroy()
        interstitialAd = null
        ad = null
    }
}