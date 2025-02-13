package io.bidapp.networks.smaato

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.smaato.sdk.banner.ad.BannerAdSize
import com.smaato.sdk.banner.widget.BannerError
import com.smaato.sdk.banner.widget.BannerView
import com.smaato.sdk.core.SmaatoSdk
import com.smaato.sdk.core.ad.AdRequestParams
import com.smaato.sdk.ub.UBBannerSize
import com.smaato.sdk.ub.UBBid
import io.bidapp.sdk.AdFormat
import io.bidapp.sdk.bid.BIDNativeBidData
import io.bidapp.sdk.bid.BIDNativeBidNotify
import io.bidapp.sdk.bid.BidappBid
import io.bidapp.sdk.bid.BidappBidRequester
import io.bidapp.sdk.mediation.bid_completion
import io.bidapp.sdk.protocols.BIDBannerAdapterDelegateProtocol
import com.smaato.sdk.ub.UnifiedBidding.PrebidListener
import com.smaato.sdk.ub.UnifiedBidding.prebidBanner
import io.bidapp.sdk.BIDLog
import io.bidapp.sdk.protocols.BIDBannerAdapterProtocol
import java.lang.ref.WeakReference

@PublishedApi
internal class BIDSmaatoBanner(
    val adapter: BIDBannerAdapterProtocol,
    private val adSpaceId: String?,
    format: AdFormat
) : BIDBannerAdapterDelegateProtocol, BannerView.EventListener {
    val size = getBannerAdSize(format)
    private var adView: WeakReference<BannerView>? = null
    private var cachedAd: Boolean? = null
    private val TAG = "Banner Smaato"


    private fun getBannerAdSize(adFormat: AdFormat?) : BannerAdSize?{
        return when{
            adFormat?.isBanner_320x50 == true -> {
                BannerAdSize.XX_LARGE_320x50
            }
            adFormat?.isBanner_300x250 == true -> {
                BannerAdSize.MEDIUM_RECTANGLE_300x250
            }
            adFormat?.isBanner_728x90 == true -> {
                BannerAdSize.LEADERBOARD_728x90
            }
            else -> {
                null
            }
        }
    }

    override fun nativeAdView(): WeakReference<View>? {
        return WeakReference(adView?.get() as? View)
    }

    override fun isAdReady(): Boolean {
       return cachedAd ?: false
    }

    override fun destroy() {
        cachedAd = null
        adView?.get()?.destroy()
        adView?.clear()
        adView = null
    }

    override fun showOnView(view: WeakReference<View>, density: Float): Boolean {
        if (cachedAd == null){
            adapter.onFailedToDisplay(Error("Smaato banner not ready"))
        }
        return try {
            val weightAndHeight =
                when (size) {
                    BannerAdSize.XX_LARGE_320x50 -> arrayOf(
                        320,
                        50
                    )
                    BannerAdSize.MEDIUM_RECTANGLE_300x250 -> arrayOf(
                        300,
                        250
                    )
                    BannerAdSize.LEADERBOARD_728x90 -> arrayOf(
                        728,
                        90
                    )
                    else -> {
                        throw IllegalArgumentException("Smaato banner size incorrect")
                    }
                }

            (view.get() as FrameLayout).addView(
                adView!!.get(),
                0,
                ViewGroup.LayoutParams(
                    (weightAndHeight[0] * density).toInt(),
                    (weightAndHeight[1] * density).toInt()
                )
            )
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
        return true
    }

    override fun load(context: Any, bidAppBid: BidappBid?) {
        cachedAd = null
        if (adSpaceId.isNullOrEmpty()) {
            adapter.onFailedToLoad(Error("Smaato banner adSpaceId is null or empty"))
            return
        }
        if (context as? Context == null) {
            adapter.onFailedToLoad(Error("Smaato banner loading error. Context is null"))
            return
        }
        if (size == null) {
            adapter.onFailedToLoad(Error("Smaato banner size incorrect"))
            return
        }
        if (adView?.get() == null){
            adView = WeakReference(BannerView(context as Context))
        }
        adView?.get()?.setEventListener(this)
        if (bidAppBid != null){
            val ubBID = (bidAppBid.nativeBid as? UBBid)
            if (ubBID == null){
                adapter.onFailedToLoad(Error("Smaato banner ubBid is null or empty"))
                return
            }
            val uniqueID = ubBID.metadata[UBBid.MetadataKeys.UNIQUE_ID].toString()
            val adRequestParam = AdRequestParams.builder().setUBUniqueId(uniqueID).build()
            adView?.get()?.loadAd(adSpaceId, size, adRequestParam)
        }
        adView?.get()?.loadAd(adSpaceId, size)
    }

    override fun load(context: Any) {
        load(context, null)
    }

    override fun revenue(): Double? {
        return null
    }


    override fun onAdLoaded(bannerView: BannerView) {
        cachedAd = true
        adapter.onLoad()
        BIDLog.d(TAG, "Ad load")
    }

    override fun onAdFailedToLoad(bannerView: BannerView, bannerError: BannerError) {
        cachedAd = null
        BIDLog.d(TAG, "Ad failed to load. Error: ${bannerError.name}")
        adapter.onFailedToLoad(Error(bannerError.name))
    }

    override fun onAdImpression(bannerView: BannerView) {
        BIDLog.d(TAG, "Ad impression $adSpaceId")
        adapter.onDisplay()
    }

    override fun onAdClicked(bannerView: BannerView) {
        BIDLog.d(TAG, "Ad clicked ")
        adapter.onClick()
    }

    override fun onAdTTLExpired(bannerView: BannerView) {
        cachedAd = null
        BIDLog.d(TAG, "Ad TTL Expired $adSpaceId")
    }



    companion object
    {
        private fun getUBBannerSize(adFormat: AdFormat?) : UBBannerSize?{
            return when{
                adFormat?.isBanner_320x50 == true -> {
                    UBBannerSize.XX_LARGE_320x50
                }
                adFormat?.isBanner_300x250 == true -> {
                    UBBannerSize.MEDIUM_RECTANGLE_300x250
                }
                adFormat?.isBanner_728x90 == true -> {
                    UBBannerSize.LEADERBOARD_728x90
                }
                else -> {
                    null
                }
            }
        }

        fun bid(
            context: Context?,
            request: BidappBidRequester,
            adSpaceId: String?,
            appId: String?,
            accountId: String?,
            adFormat: AdFormat?,
            bidCompletion: bid_completion
        ) {
            val size = getUBBannerSize(adFormat)
            if (size == null){
                bidCompletion.invoke(null, Error("Smaato banner bid is failed size is incorrect"))
                return
            }
            BIDSmaatoSDK.COPPA?.let { SmaatoSdk.setCoppa(it) }
            val listener = PrebidListener { p0, p1 ->
                if (p1 != null) {
                    bidCompletion.invoke(null, Error(p1.error.name))
                    return@PrebidListener
                }
                val notifyListener = object : BIDNativeBidNotify {
                    override fun winNotify(secPrice: Double?, secBidder: String?) {}

                    override fun loseNotify(
                        firstPrice: Double?,
                        firstBidder: String?,
                        lossReason: Int?
                    ) {}
                }
                if (p0 == null){
                    bidCompletion.invoke(null, Error("Smaatto banner bid is null"))
                    return@PrebidListener
                }
                request.requestBidsWithCompletion(BIDNativeBidData(p0,p0.bidPrice ?: 0.0, notifyListener), adSpaceId, appId, accountId, adFormat, BIDSmaatoSDK.testMode, BIDSmaatoSDK.COPPA, bidCompletion)
            }
            if (adSpaceId == null){
                bidCompletion.invoke(null, Error("Smaatto banner adSpaceId is null"))
                return
            }
            prebidBanner(adSpaceId, size, listener)
        }

    }
}