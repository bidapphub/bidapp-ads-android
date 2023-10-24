package io.bidapp.networks.applovin

import android.app.Activity
import android.util.Log
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
    var sdk: AppLovinSdk? = null
    var interstitialAdDialog: AppLovinInterstitialAdDialog? = null

    private val appLovinAdLoadListener = object : AppLovinAdLoadListener {
        override fun adReceived(p0: AppLovinAd?) {
            BIDLog.d(TAG, "adReceived")
            currentAd = p0
            adapter?.onAdLoaded()
        }

        override fun failedToReceiveAd(p0: Int) {
            BIDLog.d(TAG, "failedToReceiveAd errorCode $p0")
            adapter?.onAdFailedToLoadWithError("Error Failed To ReceiveAd : $p0")
        }

    }
    private val appLovinAdDisplayListener = object : AppLovinAdDisplayListener {
        override fun adDisplayed(p0: AppLovinAd?) {
            BIDLog.d(TAG, "adDisplayed")
            adapter?.onDisplay()
        }

        override fun adHidden(p0: AppLovinAd?) {
            BIDLog.d(TAG, "adHidden")
            adapter?.onHide()
        }

    }

    private val appLovinAdClickListener = object : AppLovinAdClickListener {
        override fun adClicked(p0: AppLovinAd?) {
            BIDLog.d(TAG, "adClicked")
            adapter?.onClick()
        }

    }

    private val appLovinAdVideoPlaybackListener = object : AppLovinAdVideoPlaybackListener {
        override fun videoPlaybackBegan(p0: AppLovinAd?) {
            BIDLog.d(TAG, "adClicked video playback began")
        }

        override fun videoPlaybackEnded(p0: AppLovinAd?, p1: Double, p2: Boolean) {
            BIDLog.d(TAG, "video play back endedad")
        }

    }

    fun init() {
        interstitialAdDialog?.setAdDisplayListener(appLovinAdDisplayListener)
        interstitialAdDialog?.setAdClickListener(appLovinAdClickListener)
        interstitialAdDialog?.setAdVideoPlaybackListener(appLovinAdVideoPlaybackListener)
    }

    override fun load(activity: Activity) {
        val load = runCatching {
            if (interstitialAdDialog == null) {
                sdk = AppLovinSdk.getInstance(activity)
                interstitialAdDialog = AppLovinInterstitialAd.create(sdk, activity)
                init()
            }
            AppLovinSdk.getInstance(activity).adService.loadNextAd(
                AppLovinAdSize.INTERSTITIAL,
                appLovinAdLoadListener
            )
        }
        if (load.isFailure) adapter?.onAdFailedToLoadWithError("Error Failed To ReceiveAd")
    }

    override fun show(activity: Activity?) {
        BIDLog.d(TAG, "show")
        currentAd?.let {
            interstitialAdDialog?.showAndRender(it)
        }
    }

    override fun activityNeededForShow(): Boolean {
        return false
    }

    override fun readyToShow(): Boolean {
        return currentAd != null
    }

    override fun shouldWaitForAdToDisplay(): Boolean {
        return true
    }
}