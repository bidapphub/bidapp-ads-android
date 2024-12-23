package io.bidapp.networks.applovin

import android.app.Activity
import android.content.Context
import com.applovin.adview.AppLovinInterstitialAd
import com.applovin.adview.AppLovinInterstitialAdDialog
import com.applovin.sdk.*
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.protocols.BIDFullscreenAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDFullscreenAdapterProtocol

@PublishedApi
internal class BIDApplovinInterstitial(
    val adapter: BIDFullscreenAdapterProtocol? = null,
    val adTag: String? = null
) : BIDFullscreenAdapterDelegateProtocol {

    val TAG = "interstitial Applovin"
    private var currentAd: AppLovinAd? = null
    private var interstitialAdDialog: AppLovinInterstitialAdDialog? = null
    private var interstitialAdListener: InterstitialAdListener? = null
    private var interstitialAdLoadListener: AppLovinAdLoadListener? = null


    override fun load(context: Any) {
        if (context as? Context == null) {
            adapter?.onAdFailedToLoadWithError("Error Failed To ReceiveAd")
            return
        }
        if (interstitialAdDialog == null) {
            interstitialAdDialog =
                AppLovinInterstitialAd.create(AppLovinSdk.getInstance(context), context)
        }
        interstitialAdLoadListener = object : AppLovinAdLoadListener {
            override fun adReceived(p0: AppLovinAd?) {
                BIDLog.d(TAG, "Ad received")
                currentAd = p0
                adapter?.onAdLoaded()
            }

            override fun failedToReceiveAd(p0: Int) {
                currentAd = null
                BIDLog.d(TAG, "Failed to receive ad errorCode $p0")
                adapter?.onAdFailedToLoadWithError("Error Failed To ReceiveAd : $p0")
            }

        }
        interstitialAdListener = InterstitialAdListener(TAG, adapter)
        interstitialAdDialog?.setAdDisplayListener(interstitialAdListener)
        interstitialAdDialog?.setAdClickListener(interstitialAdListener)
        interstitialAdDialog?.setAdVideoPlaybackListener(interstitialAdListener)
        AppLovinSdk.getInstance(context).adService.loadNextAd(
            AppLovinAdSize.INTERSTITIAL,
            interstitialAdLoadListener
        )
    }

    override fun show(activity: Activity?) {
        BIDLog.d(TAG, "Show ad")
        if (currentAd == null || interstitialAdDialog == null) {
            adapter?.onFailedToDisplay("Error showing ad")
            return
        }
        interstitialAdDialog?.showAndRender(currentAd)
    }

    override fun activityNeededForShow(): Boolean {
        return false
    }

    override fun activityNeededForLoad(): Boolean {
        return false
    }

    override fun readyToShow(): Boolean {
        return currentAd != null
    }

    override fun shouldWaitForAdToDisplay(): Boolean {
        return true
    }

    override fun revenue(): Double? {
        return null
    }

    override fun destroy() {
        interstitialAdDialog?.setAdDisplayListener(null)
        interstitialAdDialog?.setAdClickListener(null)
        interstitialAdDialog = null
        interstitialAdListener = null
        interstitialAdLoadListener = null
        currentAd = null
    }

    private class InterstitialAdListener(
        private val tag: String,
        private val adapter: BIDFullscreenAdapterProtocol?,
    ) : AppLovinAdDisplayListener, AppLovinAdClickListener, AppLovinAdVideoPlaybackListener {


        override fun adDisplayed(p0: AppLovinAd?) {
            BIDLog.d(tag, "Ad displayed")
            adapter?.onDisplay()
        }

        override fun adHidden(p0: AppLovinAd?) {
            BIDLog.d(tag, "Ad hidden")
            adapter?.onHide()
        }

        override fun adClicked(p0: AppLovinAd?) {
            BIDLog.d(tag, "Ad clicked")
            adapter?.onClick()
        }

        override fun videoPlaybackBegan(p0: AppLovinAd?) {
            BIDLog.d(tag, "Ad clicked video playback began")
        }

        override fun videoPlaybackEnded(p0: AppLovinAd?, p1: Double, p2: Boolean) {
            BIDLog.d(tag, "Ad video play back ended")
        }


    }
}