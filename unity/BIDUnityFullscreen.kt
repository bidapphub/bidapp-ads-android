package io.bidapp.networks.unity

import android.app.Activity
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.protocols.BIDFullscreenAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDFullscreenAdapterProtocol
import com.unity3d.ads.IUnityAdsLoadListener
import com.unity3d.ads.IUnityAdsShowListener
import com.unity3d.ads.UnityAds
import com.unity3d.ads.UnityAdsShowOptions

@PublishedApi
internal class BIDUnityFullscreen(
    val adapter: BIDFullscreenAdapterProtocol? = null,
    val adTag: String?,
    isRewarded: Boolean
) :
    BIDFullscreenAdapterDelegateProtocol {
    val TAG = if (isRewarded) "Reward Unity" else "Full Unity"
    var ready = false
    val loadListener: IUnityAdsLoadListener = object : IUnityAdsLoadListener {
        override fun onUnityAdsAdLoaded(placementId: String) {
            BIDLog.d(TAG, "onUnityAdsAdLoaded: $placementId")
            ready = true
            adapter?.onAdLoaded()
        }

        override fun onUnityAdsFailedToLoad(
            placementId: String,
            error: UnityAds.UnityAdsLoadError,
            message: String
        ) {
            BIDLog.d(TAG, "onUnityAdsFailedToLoad: $placementId error: $error message: $message")
            adapter?.onAdFailedToLoadWithError(error.name)
        }
    }
    val showListener: IUnityAdsShowListener = object : IUnityAdsShowListener {

        override fun onUnityAdsShowFailure(
            placementId: String,
            error: UnityAds.UnityAdsShowError,
            message: String
        ) {
            BIDLog.d(TAG, "onUnityAdsShowFailure: $placementId error: $error message: $message")
            adapter?.onFailedToDisplay(error.name)
        }

        override fun onUnityAdsShowStart(placementId: String) {
            BIDLog.d(TAG, "onUnityAdsShowStart: $placementId")
            adapter?.onDisplay()
        }

        override fun onUnityAdsShowClick(placementId: String) {
            BIDLog.d(TAG, "onUnityAdsShowClick: $placementId")
            adapter?.onClick()
        }

        override fun onUnityAdsShowComplete(
            placementId: String,
            state: UnityAds.UnityAdsShowCompletionState
        ) {
            BIDLog.d(TAG, "onUnityAdsShowComplete: $placementId state: $state")
            adapter?.onHide()
        }
    }

    override fun load(context: Any) {
        val load = runCatching {
            UnityAds.load(adTag, loadListener)
        }
        if (load.isFailure) adapter?.onAdFailedToLoadWithError("Unity fullscreen loading error")
    }

    override fun show(activity: Activity?) {
        val show = runCatching {
            UnityAds.show(
                activity!!,
                adTag,
                UnityAdsShowOptions(),
                showListener
            )
        }
        if (show.isFailure) adapter?.onFailedToDisplay("on unity ads show failure")
    }

    override fun activityNeededForShow(): Boolean {
        return true
    }

    override fun activityNeededForLoad(): Boolean {
        return false
    }

    override fun readyToShow(): Boolean {
        return ready
    }

    override fun shouldWaitForAdToDisplay(): Boolean {
        return true
    }

    override fun revenue(): Double? {
        return null
    }
}