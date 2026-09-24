package com.anythink.network.lm;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.adbid.media.AdbidAdInfo;
import com.adbid.media.AdbidBannerListener;
import com.adbid.media.AdbidError;
import com.adbid.media.ad.AdbidBannerView;
import com.anythink.banner.unitgroup.api.CustomBannerAdapter;
import com.anythink.core.api.ATAdConst;
import com.anythink.core.api.ATBiddingListener;
import com.anythink.core.api.ATBiddingResult;
import com.anythink.core.api.ATInitMediation;
import com.anythink.core.api.MediationInitCallback;

import java.util.Map;

public class LMATBannerAdapter extends CustomBannerAdapter {

    String mAppId;
    String mUnitId;
    AdbidBannerView mBannerView;

    boolean isC2SBidding;

    private void startLoadAd(Context context, Map<String, Object> serverMap) {
        AdbidBannerView bannerView;
        try {
            bannerView = new AdbidBannerView(context);
        } catch (Throwable e) {
            notifyATLoadFail("", "AdbidAdx banner create failed." + e.getMessage());
            return;
        }

        AdbidBannerListener listener = new AdbidBannerListener() {
            @Override
            public void onBannerLoad(AdbidAdInfo adbidAdInfo) {
                if (isC2SBidding) {
                    if (mBiddingListener != null) {
                        double price = 0;
                        if (adbidAdInfo != null) {
                            price = adbidAdInfo.getPrice();
                        }
                        LMATBiddingNotice biddingNotice = new LMATBiddingNotice(mBannerView);
                        mBiddingListener.onC2SBiddingResultWithCache(ATBiddingResult.success(price, System.currentTimeMillis() + "", biddingNotice, ATAdConst.CURRENCY.RMB_CENT), null);
                    }
                } else {
                    if (mLoadListener != null) {
                        mLoadListener.onAdCacheLoaded();
                    }
                }
            }

            @Override
            public void onBannerFail(String adUnitId, AdbidError adbidError) {
                mBannerView = null;
                String msg = adbidError != null ? adbidError.getMessage() : "AdbidAdx banner load failed.";
                notifyATLoadFail("", msg);
            }

            @Override
            public void onBannerShow(AdbidAdInfo adbidAdInfo) {
                if (mImpressionEventListener != null) {
                    mImpressionEventListener.onBannerAdShow();
                }
            }

            @Override
            public void onBannerClose(AdbidAdInfo adbidAdInfo) {
                if (mImpressionEventListener != null) {
                    mImpressionEventListener.onBannerAdClose();
                }
            }

            @Override
            public void onBannerClicked(AdbidAdInfo adbidAdInfo) {
                if (mImpressionEventListener != null) {
                    mImpressionEventListener.onBannerAdClicked();
                }
            }
        };

        bannerView.setUnitId(mUnitId);
        try {
            if (serverMap != null) {
                int width = ATInitMediation.getIntFromMap(serverMap, "nw_ad_width", 0);
                int height = ATInitMediation.getIntFromMap(serverMap, "nw_ad_height", 0);
                if (width > 0 && height > 0) {
                    bannerView.setAdSize(width, height);
                }
            }
        } catch (Throwable ignored) {
        }
        bannerView.setBannerAdListener(listener);

        if (bannerView.getLayoutParams() == null) {
            bannerView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        mBannerView = bannerView;

        try {
            bannerView.loadAd();
        } catch (Throwable e) {
            mBannerView = null;
            notifyATLoadFail("", "AdbidAdx banner load failed." + e.getMessage());
        }
    }

    @Override
    public View getBannerView() {
        return mBannerView;
    }

    @Override
    public String getNetworkName() {
        return LMATInitManager.getInstance().getNetworkName();
    }

    @Override
    public void loadCustomNetworkAd(final Context context, final Map<String, Object> serverExtra, Map<String, Object> localExtra) {
        initRequestParams(serverExtra, localExtra);

        if (TextUtils.isEmpty(mAppId) || TextUtils.isEmpty(mUnitId)) {
            notifyATLoadFail("", "AdbidAdx appid or unitId is empty.");
            return;
        }

        LMATInitManager.getInstance().initSDK(context, serverExtra, new MediationInitCallback() {
            @Override
            public void onSuccess() {
                startLoadAd(context, serverExtra);
            }

            @Override
            public void onFail(String errorMsg) {
                notifyATLoadFail("", errorMsg);
            }
        });
    }

    private void initRequestParams(Map<String, Object> serverExtra, Map<String, Object> localExtra) {
        mAppId = ATInitMediation.getStringFromMap(serverExtra, "app_id");
        mUnitId = ATInitMediation.getStringFromMap(serverExtra, "slot_id");
    }

    @Override
    public void destory() {
        if (mBannerView != null) {
            try {
                mBannerView.destroy();
            } catch (Throwable ignored) {
            }
            mBannerView = null;
        }
    }

    @Override
    public String getNetworkPlacementId() {
        return mUnitId;
    }

    @Override
    public String getNetworkSDKVersion() {
        return LMATInitManager.getInstance().getNetworkVersion();
    }

    @Override
    public int baseOnAdapterBridgeVersion() {
        return LMATInitManager.getInstance().getAdapterBridgeVersion();
    }

    @Override
    public ATInitMediation getMediationInitManager() {
        return LMATInitManager.getInstance();
    }

    @Override
    public boolean startBiddingRequest(Context context, Map<String, Object> serverExtra, Map<String, Object> localExtra, ATBiddingListener biddingListener) {
        isC2SBidding = true;
        loadCustomNetworkAd(context, serverExtra, localExtra);
        return true;
    }
}