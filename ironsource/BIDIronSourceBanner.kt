package io.bidapp.networks.ironsource

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.ironsource.mediationsdk.ISBannerSize
import com.ironsource.mediationsdk.IronSource
import com.ironsource.mediationsdk.demandOnly.ISDemandOnlyBannerLayout
import com.ironsource.mediationsdk.demandOnly.ISDemandOnlyBannerListener
import com.ironsource.mediationsdk.logger.IronSourceError
import io.bidapp.sdk.AdFormat
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.protocols.BIDBannerAdapterDelegateProtocol
import io.bidapp.sdk.protocols.BIDBannerAdapterProtocol
import java.lang.ref.WeakReference

@PublishedApi
internal class BIDIronSourceBanner(private val adapter: BIDBannerAdapterProtocol, private val instanceId: String?, format: AdFormat) :
    BIDBannerAdapterDelegateProtocol, ISDemandOnlyBannerListener {
    private val TAG = "Banner IronSource"
    private var adView: WeakReference<ISDemandOnlyBannerLayout>? = null
    private var cachedAd: Boolean = false
    private val bannerFormat = if (format.isBanner_320x50) ISBannerSize.BANNER
    else if (format.isBanner_300x250) ISBannerSize.RECTANGLE
    else {
        BIDLog.d(TAG, "Unsupported Iron Source banner format: ${format.name()}")
        null
    }

    override fun load(context: Any) {
        IronSource.destroyISDemandOnlyBanner(instanceId)
        cachedAd = false
        if (context as? Activity == null || bannerFormat == null){
            adapter.onFailedToLoad(Error("Banner IronSource loading error." + if (context as? Activity == null) "Activity" else "Format" + "is null"))
            return
        }
        if (instanceId.isNullOrEmpty()){
            adapter.onFailedToLoad(Error("IronSource banner adTag is null or empty"))
            return
        }
        adView = WeakReference(IronSource.createBannerForDemandOnly(context, bannerFormat))
        adView?.get()?.bannerDemandOnlyListener = this
        adView?.get().let { IronSource.loadISDemandOnlyBanner(context, it, instanceId) }
    }

    override fun revenue(): Double? {
        return null
    }

    override fun nativeAdView(): WeakReference<View>? {
        return WeakReference(adView?.get())
    }

    override fun isAdReady(): Boolean {
        return cachedAd
    }

    override fun destroy() {
        cachedAd = false
        IronSource.destroyISDemandOnlyBanner(instanceId)
        adView?.get()?.removeAllViews()
        adView?.clear()
        adView = null
    }

    override fun showOnView(view: WeakReference<View>, density: Float): Boolean {
            return try {
            val weightAndHeight: Array<Int> = when (bannerFormat) {
                ISBannerSize.RECTANGLE -> arrayOf(300, 250)
                ISBannerSize.BANNER -> arrayOf(320, 50)
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
            BIDLog.d(TAG, "Show on view is failed. Error : ${e.message}")
            false
        }
    }

    override fun waitForAdToShow(): Boolean {
        return true
    }

    override fun activityNeededForLoad(): Boolean {
        return true
    }

    override fun onBannerAdLoaded(p0: String?) {
        cachedAd = true
        adapter.onLoad()
        BIDLog.d(TAG, "Banner ad loaded: $instanceId")
    }

    override fun onBannerAdLoadFailed(p0: String?, p1: IronSourceError?) {
        cachedAd = false
        BIDLog.d(TAG, "failed to load banner ad $instanceId. Error: ${p1?.errorMessage ?: p0}")
        adapter.onFailedToLoad(Error(p1?.errorMessage ?: p0))
    }

    override fun onBannerAdShown(p0: String?) {
        adapter.onDisplay()
        BIDLog.d(TAG, "Banner ad displayed: $instanceId")
    }

    override fun onBannerAdClicked(p0: String?) {
        adapter.onClick()
        BIDLog.d(TAG, "Banner ad clicked: $instanceId")
    }

    override fun onBannerAdLeftApplication(p0: String?) {
        BIDLog.d(TAG, "Banner ad left application. $instanceId")
        adapter.onHide()
        cachedAd = false
    }
}