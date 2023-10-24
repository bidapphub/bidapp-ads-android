package io.bidapp.networks.applovinmax

import android.app.Activity
import android.util.Log
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
    private val interstitialListener = object : MaxAdListener {
        override fun onAdLoaded(p0: MaxAd?) {
            BIDLog.d(TAG, "onAdLoaded")
            adapter?.onAdLoaded()
        }

        override fun onAdDisplayed(p0: MaxAd?) {
            BIDLog.d(TAG, "onAdDisplayed")
            adapter?.onDisplay()
        }

        override fun onAdHidden(p0: MaxAd?) {
            BIDLog.d(TAG, "onAdHidden")
            adapter?.onHide()
        }

        override fun onAdClicked(p0: MaxAd?) {
            BIDLog.d(TAG, "onAdClicked")
            adapter?.onClick()
        }

        override fun onAdLoadFailed(p0: String?, p1: MaxError?) {
            val errorDescription = p1.toString()
            BIDLog.d(TAG, "onAdLoadFailed error $errorDescription")
            adapter?.onAdFailedToLoadWithError(errorDescription)
        }

        override fun onAdDisplayFailed(p0: MaxAd?, p1: MaxError?) {
            val errorDescription = p1.toString()
            BIDLog.d(TAG, "onAdDisplayFailed error $errorDescription")
            adapter?.onFailedToDisplay(p1.toString())
        }
    }

    fun init() {
        interstitialAd?.setListener(interstitialListener)
    }

    override fun load(activity: Activity) {
        BIDLog.d(TAG, "Max interstitial load")
        val load = runCatching {
            if (interstitialAd == null) {
                interstitialAd = MaxInterstitialAd(adTag, activity)
                init()
            }
            interstitialAd?.loadAd()
        }
        if(load.isFailure) adapter?.onAdFailedToLoadWithError("Max interstitial loading error")
    }

    override fun show(activity: Activity?) {
        BIDLog.d(TAG, "Max interstitial show")
        if (interstitialAd?.isReady == true) {
            interstitialAd!!.showAd()
        }
    }

    override fun activityNeededForShow(): Boolean {
        return false
    }

    override fun readyToShow(): Boolean {
      return interstitialAd?.isReady ?: false
    }

    override fun shouldWaitForAdToDisplay(): Boolean {
       return true
    }
}