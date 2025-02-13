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
    private val adTag: String?,
    private val isRewarded: Boolean
) :
    BIDFullscreenAdapterDelegateProtocol {
    val TAG = if (isRewarded) "Reward Unity" else "Full Unity"
    private var fullscreenAdListener : FullscreenAdListener? = null

    override fun load(context: Any) {
        if (adTag.isNullOrEmpty()) {
            adapter?.onAdFailedToLoadWithError("Unity fullscreen adTag is null or empty")
            return
        }
        fullscreenAdListener = FullscreenAdListener(TAG, adapter, isRewarded)
        UnityAds.load(adTag, fullscreenAdListener)
    }

    override fun show(activity: Activity?) {
        if (adTag == null || activity == null) {
            adapter?.onFailedToDisplay("Unity fullscreen showing is failure")
            return
        }
        UnityAds.show(
            activity,
            adTag,
            UnityAdsShowOptions(),
            fullscreenAdListener
        )
    }

    override fun activityNeededForShow(): Boolean {
        return true
    }

    override fun activityNeededForLoad(): Boolean {
        return false
    }

    override fun readyToShow(): Boolean {
        return fullscreenAdListener?.ready ?: false
    }

    override fun shouldWaitForAdToDisplay(): Boolean {
        return true
    }

    override fun revenue(): Double? {
        return null
    }

    override fun destroy() {
        fullscreenAdListener = null
    }

    private class FullscreenAdListener (
        private val tag : String,
        private val adapter: BIDFullscreenAdapterProtocol?,
        private val isRewarded : Boolean
    ) : IUnityAdsLoadListener, IUnityAdsShowListener {
        var ready = false

        override fun onUnityAdsAdLoaded(placementId: String) {
            BIDLog.d(tag, "Ad loaded: $placementId")
            ready = true
            adapter?.onAdLoaded()
        }

        override fun onUnityAdsFailedToLoad(
            placementId: String,
            error: UnityAds.UnityAdsLoadError,
            message: String
        ) {
            BIDLog.d(tag, "Ad failed to load: $placementId error: $error message: $message")
            adapter?.onAdFailedToLoadWithError(error.name)
        }

        override fun onUnityAdsShowFailure(
            placementId: String,
            error: UnityAds.UnityAdsShowError,
            message: String
        ) {
            BIDLog.d(tag, "Ad show failure: $placementId error: $error message: $message")
            adapter?.onFailedToDisplay(error.name)
        }

        override fun onUnityAdsShowStart(placementId: String) {
            BIDLog.d(tag, "Ad show start: $placementId")
            adapter?.onDisplay()
        }

        override fun onUnityAdsShowClick(placementId: String) {
            BIDLog.d(tag, "Ad click: $placementId")
            adapter?.onClick()
        }

        override fun onUnityAdsShowComplete(
            placementId: String,
            state: UnityAds.UnityAdsShowCompletionState
        ) {
            BIDLog.d(tag, "Ad show complete: $placementId state: $state")
            if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED && isRewarded) {
                BIDLog.d(tag, "on ad rewarded $placementId")
                adapter?.onReward()
                adapter?.onHide()
            } else {
                adapter?.onHide()
            }
        }

    }
}