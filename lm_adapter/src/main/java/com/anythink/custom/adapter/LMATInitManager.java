package com.anythink.custom.adapter;

import android.content.Context;
import android.text.TextUtils;

import com.adbid.media.AdbidError;
import com.adbid.sdk.AdbidCustomController;
import com.adbid.sdk.AdbidInitConfig;
import com.adbid.sdk.AdbidSdk;
import com.adbid.sdk.AdbidSdkInitListener;
import com.anythink.core.api.ATAdConst;
import com.anythink.core.api.ATInitMediation;
import com.anythink.core.api.ATSDK;
import com.anythink.core.api.MediationInitCallback;
import com.anythink.core.api.bridge.ATAdapterBridgeConst;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class LMATInitManager extends ATInitMediation {

    public static final String TAG = LMATInitManager.class.getSimpleName();
    private volatile static LMATInitManager sInstance;
    int personAdStatus = 0;
    private boolean mHasInit;
    private String mLocalInitAppId;
    private final AtomicBoolean mIsIniting;
    private final Object mLock = new Object();
    private List<MediationInitCallback> mListeners;

    private LMATInitManager() {
        mIsIniting = new AtomicBoolean(false);
    }

    //6.5.50以上版本增加该方法返回
    @Override
    public int getAdapterBridgeVersion() {
        return ATAdapterBridgeConst.ADAPTER_BRIDGE_VERSIONCODE;
    }

    public static LMATInitManager getInstance() {
        if (sInstance == null) {
            synchronized (LMATInitManager.class) {
                if (sInstance == null) sInstance = new LMATInitManager();
            }
        }
        return sInstance;
    }

    public synchronized void initSDK(Context context, Map<String, Object> serviceExtras) {
        initSDK(context, serviceExtras, null);
    }

    @Override
    public synchronized void initSDK(Context context, Map<String, Object> serviceExtras, MediationInitCallback onInitCallback) {
        try {
            personAdStatus = ATSDK.getPersionalizedAdStatus();
        } catch (Throwable ignored) {

        }

        if (mHasInit) {
            if (onInitCallback != null) {
                onInitCallback.onSuccess();
            }
            return;
        }

        synchronized (mLock) {

            if (mIsIniting.get()) {
                if (onInitCallback != null) {
                    mListeners.add(onInitCallback);
                }
                return;
            }

            if (mListeners == null) {
                mListeners = new ArrayList<>();
            }

            mIsIniting.set(true);
        }

        String app_id = getStringFromMap(serviceExtras, "app_id");

        if (onInitCallback != null) {
            synchronized (mLock) {
                mListeners.add(onInitCallback);
            }
        }
        if (serviceExtras.containsKey(ATInitMediation.KEY_LOCAL)) {
            mLocalInitAppId = app_id;
        } else if (mLocalInitAppId != null && !TextUtils.equals(mLocalInitAppId, app_id)) {
            checkToSaveInitData(getNetworkName(), serviceExtras, mLocalInitAppId);
            mLocalInitAppId = null;
        }

        try {
            AdbidInitConfig.Builder builder = AdbidInitConfig.builder(app_id);
            builder.addCustomController(new AdbidCustomController() {
                @Override
                public boolean isSupportPersonalized() {
                    return personAdStatus != ATAdConst.PRIVACY.PERSIONALIZED_LIMIT_STATUS;
                }

                @Override
                public boolean isCanUsePhoneState() {
                    return true;
                }

                @Override
                public boolean isCanUseLocation() {
                    return true;
                }

                @Override
                public boolean isCanUseWifiState() {
                    return true;
                }

                @Override
                public boolean isCanUseOaid() {
                    return true;
                }

                @Override
                public String getDevOaid() {
                    return "";
                }

                @Override
                public boolean isCanUseAppList() {
                    return false;
                }

                @Override
                public List getAppList() {
                    return null;
                }

                @Override
                public boolean isCanUseAndroidId() {
                    return true;
                }

                @Override
                public String getAndroidId() {
                    return "";
                }

                @Override
                public boolean isCanUseMacAddress() {
                    return true;
                }

                @Override
                public String getMacAddress() {
                    return "";
                }

                @Override
                public boolean isCanUseWriteExternal() {
                    return true;
                }

                @Override
                public boolean isCanUseShakeAd() {
                    return true;
                }

                @Override
                public boolean isCanUseRecordAudio() {
                    return true;
                }

                @Override
                public String getDevImei() {
                    return "";
                }

                @Override
                public String[] getDevImeiList() {
                    return null;
                }

                @Override
                public com.adbid.sdk.AdbidLocation getLocation() {
                    return null;
                }

                @Override
                public boolean isCanUseIP() {
                    return true;
                }

                @Override
                public String getIP() {
                    return "";
                }
            });
            AdbidInitConfig config = builder.build();

            AdbidSdk.getInstance(context.getApplicationContext()).initialize(config, new AdbidSdkInitListener() {
                @Override
                public void onSdkInitCallback(boolean isSuccess, AdbidError error) {
                    if (isSuccess) {
                        mHasInit = true;
                        callbackResult(true, null, null);
                    } else {
                        String msg = error != null ? error.getMessage() : "AdbidAdx initSDK failed.";
                        callbackResult(false, "", msg);
                    }
                }
            });
        } catch (Throwable e) {
            callbackResult(false, "", "AdbidAdx initSDK failed." + e.getMessage());
        }
    }

    private void callbackResult(boolean success, String errorCode, String errorMsg) {
        synchronized (mLock) {
            int size = mListeners.size();
            MediationInitCallback initListener;
            for (int i = 0; i < size; i++) {
                initListener = mListeners.get(i);
                if (initListener != null) {
                    if (success) {
                        initListener.onSuccess();
                    } else {
                        initListener.onFail(errorCode + " | " + errorMsg);
                    }
                }
            }
            mListeners.clear();

            mIsIniting.set(false);
        }
    }

    @Override
    public String getNetworkName() {
        return "AdbidSdk";
    }

    @Override
    public String getNetworkVersion() {
        return AdbidSdk.VERSION;
    }

    @Override
    public String getNetworkSDKClass() {
        return "com.adbid.sdk.AdbidSdk";
    }
}