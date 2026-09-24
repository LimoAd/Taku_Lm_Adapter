package com.anythink.network.lm;

import android.content.Context;
import android.text.TextUtils;

import com.adbid.media.AdbidError;
import com.adbid.sdk.AdbidInitConfig;
import com.adbid.sdk.AdbidSdk;
import com.adbid.sdk.AdbidSdkInitListener;
import com.anythink.core.api.ATAdConst;
import com.anythink.core.api.ATInitMediation;
import com.anythink.core.api.ATSDK;
import com.anythink.core.api.MediationInitCallback;
import com.anythink.core.api.bridge.ATAdapterBridgeConst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class LMATInitManager extends ATInitMediation {

    public static final String TAG = LMATInitManager.class.getSimpleName();

    /** 适配器版本，对齐官方适配器的 "SDK版本.适配器patch" 命名规则 */
    private static final String ADAPTER_VERSION = "2.1.0.24.1.0";

    private volatile static LMATInitManager sInstance;
    int personAdStatus = 0;
    private boolean mHasInit;
    private String mInitAppId;
    private String mLocalInitAppId;
    private final AtomicBoolean mIsIniting;
    private final Object mLock = new Object();
    private List<MediationInitCallback> mListeners;

    /** App 注入的自定义隐私控制器，参考 KSATCustomController 的设计 */
    private LMATCustomController mCustomController;
    /** 未注入时使用的默认隐私控制器 */
    private final LMATCustomController mDefaultCustomController = new LMATCustomController() {
    };

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

    /**
     * 注入自定义隐私控制器（参考官方 KSATInitManager#setKSATCustomController）。
     * 仅在初始化前调用生效，App 可继承 LMATCustomController 并覆写任意隐私开关。
     */
    public void setLMATCustomController(LMATCustomController customController) {
        if (customController != null) {
            mCustomController = customController;
        }
    }

    public synchronized void initSDK(Context context, Map<String, Object> serviceExtras) {
        initSDK(context, serviceExtras, null);
    }

    @Override
    public synchronized void initSDK(Context context, Map<String, Object> serviceExtras, MediationInitCallback onInitCallback) {
        int personAdStatus = ATAdConst.PRIVACY.PERSIONALIZED_ALLOW_STATUS;
        try {
            personAdStatus = ATSDK.getPersionalizedAdStatus();
        } catch (Throwable ignored) {
        }
        this.personAdStatus = personAdStatus;
        // 将 TopOn 全局个性化状态同步给隐私控制器
        getCustomController().updatePersonAdStatus(personAdStatus);

        String app_id = getStringFromMap(serviceExtras, "app_id");

        // 快速路径：已用相同 app_id 初始化成功过，直接回调成功（参考官方适配器）
        if (mHasInit && TextUtils.equals(app_id, mInitAppId)) {
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

            if (onInitCallback != null) {
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
            builder.addCustomController(getCustomController());
            AdbidInitConfig config = builder.build();

            AdbidSdk.getInstance(context.getApplicationContext()).initialize(config, new AdbidSdkInitListener() {
                @Override
                public void onSdkInitCallback(boolean isSuccess, AdbidError error) {
                    if (isSuccess) {
                        mHasInit = true;
                        mInitAppId = app_id;
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

    private LMATCustomController getCustomController() {
        return mCustomController != null ? mCustomController : mDefaultCustomController;
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
    public String getAdapterVersion() {
        return ADAPTER_VERSION;
    }

    @Override
    public String getNetworkSDKClass() {
        return "com.adbid.sdk.AdbidSdk";
    }

    /**
     * 插件类状态检测（参考官方适配器 KSATInitManager#getPluginClassStatus）。
     * AdbidAdx SDK 无可选插件类，其运行所需类全部由宿主 app 模块的
     * adbid_sdk.aar（含 AndroidX 依赖）提供。
     */
    public Map<String, Boolean> getPluginClassStatus() {
        return new HashMap<>();
    }

    /**
     * 资源状态检测（参考官方适配器 KSATInitManager#getResourceStatus）。
     * AdbidAdx SDK 的资源随宿主 app 的 aar 合并进 APK，无额外需检查的资源。
     */
    public List getResourceStatus() {
        return new ArrayList();
    }
}
