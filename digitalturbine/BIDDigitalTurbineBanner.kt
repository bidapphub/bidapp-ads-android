package io.bidapp.networks.digitalturbine

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
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


class BIDDigitalTurbineBanner(val adapter: BIDBannerAdapterProtocol, val adTag: String?, val format: AdFormat) : BIDBannerAdapterDelegateProtocol, InneractiveAdViewEventsListenerWithImpressionData {

    val TAG = "Banner Digital Turbine"
    private var adViewSpot : WeakReference<InneractiveAdSpot>? = null
    var ready = false
    var view : WeakReference<View>? = null
    private var controller : InneractiveAdViewUnitController? = null

     private val bannerLoadListener = object : InneractiveAdSpot.RequestListener{
        override fun onInneractiveSuccessfulAdRequest(p0: InneractiveAdSpot?) {
            ready = true
            BIDLog.d(TAG, "Ad loaded adTag: ($adTag)")
            adapter.onLoad()
        }

        override fun onInneractiveFailedAdRequest(
            p0: InneractiveAdSpot?,
            p1: InneractiveErrorCode?
        ) {
            val error = p1 ?: "Unknown Error"
            BIDLog.d(TAG, "Failed to load ad. adTag: ($adTag) Error: $error")
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
            adapter.onFailedToLoad(Error("Banner load is failed"))
            return
        }
        if (!format.isBanner_320x50 && !format.isBanner_300x250 && !format.isBanner_728x90){
            adapter.onFailedToLoad(Error("Unsupported Digital Turbine banner format : ${format.name()}"))
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
            controller?.eventsListener = this
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
            val weightAndHeight: Array<Int> = when (format.name()) {
                "MREC" -> arrayOf(300, 250)
                "BANNER" -> arrayOf(320, 50)
                "LEADERBOARD" -> arrayOf(728, 90)
                else -> arrayOf(0,0)
            }
            val adViewGroup = RelativeLayout(view.get()?.context)
            val layoutParams = RelativeLayout.LayoutParams(
                (weightAndHeight[0] * density).toInt(),
                (weightAndHeight[1] * density).toInt()
            )
            adViewGroup.layoutParams = layoutParams
            controller!!.bindView(adViewGroup)
            (view.get() as FrameLayout).addView(
                adViewGroup,
                0,
                ViewGroup.LayoutParams(
                    (weightAndHeight[0] * density).toInt(),
                    (weightAndHeight[1] * density).toInt()
                )
            )
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

    override fun onAdImpression(p0: InneractiveAdSpot?, p1: ImpressionData?) {
        BIDLog.d(TAG, "Ad show adTag: ($adTag)")
        adapter.onDisplay()
    }

    override fun onAdImpression(p0: InneractiveAdSpot?) {}

    override fun onAdClicked(p0: InneractiveAdSpot?) {
        BIDLog.d(TAG, "Ad clicked. adTag: ($adTag)")
        adapter.onClick()
    }

    override fun onAdWillCloseInternalBrowser(p0: InneractiveAdSpot?) {
        BIDLog.d(TAG, "Ad close internal browser. adTag: ($adTag)")
    }

    override fun onAdWillOpenExternalApp(p0: InneractiveAdSpot?) {
        BIDLog.d(TAG, "Ad will open external app. adTag: ($adTag)")
    }

    override fun onAdEnteredErrorState(
        p0: InneractiveAdSpot?,
        p1: InneractiveUnitController.AdDisplayError?
    ) {
        val error = p1?.message ?: "Unknown error"
        BIDLog.d(TAG, "Failed to play ad $error adTag: ($adTag)")
        adapter.onFailedToDisplay(Error(error))
    }

    override fun onAdExpanded(p0: InneractiveAdSpot?) {
        BIDLog.d(TAG, "On ad expanded adTag: ($adTag)")
    }

    override fun onAdResized(p0: InneractiveAdSpot?) {
        BIDLog.d(TAG, "On ad resized adTag: ($adTag)")
    }

    override fun onAdCollapsed(p0: InneractiveAdSpot?) {
        BIDLog.d(TAG, "On ad collapsed adTag: ($adTag)")
    }


}