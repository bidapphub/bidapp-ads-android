package io.bidapp.networks.yandex

import android.app.Activity
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequestConfiguration
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.protocols.BIDFullscreenAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDFullscreenAdapterProtocol

class BIDYandexInterstitial(
    val adapter: BIDFullscreenAdapterProtocol? = null,
    val adUnitId: String? = null
) :
    BIDFullscreenAdapterDelegateProtocol {

    val TAG = "Full Yandex"
    private var interstitialAdLoader : InterstitialAdLoader? = null
    private var interstitialAdListener: InterstitialListenerAd? = null


    override fun load(context: Any) {
        if (context as? Activity == null) {
            adapter?.onAdFailedToLoadWithError("Yandex fullscreen loading error")
            return
        }
        if (adUnitId.isNullOrEmpty()) {
            adapter?.onAdFailedToLoadWithError("Yandex fullscreen adUnitId is null or incorrect format")
            return
        }
        interstitialAdListener = InterstitialListenerAd(TAG, adapter, adUnitId)
        val loadInterstitialAdRunnable = Runnable {
            if (interstitialAdLoader == null) {
                interstitialAdLoader = InterstitialAdLoader(context.applicationContext)
            }
            interstitialAdLoader?.setAdLoadListener(interstitialAdListener)
            val adRequestConfiguration = AdRequestConfiguration.Builder(adUnitId).build()
            interstitialAdLoader?.loadAd(adRequestConfiguration)
        }
        (context).runOnUiThread(loadInterstitialAdRunnable)
    }

    override fun show(activity: Activity?) {
        if (interstitialAdListener?.interstitial == null || activity == null){
        adapter?.onAdFailedToLoadWithError("Yandex interstitial ad failed to load. $adUnitId")
        return
        }
        interstitialAdListener?.interstitial?.setAdEventListener(interstitialAdListener)
        interstitialAdListener?.interstitial?.show(activity)
    }

    override fun activityNeededForShow(): Boolean {
        return true
    }

    override fun activityNeededForLoad(): Boolean {
        return true
    }

    override fun readyToShow(): Boolean {
       return interstitialAdLoader != null
    }

    override fun shouldWaitForAdToDisplay(): Boolean {
        return true
    }

    override fun destroy() {
        interstitialAdListener?.interstitial?.setAdEventListener(null)
        interstitialAdListener = null
    }

    override fun revenue(): Double? {
        return null
    }

    private class InterstitialListenerAd(
        private val tag : String,
        private val adapter: BIDFullscreenAdapterProtocol?,
        private val adUnitId: String?
    ) : InterstitialAdLoadListener, InterstitialAdEventListener {
        var interstitial : InterstitialAd? = null
        override fun onAdLoaded(interstitialAd: InterstitialAd) {
            interstitial = interstitialAd
            BIDLog.d(tag, "Ad load $adUnitId")
            adapter?.onAdLoaded()
        }

        override fun onAdFailedToLoad(error: AdRequestError) {
            interstitial = null
            BIDLog.d(tag, "Error $adUnitId exception: ${error.description}")
            adapter?.onAdFailedToLoadWithError(error.description)
        }

        override fun onAdShown() {
            BIDLog.d(tag, "Ad shown. $adUnitId")
            if (BIDYandexSDK.debugOrTesting != null && BIDYandexSDK.debugOrTesting == true){
                adapter?.onDisplay()
            }
        }

        override fun onAdFailedToShow(adError: AdError) {
            BIDLog.d(tag, "Error $adUnitId exception: ${adError.description}")
            adapter?.onFailedToDisplay(adError.description)
        }

        override fun onAdDismissed() {
            BIDLog.d(tag, "Ad dismiss. $adUnitId")
            adapter?.onHide()
        }

        override fun onAdClicked() {
            BIDLog.d(tag, "Ad click. $adUnitId")
            adapter?.onClick()
        }

        override fun onAdImpression(impressionData: ImpressionData?) {
            BIDLog.d(tag, "Ad impression. $adUnitId")
            adapter?.onDisplay()
        }
    }
}