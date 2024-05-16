package io.bidapp.networks.startIo

import android.app.Activity
import android.content.Context
import com.startapp.sdk.adsbase.Ad
import com.startapp.sdk.adsbase.StartAppAd
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener
import com.startapp.sdk.adsbase.adlisteners.AdEventListener
import com.startapp.sdk.adsbase.adlisteners.VideoListener
import com.startapp.sdk.adsbase.model.AdPreferences
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.protocols.BIDFullscreenAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDFullscreenAdapterProtocol

@PublishedApi
internal class BIDStartIoFullscreen(
    val adapter: BIDFullscreenAdapterProtocol? = null,
    val adTag: String?,
    val isRewarded: Boolean,
    private val ecpm: Double
) :
    BIDFullscreenAdapterDelegateProtocol {
    val TAG = if (isRewarded) "Reward StartIo" else "Full StartIo"
    private var startAppAd: StartAppAd? = null
    private var startAppAdPreferences: AdPreferences? = null
    private var isRewardGranted = false


    private val loadListener: AdEventListener = object : AdEventListener {
        override fun onReceiveAd(p0: Ad) {
            BIDLog.d(TAG, "Fullscreen ads load: $adTag")
            adapter?.onAdLoaded()
        }

        override fun onFailedToReceiveAd(p0: Ad?) {
            BIDLog.d(TAG, "Fullscreen ads load failed: $adTag")
            adapter?.onAdFailedToLoadWithError(p0?.errorMessage.toString())
        }

    }

    private val rewardListener = VideoListener {
        if (isRewarded) {
            BIDLog.d(TAG, "ad rewarded adTag: $adTag")
            isRewardGranted = true
        }
    }

    private val adDisplayListener = object : AdDisplayListener {
        override fun adHidden(p0: Ad?) {
            BIDLog.d(TAG, "ad hide $adTag")
            if (isRewarded && isRewardGranted) {
                adapter?.onReward()
                isRewardGranted = false
            }
            adapter?.onHide()
        }

        override fun adDisplayed(p0: Ad?) {
            BIDLog.d(TAG, "ad displayed $adTag")
            adapter?.onDisplay()
        }

        override fun adClicked(p0: Ad?) {
            BIDLog.d(TAG, "ad clicked $adTag")
            adapter?.onClick()
        }

        override fun adNotDisplayed(p0: Ad?) {
            val errorMessage = p0?.errorMessage ?: "Unknown error"
            BIDLog.d(TAG, "ad failed to display $adTag")
            adapter?.onFailedToDisplay(errorMessage)
        }
    }

    private fun init() {
        startAppAd?.setVideoListener(rewardListener)
    }

    override fun load(context: Any) {
        if (context as? Context == null){
            adapter?.onAdFailedToLoadWithError("StartIo fullscreen loading error")
            return
        }
        if (startAppAd == null) {
            startAppAd = StartAppAd(context)
        }
        init()
        startAppAdPreferences = AdPreferences()
        if (adTag.isNullOrEmpty()) BIDLog.d(TAG, "StartIo adTag is null or empty")
        else startAppAdPreferences?.adTag = adTag
        startAppAdPreferences?.minCpm = ecpm
        if (isRewarded) {
            startAppAd?.loadAd(
                StartAppAd.AdMode.REWARDED_VIDEO,
                startAppAdPreferences,
                loadListener
            )
        } else startAppAd?.loadAd(StartAppAd.AdMode.VIDEO, startAppAdPreferences, loadListener)
}

override fun show(activity: Activity?) {
    if (startAppAd == null || startAppAd?.isReady == false) {
        adapter?.onFailedToDisplay("Failed to display")
        return
    }
    startAppAd?.showAd(adDisplayListener)
}

override fun activityNeededForShow(): Boolean {
    return false
}

override fun activityNeededForLoad(): Boolean {
    return false
}

override fun readyToShow(): Boolean {
    return startAppAd?.isReady ?: false
}

override fun shouldWaitForAdToDisplay(): Boolean {
    return true
}

override fun revenue(): Double? {
    return startAppAdPreferences?.minCpm
}

    override fun destroy() {
        startAppAd?.close()
        startAppAd?.setVideoListener(null)
        startAppAd = null
    }
}