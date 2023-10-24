package io.bidapp.demo;

import android.app.Activity;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;



public class BannerViewDelegate implements BIDBannerViewDelegate {
    private final WeakReference<Activity> activityWeakReference;

    public BannerViewDelegate(Activity activity) {
        this.activityWeakReference = new WeakReference<>(activity);
    }

    @Override
    public void adViewReadyToRefresh(BannerView adView, @NonNull AdInfo adInfo) {
        System.out.println("App - adViewReadyToRefresh. AdView: " + adView + ", AdInfo: " + adInfo);
        adView.refreshAd();
    }

    @Override
    public void adViewDidDisplayAd(@NonNull BannerView adView, @NonNull AdInfo adInfo) {
        System.out.println("App - didDisplayAd. AdView: " + adView + ", AdInfo: " + adInfo);
    }

    @Override
    public void adViewDidFailToDisplayAd(@NonNull BannerView adView, @NonNull AdInfo adInfo, Error errors) {
        System.out.println("App - didFailToDisplayAd. AdView: " + adView + ", Error: " + errors.getLocalizedMessage());
    }

    @Override
    public void adViewClicked(@NonNull BannerView adView, @NonNull AdInfo adInfo) {
        System.out.println("App - didClicked. AdView: " + adView + ", AdInfo: " + adInfo);
    }

    @Override
    public Activity activityForShowAd() {
        return activityWeakReference.get();
    }
}