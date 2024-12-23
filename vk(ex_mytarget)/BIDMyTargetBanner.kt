package io.bidapp.networks.mytarget

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.my.target.ads.MyTargetView
import com.my.target.ads.MyTargetView.AdSize
import com.my.target.common.models.IAdLoadingError
import io.bidapp.sdk.AdFormat
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.protocols.BIDBannerAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDBannerAdapterProtocol
import java.lang.ref.WeakReference

class BIDMyTargetBanner(private val adapter: BIDBannerAdapterProtocol, private val slotId: String?, format: AdFormat) :
    BIDBannerAdapterDelegateProtocol, MyTargetView.MyTargetViewListener {
    private val slotIdToInt = slotId?.toIntOrNull()
    private var adView: WeakReference<MyTargetView>? = null
    private var cachedAd: WeakReference<MyTargetView>? = null
    private val TAG = "Banner MyTarget"
    private val bannerFormat = if (format.isBanner_320x50) AdSize.ADSIZE_320x50
    else if (format.isBanner_300x250) AdSize.ADSIZE_300x250
    else if (format.isBanner_728x90) AdSize.ADSIZE_728x90
    else {
        BIDLog.d(TAG, "Unsupported MyTarget banner format: ${format?.name()}")
        null
    }

    override fun nativeAdView(): WeakReference<View>? {
        return WeakReference(adView?.get() as? View)
    }

    override fun isAdReady(): Boolean {
      return cachedAd?.get() != null
    }

    override fun load(context: Any) {
        cachedAd = null
        if (context as? Context == null || bannerFormat == null){
            adapter.onFailedToLoad(Error("MyTarget banner loading error"))
            return
        }
        if (slotIdToInt == null){
            adapter.onFailedToLoad(Error("MyTarget banner slotId is null or incorrect format"))
            return
        }
        if (adView?.get() == null) {
            adView = WeakReference(MyTargetView(context))
            adView?.get()?.setSlotId(slotIdToInt)
            adView?.get()?.setAdSize(bannerFormat)
            adView?.get()?.listener = this
            adView?.get()?.load()
        }
    }

    override fun destroy() {
        adView?.get()?.destroy()
        adView?.clear()
    }

    override fun showOnView(view: WeakReference<View>, density: Float): Boolean {
        return try {
            val weightAndHeight: Array<Int> = when (bannerFormat) {
                AdSize.ADSIZE_300x250 -> arrayOf(300, 250)
                AdSize.ADSIZE_320x50 -> arrayOf(320, 50)
                AdSize.ADSIZE_728x90 -> arrayOf(728, 90)
                else -> arrayOf(0, 0)
            }
            (view.get() as FrameLayout).addView(
                adView!!.get()!!,
                0,
                ViewGroup.LayoutParams(
                    (weightAndHeight[0] * density).toInt(),
                    (weightAndHeight[1] * density).toInt()
                )
            )
            return true
        } catch (e: Exception) {
            BIDLog.d(TAG, "Show on view is failed")
            false
        }
    }

    override fun waitForAdToShow(): Boolean {
    return true
    }

    override fun activityNeededForLoad(): Boolean {
    return false
    }

    override fun onLoad(p0: MyTargetView) {
        cachedAd = WeakReference(p0)
        adapter.onLoad()
        BIDLog.d(TAG, "Ad loaded. slotId: $slotId")
    }

    override fun onNoAd(p0: IAdLoadingError, p1: MyTargetView) {
        BIDLog.d(TAG, "Failed to load ad. Error: ${p0.message} slotId: ($slotId)")
        adapter.onFailedToLoad(Error(p0.message))
    }

    override fun onShow(p0: MyTargetView) {
        BIDLog.d(TAG, "Ad displayed. slotId: ($slotId)")
        adapter.onDisplay()
    }

    override fun onClick(p0: MyTargetView) {
        BIDLog.d(TAG, "Ad click. slotId: ($slotId)")
        adapter.onClick()
    }

    override fun revenue(): Double? {
        return null
    }
}