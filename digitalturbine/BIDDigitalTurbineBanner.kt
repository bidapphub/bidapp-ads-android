package io.bidapp.networks.digitalturbine

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import com.fyber.inneractive.sdk.external.ImpressionData
import com.fyber.inneractive.sdk.external.InneractiveAdRequest
import com.fyber.inneractive.sdk.external.InneractiveAdSpot
import com.fyber.inneractive.sdk.external.InneractiveAdSpotManager
import com.fyber.inneractive.sdk.external.InneractiveAdViewEventsListenerWithImpressionData
import com.fyber.inneractive.sdk.external.InneractiveAdViewUnitController
import com.fyber.inneractive.sdk.external.InneractiveErrorCode
import com.fyber.inneractive.sdk.external.InneractiveUnitController
import io.bidapp.sdk.AdFormat
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.protocols.BIDBannerAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDBannerAdapterProtocol
import java.lang.ref.WeakReference


class BIDDigitalTurbineBanner(val adapter: BIDBannerAdapterProtocol, val adTag: String?, val format: AdFormat) : BIDBannerAdapterDelegateProtocol {

    val TAG = "Banner Digital Turbine"
    private var adViewSpot : WeakReference<InneractiveAdSpot>? = null
    var ready = false
    var view : WeakReference<View>? = null
    private var controller : InneractiveAdViewUnitController? = null

    private val bannerEventListener = object : InneractiveAdViewEventsListenerWithImpressionData{
        override fun onAdImpression(p0: InneractiveAdSpot?, p1: ImpressionData?) {
            BIDLog.d(TAG, "ad show adTag: ($adTag)")
            adapter.onDisplay()
        }

        override fun onAdImpression(p0: InneractiveAdSpot?) {
        }

        override fun onAdClicked(p0: InneractiveAdSpot?) {
            BIDLog.d(TAG, "ad clicked. adTag: ($adTag)")
            adapter.onClick()
        }

        override fun onAdWillCloseInternalBrowser(p0: InneractiveAdSpot?) {}

        override fun onAdWillOpenExternalApp(p0: InneractiveAdSpot?) {}

        override fun onAdEnteredErrorState(
            p0: InneractiveAdSpot?,
            p1: InneractiveUnitController.AdDisplayError?
        ) {
            val error = p1?.message ?: "Unknown error"
            BIDLog.d(TAG, "failed to play $error")
            adapter.onFailedToDisplay(Error(error))
        }

        override fun onAdExpanded(p0: InneractiveAdSpot?) {
        }

        override fun onAdResized(p0: InneractiveAdSpot?) {}

        override fun onAdCollapsed(p0: InneractiveAdSpot?) {}

    }

    private val bannerLoadListener = object : InneractiveAdSpot.RequestListener{
        override fun onInneractiveSuccessfulAdRequest(p0: InneractiveAdSpot?) {
            ready = true
            BIDLog.d(TAG, "ad loaded adTag: ($adTag)")
            adapter.onLoad()
        }

        override fun onInneractiveFailedAdRequest(
            p0: InneractiveAdSpot?,
            p1: InneractiveErrorCode?
        ) {
            val error = p1 ?: "Unknown Error"
            BIDLog.d(TAG, "Admob failed to load ad. adTag: ($adTag) Error: $error")
            adapter.onFailedToLoad(Error(error.toString()))
            ready = false
        }
    }
    override fun nativeAdView(): WeakReference<View>? {
        return view
    }

    override fun isAdReady(): Boolean {
       return adViewSpot?.get()?.isReady ?: false
    }

    override fun load(context: Any) {
        if (context as? Context == null){
            adapter.onFailedToLoad(Error("banner load is failed"))
            return
        }
        if (!format.isBanner_320x50 && !format.isBanner_300x250){
            adapter.onFailedToLoad(Error("Unsupported Digital Turbine banner format"))
            return
        }
        if (adTag.isNullOrEmpty()){
            adapter.onFailedToLoad(Error("Digital Turbine banner adtag is null or empty"))
            return
        }

        val load = runCatching {
            if (adViewSpot == null || controller == null) {
                controller = InneractiveAdViewUnitController()
                adViewSpot = WeakReference(InneractiveAdSpotManager.get().createSpot())
            }
            controller?.eventsListener = bannerEventListener
            adViewSpot?.get()?.addUnitController(controller)
            adViewSpot?.get()?.setRequestListener(bannerLoadListener)
            val request = InneractiveAdRequest(adTag)
            adViewSpot?.get()?.requestAd(request)
        }
        if (load.isFailure) adapter.onFailedToLoad(Error("banner load is failed"))
    }

    override fun destroy() {
        adViewSpot?.get()?.destroy()
        adViewSpot = null
        (view?.get() as? FrameLayout)?.removeAllViews()
        view?.clear()
    }

    override fun showOnView(view: WeakReference<View>, density: Float): Boolean {
        return try {
            val weightAndHeight: Array<Int> = when (format.isBanner_320x50()) {
                false -> arrayOf(300, 250)
                true -> arrayOf(320, 50)
            }
            (view.get() as FrameLayout).layoutParams.width = (weightAndHeight[0] * density).toInt()
            (view.get() as FrameLayout).layoutParams.height = (weightAndHeight[1] * density).toInt()
            controller!!.bindView(view.get() as FrameLayout)
            this.view = view
            true
        } catch (e: Exception) {
            BIDLog.d(TAG, "Show on view is failed ${e.message}")
            false
        }
    }

    override fun waitForAdToShow(): Boolean {
        return true
    }

    override fun activityNeededForLoad(): Boolean {
        return false
    }

    override fun revenue(): Double? {
        return null
    }
}