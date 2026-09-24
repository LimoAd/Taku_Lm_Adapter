package com.anythink.network.lm;

import com.adbid.media.nativeAd.AdbidNativeAppInfo;
import com.anythink.core.api.ATAdAppInfo;

public class LMATDownloadAppInfo extends ATAdAppInfo {
    public String publisher;
    public String appVersion;
    public String appPrivacyLink;
    public String appPermissionLink;
    public String appName;

    public LMATDownloadAppInfo(AdbidNativeAppInfo nativeAppInfo) {
        publisher = nativeAppInfo.getPublisher();
        appVersion = nativeAppInfo.getAppVersion();
        appPrivacyLink = nativeAppInfo.getAppPrivacyUrl();
        appPermissionLink = nativeAppInfo.getAppPermissonUrl();
        appName = nativeAppInfo.getAppName();
    }

    @Override
    public String getPublisher() {
        return publisher;
    }

    @Override
    public String getAppVersion() {
        return appVersion;
    }

    @Override
    public String getAppPrivacyUrl() {
        return appPrivacyLink;
    }

    @Override
    public String getAppPermissonUrl() {
        return appPermissionLink;
    }

    @Override
    public String getAppName() {
        return appName;
    }

    @Override
    public String getAppPackageName() {
        return "";
    }

    @Override
    public String getDownloadCount() {
        return "";
    }

    @Override
    public long getAppSize() {
        return 0;
    }
}