package com.anythink.network.lm;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.adbid.media.AdbidError;
import com.adbid.media.nativeAd.AdbidNativeAd;
import com.adbid.media.nativeAd.AdbidNativeAdView;
import com.adbid.media.nativeAd.AdbidNativeAppInfo;
import com.adbid.media.nativeAd.AdbidNativeEventListener;
import com.anythink.nativead.api.ATNativePrepareExInfo;
import com.anythink.nativead.api.ATNativePrepareInfo;
import com.anythink.nativead.api.NativeAdInteractionType;
import com.anythink.nativead.unitgroup.api.CustomNativeAd;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class LMATNativeAd extends CustomNativeAd {
    private static final String TAG = LMATNativeAd.class.getSimpleName();
    WeakReference<Context> mContext;
    Context mApplicationContext;

    AdbidNativeAd mAdbidNativeAd;

    int mVideoAutoPlay;
    int mVideoDuration;

    int mMuteApiSet = 0;//0:not set,1:set mute,2:not mute

    protected LMATNativeAd(Context context, AdbidNativeAd adbidNativeAd, int videoMuted) {

        mApplicationContext = context.getApplicationContext();
        mContext = new WeakReference<>(context);

        mVideoAutoPlay = 1;
        mVideoDuration = 0;
        mAdbidNativeAd = adbidNativeAd;

        setAdData(adbidNativeAd);

        try {
            setVideoMute(videoMuted == 1);
        } catch (Throwable ignored) {

        }
    }

    private void setAdData(AdbidNativeAd adbidNativeAd) {
        try {
            setTitle(adbidNativeAd.getTitle());
        } catch (Throwable ignored) {
        }
        try {
            setDescriptionText(adbidNativeAd.getDescriptionText());
        } catch (Throwable ignored) {
        }
        try {
            setIconImageUrl(adbidNativeAd.getIconImgUrl());
        } catch (Throwable ignored) {
        }
        try {
            setMainImageUrl(adbidNativeAd.getMainImageUrl());
        } catch (Throwable ignored) {
        }
        try {
            List<String> imgList = adbidNativeAd.getImageUrlList();
            if (imgList != null) {
                setImageUrlList(imgList);
            }
        } catch (Throwable ignored) {
        }
        try {
            setAdFrom(adbidNativeAd.getAdFrom());
        } catch (Throwable ignored) {
        }
        try {
            setCallToActionText(adbidNativeAd.getCallToAction());
        } catch (Throwable ignored) {
        }
        try {
            setVideoDuration(adbidNativeAd.getVideoDuration() / 1000D);
        } catch (Throwable ignored) {
        }
        try {
            setNativeInteractionType(adbidNativeAd.isDownload() ? NativeAdInteractionType.APP_DOWNLOAD_TYPE : NativeAdInteractionType.UNKNOW);
        } catch (Throwable ignored) {
        }

        try {
            AdbidNativeAppInfo appInfo = adbidNativeAd.getNativeAppInfo();
            if (adbidNativeAd.isDownload() && appInfo != null) {
                setAdAppInfo(new LMATDownloadAppInfo(appInfo));
            }
        } catch (Throwable ignored) {
        }

        try {
            if (adbidNativeAd.getAdMaterialType() != null
                    && "video".equalsIgnoreCase(adbidNativeAd.getAdMaterialType().name())) {
                mAdSourceType = NativeAdConst.VIDEO_TYPE;
            } else {
                mAdSourceType = NativeAdConst.IMAGE_TYPE;
            }
        } catch (Throwable ignored) {
            mAdSourceType = NativeAdConst.IMAGE_TYPE;
        }

        adbidNativeAd.setEventListener(new AdbidNativeEventListener() {
            @Override
            public void onImpression(AdbidNativeAdView adbidNativeAdView, com.adbid.media.AdbidAdInfo adbidAdInfo) {
                notifyAdImpression();
            }

            @Override
            public void onNativeAdClick(AdbidNativeAdView adbidNativeAdView, com.adbid.media.AdbidAdInfo adbidAdInfo) {
                Log.i(TAG, "onNativeAdClick....");
                notifyAdClicked();
            }

            @Override
            public void onAdClose(AdbidNativeAdView adbidNativeAdView) {

            }
        });
    }

    com.adbid.media.AdbidAdInfo getAdbidAdInfo() {
        if (mAdbidNativeAd != null) {
            return mAdbidNativeAd.getAdbidAdInfo();
        }
        return null;
    }

    @Override
    public View getAdMediaView(Object... object) {
        if (mAdbidNativeAd != null) {
            try {
                View mediaView = mAdbidNativeAd.getMediaView();
                if (mediaView != null) {
                    return mediaView;
                }
            } catch (Throwable ignored) {
            }
        }
        return super.getAdMediaView(object);
    }

    @Override
    public boolean isNativeExpress() {
        return false;
    }

    AdbidNativeAdView mAdbidNativeAdView;

    @Override
    public ViewGroup getCustomAdContainer() {
        if (mAdbidNativeAd != null) {
            try {
                mAdbidNativeAdView = new AdbidNativeAdView(mApplicationContext);
            } catch (Throwable t) {
                mAdbidNativeAdView = null;
            }
        }
        return mAdbidNativeAdView;
    }

    @Override
    public void prepare(View view, ATNativePrepareInfo nativePrepareInfo) {
        if (mAdbidNativeAd == null || mAdbidNativeAdView == null) {
            return;
        }

        List<View> clickViewList = nativePrepareInfo.getClickViewList();

        if (clickViewList == null || clickViewList.isEmpty()) {
            clickViewList = new ArrayList<>();
            fillChildView(view, clickViewList);
        }

        List<View> creativeClickViews = new ArrayList<>();
        if (nativePrepareInfo instanceof ATNativePrepareExInfo) {
            List<View> creativeClickViewList = ((ATNativePrepareExInfo) nativePrepareInfo).getCreativeClickViewList();
            if (creativeClickViewList != null) {
                creativeClickViews.addAll(creativeClickViewList);
            }
        }

        View clickView = null;
        if (!creativeClickViews.isEmpty()) {
            clickView = creativeClickViews.get(0);
        } else if (!clickViewList.isEmpty()) {
            clickView = clickViewList.get(0);
        }

        try {
            mAdbidNativeAd.registerViews(mAdbidNativeAdView, clickViewList, clickView);
        } catch (Throwable ignored) {
        }
    }

    private void fillChildView(View parentView, List<View> childViews) {
        if (parentView instanceof ViewGroup && parentView != mAdbidNativeAdView) {
            ViewGroup viewGroup = (ViewGroup) parentView;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                fillChildView(child, childViews);
            }
        } else {
            childViews.add(parentView);
        }
    }

    @Override
    public void clear(View view) {
        try {
            if (mAdbidNativeAdView != null) {
                mAdbidNativeAdView.clear();
            }
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void onResume() {
        try {
            if (mAdbidNativeAd != null) {
                mAdbidNativeAd.startVideo();
            }
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void resumeVideo() {
        try {
            if (mAdbidNativeAd != null) {
                mAdbidNativeAd.startVideo();
            }
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void pauseVideo() {
        try {
            if (mAdbidNativeAd != null) {
                mAdbidNativeAd.pauseVideo();
            }
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void setVideoMute(boolean isMute) {
        mMuteApiSet = isMute ? 1 : 2;
        try {
            if (mAdbidNativeAd != null) {
                mAdbidNativeAd.setMuted(isMute);
            }
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        try {
            if (mAdbidNativeAd != null) {
                mAdbidNativeAd.setEventListener(null);
                mAdbidNativeAd.destroy();
                mAdbidNativeAd = null;
            }
        } catch (Throwable ignored) {
        }

        mApplicationContext = null;
        if (mContext != null) {
            mContext.clear();
            mContext = null;
        }
        mAdbidNativeAdView = null;
    }
}