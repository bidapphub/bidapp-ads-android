package com.bidapp.demo;

import androidx.annotation.NonNull;

import io.bidapp.sdk.AdInfo;
import io.bidapp.sdk.BannerView;
import io.bidapp.sdk.protocols.BIDBannerViewDelegate;

public class BannerViewDelegate implements BIDBannerViewDelegate {


    @Override
    public void adViewReadyToRefresh(BannerView adView, AdInfo adInfo) {
        System.out.println("App - adViewReadyToRefresh. AdView: " + adView + ", AdInfo: " + adInfo);
        adView.refreshAd();
    }

    @Override
    public void adViewDidDisplayAd(@NonNull BannerView adView, AdInfo adInfo) {
        System.out.println("App - didDisplayAd. AdView: " + adView + ", AdInfo: " + adInfo);
    }

    @Override
    public void adViewDidFailToDisplayAd(@NonNull BannerView adView, AdInfo adInfo, Error errors) {
        System.out.println("App - didFailToDisplayAd. AdView: " + adView + ", Error: " + errors.getLocalizedMessage());
    }

    @Override
    public void adViewClicked(@NonNull BannerView adView, AdInfo adInfo) {
        System.out.println("App - didClicked. AdView: " + adView + ", AdInfo: " + adInfo);
    }

}