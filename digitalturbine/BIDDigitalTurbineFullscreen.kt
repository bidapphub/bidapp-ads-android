package io.bidapp.networks.digitalturbine

import android.app.Activity
import android.util.Log
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
import java.lang.ref.WeakReference


class BIDDigitalTurbineFullscreen(
    val adapter: BIDFullscreenAdapterProtocol? = null,
    val adTag: String?,
    val isRewarded: Boolean
) : BIDFullscreenAdapterDelegateProtocol {
    val TAG = if (isRewarded) "Reward Digital Turbine" else "Full Digital Turbine"
    var isGrantedReward = false
    var fullscreenSpot: WeakReference<InneractiveAdSpot>? = null


    val showEventListener = object : InneractiveFullscreenAdEventsListenerWithImpressionData {
        override fun onAdImpression(p0: InneractiveAdSpot?, p1: ImpressionData?) {
            BIDLog.d(TAG, "ad displayed $adTag")
            adapter?.onDisplay()
        }

        override fun onAdImpression(p0: InneractiveAdSpot?) {}

        override fun onAdClicked(p0: InneractiveAdSpot?) {
            BIDLog.d(TAG, "ad clicked $adTag")
            adapter?.onAdLoaded()
        }

        override fun onAdWillCloseInternalBrowser(p0: InneractiveAdSpot?) {}

        override fun onAdWillOpenExternalApp(p0: InneractiveAdSpot?) {}

        override fun onAdEnteredErrorState(
            p0: InneractiveAdSpot?,
            p1: InneractiveUnitController.AdDisplayError?
        ) {
            val errorMessage = p1?.localizedMessage ?: "Unknown error"
            BIDLog.d(TAG, "Ad failed to display $adTag")
            adapter?.onFailedToDisplay(errorMessage)
        }

        override fun onAdDismissed(p0: InneractiveAdSpot?) {
            if (isRewarded) {
                BIDLog.d(TAG, "ad hide $adTag")
                adapter?.onHide()
                if (isGrantedReward) {
                    BIDLog.d(TAG, "rewarded $adTag")
                    adapter?.onReward()
                }
            } else {
                BIDLog.d(TAG, "ad hide $adTag")
                adapter?.onHide()
            }
        }

    }

    private val loadEventListener = object : InneractiveAdSpot.RequestListener {
        override fun onInneractiveSuccessfulAdRequest(p0: InneractiveAdSpot?) {
            BIDLog.d(TAG, "ad load $adTag")
            adapter?.onAdLoaded()
        }

        override fun onInneractiveFailedAdRequest(
            p0: InneractiveAdSpot?,
            p1: InneractiveErrorCode?
        ) {
            val error = p1?.name ?: "Unknown error"
            BIDLog.d(TAG, "onError $adTag exception: $error")
            adapter?.onAdFailedToLoadWithError(error)
        }

    }

    private val contentEventListener = object : InneractiveFullscreenVideoContentController() {
        override fun onProgress(p0: Int, p1: Int) {
            BIDLog.d(TAG, "rewarded ad on progress $adTag")
        }

        override fun onCompleted() {
            BIDLog.d(TAG, "rewarded ad on complete $adTag")
        }

        override fun onPlayerError() {
            BIDLog.d(TAG, "rewarded ad error $adTag")
            adapter?.onFailedToDisplay("rewarded ad error")
        }
    }

    private val rewardEventListener = object : InneractiveFullScreenAdRewardedListener {
        override fun onAdRewarded(p0: InneractiveAdSpot?) {
            isGrantedReward = true
        }
    }


    override fun load(context: Any) {
        if (adTag == null) {
            adapter?.onAdFailedToLoadWithError("Digital turbine adtag is null")
            return
        }
        var videoContentController : InneractiveFullscreenVideoContentController? = null
        var controller : InneractiveFullscreenUnitController? = null
        val load = runCatching {
            if (fullscreenSpot?.get() == null) {
                videoContentController = InneractiveFullscreenVideoContentController()
                controller = InneractiveFullscreenUnitController()
                controller?.addContentController(videoContentController)
                fullscreenSpot = WeakReference(InneractiveAdSpotManager.get().createSpot())
            }
            if (isRewarded) {
                controller?.rewardedListener = rewardEventListener
                videoContentController?.eventsListener = contentEventListener
            }
            controller?.eventsListener = showEventListener
            fullscreenSpot?.get()?.addUnitController(controller)
            fullscreenSpot?.get()?.setRequestListener(loadEventListener)
            val fullscreenAdRequest = InneractiveAdRequest(adTag)
            fullscreenSpot?.get()?.requestAd(fullscreenAdRequest)
        }
        if (load.isFailure) adapter?.onAdFailedToLoadWithError("Unknown error")
    }

    override fun show(activity: Activity?) {
        val showController =
            (fullscreenSpot?.get()?.selectedUnitController as? InneractiveFullscreenUnitController)
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
        return fullscreenSpot?.get()?.isReady ?: false
    }

    override fun revenue(): Double? {
        return null
    }

    override fun destroy() {
        fullscreenSpot?.get()?.destroy()
        fullscreenSpot = null
    }

    override fun shouldWaitForAdToDisplay(): Boolean {
        return true
    }
}