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
    var interstitialAd : InterstitialAd? = null


    private var loadCallback : InterstitialAdLoadListener? = object : InterstitialAdLoadListener{
        override fun onAdLoaded(interstitialAd: InterstitialAd) {
            this@BIDYandexInterstitial.interstitialAd = interstitialAd
            BIDLog.d(TAG, "on ad load $adUnitId")
            adapter?.onAdLoaded()
        }

        override fun onAdFailedToLoad(error: AdRequestError) {
            interstitialAd = null
            BIDLog.d(TAG, "onError $adUnitId exception: ${error.description}")
            adapter?.onAdFailedToLoadWithError(error.description)
        }

    }

    private val showCallback = object : InterstitialAdEventListener{
        override fun onAdShown() {
            BIDLog.d(TAG, "on ad shown. $adUnitId")
            if (BIDYandexSDK.debugOrTesting != null && BIDYandexSDK.debugOrTesting == true){
                adapter?.onDisplay()
            }
        }

        override fun onAdFailedToShow(adError: AdError) {
            BIDLog.d(TAG, "onError $adUnitId exception: ${adError.description}")
            adapter?.onFailedToDisplay(adError.description)
        }

        override fun onAdDismissed() {
            BIDLog.d(TAG, "on ad dismiss. $adUnitId")
            adapter?.onHide()
        }

        override fun onAdClicked() {
            BIDLog.d(TAG, "on ad click. $adUnitId")
            adapter?.onClick()
        }

        override fun onAdImpression(impressionData: ImpressionData?) {
            BIDLog.d(TAG, "on ad impression. $adUnitId")
            adapter?.onDisplay()
        }

    }

    override fun load(context: Any) {
        if (context as? Activity == null) {
            adapter?.onAdFailedToLoadWithError("Yandex fullscreen loading error")
            return
        }
        if (adUnitId.isNullOrEmpty()) {
            adapter?.onAdFailedToLoadWithError("Yandex fullscreen adUnitId is null or incorrect format")
            return
        }
        interstitialAd = null
        val loadInterstitialAdRunnable = Runnable {
            if (interstitialAdLoader == null) {
                interstitialAdLoader = InterstitialAdLoader(context.applicationContext)
            }
            interstitialAdLoader?.setAdLoadListener(loadCallback)
            val adRequestConfiguration = AdRequestConfiguration.Builder(adUnitId).build()
            interstitialAdLoader?.loadAd(adRequestConfiguration)
        }
        (context as Activity).runOnUiThread(loadInterstitialAdRunnable)
    }

    override fun show(activity: Activity?) {
        if (interstitialAd == null || activity == null){
        adapter?.onAdFailedToLoadWithError("Yandex interstitial ad failed to load. $adUnitId")
        return
        }
        interstitialAd?.setAdEventListener(showCallback)
        interstitialAd?.show(activity)
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
        if ( interstitialAd != null )
        {
            interstitialAd?.setAdEventListener(null)
            loadCallback = null
            interstitialAd = null
        }
    }

    override fun revenue(): Double? {
        return null
    }
}