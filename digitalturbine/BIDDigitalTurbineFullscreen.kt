package io.bidapp.networks.digitalturbine

import android.app.Activity
import com.fyber.inneractive.sdk.external.ImpressionData
import com.fyber.inneractive.sdk.external.InneractiveAdRequest
import com.fyber.inneractive.sdk.external.InneractiveAdSpot
import com.fyber.inneractive.sdk.external.InneractiveAdSpotManager
import com.fyber.inneractive.sdk.external.InneractiveErrorCode
import com.fyber.inneractive.sdk.external.InneractiveFullScreenAdRewardedListener
import com.fyber.inneractive.sdk.external.InneractiveFullscreenAdEventsListenerWithImpressionData
import com.fyber.inneractive.sdk.external.InneractiveFullscreenUnitController
import com.fyber.inneractive.sdk.external.InneractiveFullscreenVideoContentController
import com.fyber.inneractive.sdk.external.InneractiveUnitController
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.protocols.BIDFullscreenAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDFullscreenAdapterProtocol



class BIDDigitalTurbineFullscreen(
    val adapter: BIDFullscreenAdapterProtocol? = null,
    val adTag: String?,
    val isRewarded: Boolean
) : BIDFullscreenAdapterDelegateProtocol {
    val TAG = if (isRewarded) "Reward Digital Turbine" else "Interstitial Digital Turbine"
    private var fullscreenSpot: InneractiveAdSpot? = null
    private var fullscreenAdListener : FullscreenAdListener? = null


    override fun load(context: Any) {
        if (adTag.isNullOrEmpty()) {
            adapter?.onAdFailedToLoadWithError("Digital turbine adTag is null or empty")
            return
        }
        var videoContentController : InneractiveFullscreenVideoContentController? = null
        var controller : InneractiveFullscreenUnitController? = null
        val load = runCatching {
            if (fullscreenSpot == null) {
                videoContentController = InneractiveFullscreenVideoContentController()
                controller = InneractiveFullscreenUnitController()
                controller?.addContentController(videoContentController)
                fullscreenSpot = InneractiveAdSpotManager.get().createSpot()
            }
            fullscreenAdListener = FullscreenAdListener(TAG, adapter, adTag, isRewarded)
            if (isRewarded) {
                controller?.rewardedListener = fullscreenAdListener
                videoContentController?.eventsListener = fullscreenAdListener
            }
            controller?.eventsListener = fullscreenAdListener
            fullscreenSpot?.addUnitController(controller)
            fullscreenSpot?.setRequestListener(fullscreenAdListener)
            val fullscreenAdRequest = InneractiveAdRequest(adTag)
            fullscreenSpot?.requestAd(fullscreenAdRequest)
        }
        if (load.isFailure) adapter?.onAdFailedToLoadWithError("Unknown error")
    }

    override fun show(activity: Activity?) {
        val showController =
            (fullscreenSpot?.selectedUnitController as? InneractiveFullscreenUnitController)
        if (activity == null || showController == null) {
            adapter?.onFailedToDisplay("Digital turbine showing ad is failure")
            return
        }
        showController.show(activity)
    }

    override fun activityNeededForShow(): Boolean {
        return true
    }

    override fun activityNeededForLoad(): Boolean {
        return false
    }

    override fun readyToShow(): Boolean {
        return fullscreenSpot?.isReady ?: false
    }

    override fun revenue(): Double? {
        return null
    }

    override fun destroy() {
        fullscreenSpot?.destroy()
        fullscreenSpot = null
        fullscreenAdListener = null
    }

    override fun shouldWaitForAdToDisplay(): Boolean {
        return true
    }

    private class FullscreenAdListener (
        private val tag : String,
        private val adapter: BIDFullscreenAdapterProtocol?,
        private val adTag: String?,
        private val isRewarded : Boolean
    ) : InneractiveFullscreenAdEventsListenerWithImpressionData, InneractiveAdSpot.RequestListener, InneractiveFullscreenVideoContentController(), InneractiveFullScreenAdRewardedListener  {
        var isGrantedReward = false


        override fun onAdImpression(p0: InneractiveAdSpot?, p1: ImpressionData?) {
            BIDLog.d(tag, "Ad displayed $adTag")
            adapter?.onDisplay()
        }

        override fun onAdImpression(p0: InneractiveAdSpot?) {}

        override fun onAdClicked(p0: InneractiveAdSpot?) {
            BIDLog.d(tag, "Ad clicked $adTag")
            adapter?.onClick()
        }

        override fun onAdWillCloseInternalBrowser(p0: InneractiveAdSpot?) {
            BIDLog.d(tag, "Ad will close internal browser $adTag")
        }

        override fun onAdWillOpenExternalApp(p0: InneractiveAdSpot?) {
            BIDLog.d(tag, "Ad will open external app $adTag")
        }

        override fun onAdEnteredErrorState(
            p0: InneractiveAdSpot?,
            p1: InneractiveUnitController.AdDisplayError?
        ) {
            val errorMessage = p1?.localizedMessage ?: "Unknown error"
            BIDLog.d(tag, "Ad failed to display $adTag")
            adapter?.onFailedToDisplay(errorMessage)
        }

        override fun onAdDismissed(p0: InneractiveAdSpot?) {
            if (isRewarded) {
                BIDLog.d(tag, "Ad hide $adTag")
                adapter?.onHide()
                if (isGrantedReward) {
                    BIDLog.d(tag, "Ad rewarded $adTag")
                    adapter?.onReward()
                    isGrantedReward = false
                }
            } else {
                BIDLog.d(tag, "Ad hide $adTag")
                adapter?.onHide()
            }
        }

        override fun onInneractiveSuccessfulAdRequest(p0: InneractiveAdSpot?) {
            BIDLog.d(tag, "Ad load $adTag")
            adapter?.onAdLoaded()
        }

        override fun onInneractiveFailedAdRequest(
            p0: InneractiveAdSpot?,
            p1: InneractiveErrorCode?
        ) {
            val error = p1?.name ?: "Unknown error"
            BIDLog.d(tag, "On Error $adTag exception: $error")
            adapter?.onAdFailedToLoadWithError(error)
        }

        override fun onProgress(p0: Int, p1: Int) {
            BIDLog.d(tag, "Ad on progress $adTag")
        }

        override fun onCompleted() {
            BIDLog.d(tag, "Ad on complete $adTag")
        }

        @Deprecated("Deprecated in Java")
        override fun onPlayerError() {
            BIDLog.d(tag, "Ad error $adTag")
            adapter?.onFailedToDisplay("Ad error")
        }

        override fun onAdRewarded(p0: InneractiveAdSpot?) {
            isGrantedReward = true
        }

    }
}