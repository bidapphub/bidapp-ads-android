package com.bidapp.demo

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TableLayout
import android.widget.TableRow
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.core.view.get
import io.bidapp.sdk.AdFormat
import io.bidapp.sdk.AdInfo
import io.bidapp.sdk.BIDBannerViewDelegate
import io.bidapp.sdk.BannerView
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.schedule
import kotlin.random.Random

class BannerActivity : AppCompatActivity(), BIDBannerViewDelegate {
    var progressBar: ProgressBar? = null
    var pendingBanners: MutableList<BannerView> = mutableListOf()
    var dataSource: MutableList<View> = mutableListOf()
    var generateBannerTimer: TimerTask? = null
    var removeBannerTimer: TimerTask? = null
    var tableLayout: TableLayout? = null
    var bannerCount = 0
    var isAfterDelete = false
    var allBannersArray = arrayListOf<BannerView?>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_banner)
        progressBar = findViewById(R.id.progressBar)
        tableLayout = findViewById(R.id.tablelay)
        progressBar?.max = 5
        addOneMoreBanner()
        scheduleRemoveOneBanner()
    }

    private fun addOneMoreBanner() {
        if (pendingBanners.size < 2) {
            val format =
                if (Random.nextInt(2) == 0) AdFormat.banner_320x50 else AdFormat.banner_300x250
            val banner = BannerView(this).banner(format)
            banner.setBannerViewDelegate(this)
            pendingBanners.add(banner)
            allBannersArray.add(banner)
            scheduleAddOneMoreBanner()
        }
    }

    private fun scheduleRemoveOneBanner() {
        removeBannerTimer?.cancel()
        removeBannerTimer = Timer().schedule(30000) {
            runOnUiThread {
                removeOneBanner()
            }
        }
    }


    private fun scheduleAddOneMoreBanner() {
        generateBannerTimer?.cancel()
        generateBannerTimer = Timer().schedule(10000) {
            runOnUiThread {
                addOneMoreBanner()
            }
        }
    }

    private fun reloadTable() {
        bannerCount = 0
        for (view in dataSource.reversed()) {
            if ((view as ConstraintLayout).childCount > 0) (view).removeViewAt(0)
        }
        dataSource.clear()
        isAfterDelete = false
        allBannersArray.forEach {
            it?.destroy()
        }
        tableLayout?.children?.forEach { child ->
            if (child is TableRow) {
                child.setBackgroundResource(R.drawable.cell_border)
            }
        }
    }


    private fun removeOneBanner() {
        var removedTheLastOne = false
        for (view in dataSource.reversed()) {
            if ((view as ConstraintLayout).childCount > 0) {
                (view).removeViewAt(0)
                (view.parent as TableRow).setBackgroundResource(R.drawable.background3)
                removedTheLastOne = (view == dataSource.first())
                break
            }
        }

        if (removedTheLastOne) {
            isAfterDelete = true
            dataSource.clear()
            addOneMoreBanner()
        }
        scheduleRemoveOneBanner()
    }


    fun addAdToSuperviewIfNeeded(adView: BannerView, adFormat: AdFormat) {
        if (adView.parent != null) return
        if (bannerCount > 0 && !isAfterDelete) {
            val count = bannerCount - 1
            val previosTargetRow: TableRow = tableLayout?.getChildAt(count) as TableRow
            previosTargetRow.setBackgroundResource(R.drawable.background2)
        }
        val targetRow: TableRow = tableLayout?.getChildAt(bannerCount++) as TableRow
        val constraintLayout = (targetRow.get(0) as ConstraintLayout)
        targetRow.setBackgroundResource(R.drawable.background1)

        adView.setBackgroundColor(Color.MAGENTA)
        val density = this.resources.displayMetrics.density
        if (adFormat.isBanner_320x50)
            constraintLayout.addView(adView, (320 * density).toInt(), (50 * density).toInt())
        else
            constraintLayout.addView(adView, (300 * density).toInt(), (250 * density).toInt())
        pendingBanners.remove(adView)
        dataSource.add(0, constraintLayout)
        isAfterDelete = false
        progressBar?.progress = bannerCount
        if (tableLayout?.childCount == bannerCount) reloadTable()
        if (dataSource.size == 5) {
            generateBannerTimer?.cancel()
            generateBannerTimer = null
        }
    }


    override fun adViewReadyToRefresh(adView: BannerView, adInfo: AdInfo?) {
        print("App - adViewReadyToRefresh. AdView: $adView, AdInfo: $adInfo")
        adInfo?.adFormat?.let { addAdToSuperviewIfNeeded(adView, adInfo.adFormat as AdFormat) }
        adView.refreshAd()
    }

    override fun adViewDidDisplayAd(adView: BannerView, adInfo: AdInfo?) {
        print("App - didDisplayAd. AdView: $adView, AdInfo: $adInfo")
    }

    override fun adViewDidFailToDisplayAd(adView: BannerView, adInfo: AdInfo?, errors: Error) {
        print("App - didFailToDisplayAd. AdView: $adView, Error:${errors.localizedMessage}")
    }

    override fun adViewClicked(adView: BannerView, adInfo: AdInfo?) {
        print("App - didClicked. AdView: $adView, AdInfo: $adInfo")
    }



    override fun onDestroy() {
        super.onDestroy()
        generateBannerTimer?.cancel()
        removeBannerTimer?.cancel()
        allBannersArray.forEach{
            it?.destroy()
        }
    }


}