package com.bidapp.demo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.bidapp.sdk.AdInfo;
import io.bidapp.sdk.BIDBannerViewDelegate;
import io.bidapp.sdk.BannerView;


public class BannerViewDelegate implements BIDBannerViewDelegate {



    @Override
    public void adViewDidLoadAd(@NonNull BannerView bannerView, @Nullable AdInfo adInfo) {
        System.out.println("App - adViewDidLoadAd. AdView: " + bannerView + ", AdInfo: " + adInfo);
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

    @Override
    public void allNetworksFailedToDisplayAd(@NonNull BannerView adView) {
        System.out.println("App - didClicked. AdView: " + adView);
    }


}