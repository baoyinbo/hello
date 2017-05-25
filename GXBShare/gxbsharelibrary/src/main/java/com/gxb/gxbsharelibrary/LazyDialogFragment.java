package com.gxb.gxbsharelibrary;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.gxb.gxbsharelibrary.lifecycle.IComponentContainer;
import com.gxb.gxbsharelibrary.lifecycle.LifeCycleComponent;
import com.gxb.gxbsharelibrary.lifecycle.LifeCycleComponentManager;
import com.robin.lazy.logger.LazyLogger;
import com.robin.lazy.net.http.HttpRequestManager;
import com.robin.lazy.net.http.RequestLifecycleContext;


import java.io.Serializable;
import java.util.ArrayList;

/**
 * Fragment基类
 *
 * @author 江钰锋
 * @version [版本号, 2014年6月19日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public abstract class LazyDialogFragment extends DialogFragment
        implements
        RequestLifecycleContext,
        IComponentContainer {
    /**
     * 返回监听
     */
    private FragmentResultCallback callback;
    /**
     * 上一个页面的请求码
     */
    private int mRequestCode;
    /**
     * 当前dialog窗口
     */
    private Window dialogWindow;

    /**
     * 生命周期管理者
     */
    private LifeCycleComponentManager mComponentContainer;

    /**
     * 标志位，标志已经初始化完成
     */
    private boolean isPrepared;

    /**
     * 是否第一次执行onResume()方法
     */
    private boolean isFirstResume = true;

    private Bundle bundle;

    @Override
    public void onAttach(Activity activity) {
        LazyLogger.d(" DialogFragment.onAttach() invoked!!");
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LazyLogger.d(" DialogFragment.onCreate() invoked!!");
        super.onCreate(savedInstanceState);
        // 如果setCancelable()中参数为true，若点击dialog覆盖不到的activity的空白或者按返回键，则进行cancel，状态检测依次onCancel()和onDismiss()。如参数为false，则按空白处或返回键无反应。缺省为true
        setCancelable(true);
        // 可以设置dialog的显示风格，如style为STYLE_NO_TITLE，将被显示title。遗憾的是，我没有在DialogFragment中找到设置title内容的方法。theme为0，表示由系统选择合适的theme。
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.Theme_dialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LazyLogger.d(" DialogFragment.onCreateView() invoked!!");
        Bundle mBundle = getArguments();
        if (mBundle != null) {
            mRequestCode = mBundle.getInt("requestCode", 0);
        }
        return inflater.inflate(getLayoutId(), container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        LazyLogger.d(" DialogFragment.onViewCreated() invoked!!");
        super.onViewCreated(view, savedInstanceState);
        initView(savedInstanceState);
        isPrepared = true;
        if (mComponentContainer != null) {
            mComponentContainer.onBecomesVisibleFromTotallyInvisible();
        }
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        Animation anim = null;
        try {
            anim = AnimationUtils.loadAnimation(getActivity(), nextAnim);
        } catch (NotFoundException e) {
            e.printStackTrace();
        } finally {
            if (anim != null) {
                anim.setAnimationListener(new AnimationListener() {

                    public void onAnimationStart(Animation animation) {
                        // 动画开始
                    }

                    public void onAnimationRepeat(Animation animation) {
                        // 动画循环
                    }

                    public void onAnimationEnd(Animation animation) {
                        // 动画结束
                        LazyDialogFragment.this.onAnimationEnd();
                    }
                });
            } else {
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        if (isPrepared) {
                            LazyDialogFragment.this.onAnimationEnd();
                        }
                    }
                }, 500);
            }
        }
        return anim;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        // 此方法只是在FragmentPagerAdapter或者FragmentStatePagerAdapter中显示或者不显示才会调用的方法
        if (isPrepared) {
            if (getUserVisibleHint()) {
                onVisible();
            } else {
                onInvisible();
            }
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        // 此方法在是在调用FragmentTransaction.hide或者FragmentTransaction.show的时候才会调用的方法
        if (isPrepared) {
            if (hidden) {
                onInvisible();
            } else {
                onVisible();
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        LazyLogger.d(" DialogFragment.onActivityCreated() invoked!!");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public LazyAppCompatActivity getCurrContext() {
        return (LazyAppCompatActivity) getActivity();
    }

    @Override
    public void addComponent(LifeCycleComponent component) {
        if (mComponentContainer == null) {
            mComponentContainer = new LifeCycleComponentManager();
        }
        mComponentContainer.addComponent(component);
    }

    @Override
    public void onStart() {
        LazyLogger.d(" DialogFragment.onStart() invoked!!");
        super.onStart();
    }

    @Override
    public void onResume() {
        LazyLogger.d(" DialogFragment.onResume() invoked!!");
        super.onResume();
        if (!isFirstResume) {
            if (mComponentContainer != null) {
                mComponentContainer.onBecomesVisibleFromPartiallyInvisible();
            }
        }
        if (isFirstResume) {
            isFirstResume = false;
        }
    }

    @Override
    public void onPause() {
        LazyLogger.d(" DialogFragment.onPause() invoked!!");
        super.onPause();
        if (mComponentContainer != null) {
            mComponentContainer.onBecomesPartiallyInvisible();
        }
    }

    @Override
    public void onStop() {
        LazyLogger.d(" DialogFragment.onStop() invoked!!");
        super.onStop();
        if (mComponentContainer != null) {
            mComponentContainer.onBecomesTotallyInvisible();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isPrepared = false;
        getHttpRequestManager().cancelConetxtRequest(this);
        if (mComponentContainer != null) {
            mComponentContainer.onDestroy();
        }
    }

    /**
     * 设置宽度是否填充整个屏幕
     *
     * @param isFull
     */
    protected void setDialogFullWidth(boolean isFull) {
        if (isFull) {
            if (dialogWindow == null) {
                dialogWindow = getDialog().getWindow();
            }
            DisplayMetrics dm = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            dialogWindow.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        }
    }

    /**
     * 设置dialog显示位置
     *
     * @param gravity
     */
    protected void setDialogWindowGravity(int gravity) {
        if (dialogWindow == null) {
            dialogWindow = getDialog().getWindow();
        }
        dialogWindow.setGravity(gravity);
    }

    /***
     * 获取当前窗口
     *
     * @return
     */
    public Window getDialogWindow() {
        if (dialogWindow == null) {
            dialogWindow = getDialog().getWindow();
        }
        return dialogWindow;
    }

    /***
     * fragemnt值回传
     *
     * @param resultCode
     * @param intent
     * @see [类、类#方法、类#成员]
     */
    private void fragmentCallbackResult(int resultCode, Intent intent) {
        if (callback != null) {
            callback.onFragmentResult(mRequestCode, resultCode, intent);
        }
    }

    /**
     * 设置副fragment回调接口
     *
     * @param callback
     * @see [类、类#方法、类#成员]
     */
    protected void setFragmentResultCallback(FragmentResultCallback callback) {
        this.callback = callback;
    }

    /***
     * 显示
     *
     * @param fragment
     * @param requestCode 请求码
     * @see [类、类#方法、类#成员]
     */
    public void showAllowingStateLoss(@NonNull LazyFragment fragment,int requestCode ) {
        setFragmentResultCallback(fragment);
        addValues("requestCode", requestCode);
        commitAddValues();
        showAllowingStateLoss(fragment);
    }

    /***
     * 显示
     *
     * @param fragment
     * @see [类、类#方法、类#成员]
     */
    public void showAllowingStateLoss(Fragment fragment) {
        showAllowingStateLoss(fragment, null);
    }

    /***
     * 显示
     *
     * @param fragment
     * @param tag
     * @see [类、类#方法、类#成员]
     */
    public void showAllowingStateLoss(Fragment fragment, String tag) {
        if (fragment == null || fragment.getActivity() == null || fragment.getActivity().isFinishing()) {
            LazyLogger.d(String.format(
                    "fragment [%s] is null or is finishing!", fragment));
            return;
        }
        showAllowingStateLoss(fragment.getFragmentManager(), tag);
    }

    /***
     * 显示
     *
     * @param activity
     * @param requestCode 请求码
     * @see [类、类#方法、类#成员]
     */
    public void showAllowingStateLoss(@NonNull LazyAppCompatActivity activity,int requestCode ) {
        setFragmentResultCallback(activity);
        addValues("requestCode", requestCode);
        commitAddValues();
        showAllowingStateLoss(activity);
    }

    /***
     * 显示
     *
     * @param activity
     * @see [类、类#方法、类#成员]
     */
    public void showAllowingStateLoss(FragmentActivity activity) {
        showAllowingStateLoss(activity, null);
    }

    /***
     * 显示
     *
     * @param activity
     * @param tag
     * @see [类、类#方法、类#成员]
     */
    public void showAllowingStateLoss(FragmentActivity activity, String tag) {
        if (activity == null || activity.isFinishing()) {
            LazyLogger.d(String.format(
                    "activity [%s] is null or is finishing!", activity));
            return;
        }
        showAllowingStateLoss(activity.getSupportFragmentManager(), tag);
    }

    /**
     * 显示
     * @param fragmentManager
     * @param tag
     */
    public void showAllowingStateLoss(@NonNull FragmentManager fragmentManager, @Nullable String tag) {
        if (fragmentManager == null) throw new NullPointerException("fragmentManager is null");
        try {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            if (!TextUtils.isEmpty(tag)) {
                transaction.add(this, tag);
            } else {
                transaction.add(this, getClass().getName());
            }
            transaction.commitAllowingStateLoss();
        } catch (Exception e) {
            LazyLogger.e("Activity has been destroyed", e);
        }
    }

    /**
     * 解除当前dialog
     * @param resultCode 结果码
     */
    protected void dismiss(int resultCode) {
        super.dismiss();
        fragmentCallbackResult(resultCode, null);
    }

    /**
     * 解除当前dialog
     * @param resultCode 结果码
     * @param intent 返回结果
     */
    protected void dismiss(int resultCode, Intent intent) {
        super.dismiss();
        fragmentCallbackResult(resultCode, intent);
    }

    /**
     * 解除当前dialog
     * @param resultCode 结果码
     */
    protected void dismissAllowingStateLoss(int resultCode) {
        super.dismissAllowingStateLoss();
        fragmentCallbackResult(resultCode, null);
    }

    /**
     * 解除当前dialog
     * @param resultCode 结果码
     * @param intent 返回结果
     */
    protected void dismissAllowingStateLoss(int resultCode, Intent intent) {
        super.dismissAllowingStateLoss();
        fragmentCallbackResult(resultCode, intent);
    }

    /**
     * 返回htt数据请求管理者
     *
     * @return
     * @see [类、类#方法、类#成员]
     */
    protected HttpRequestManager getHttpRequestManager() {
        if (getActivity() != null) {
            return ((LazyAppCompatActivity) getActivity()).getHttpRequestManager();
        }
        return null;
    }

    /**
     * 根据id查找view
     *
     * @param viewId
     * @return
     * @see [类、类#方法、类#成员]
     */
    public View findViewById(int viewId) {
        if (getView() != null) {
            return getView().findViewById(viewId);
        }
        return null;
    }

    /***
     * 设置layout
     *
     * @return
     * @see [类、类#方法、类#成员]
     */
    public abstract int getLayoutId();

    /**
     * 在viewpager中第一次初始化view后时才会调用
     *
     * @param savedInstanceState activity被意外回收所保存的数据
     * @see [类、类#方法、类#成员]
     */
    public abstract void initView(Bundle savedInstanceState);

    /***
     * 动画结束执行方法
     */
    public void onAnimationEnd() {

    }

    /**
     * view初始化后时可见的情况下调用(不管是否重新加载view都会调用)
     *
     * @see [类、类#方法、类#成员]
     */
    public void onVisible() {
        if (mComponentContainer != null) {
            mComponentContainer.onBecomesVisibleFromPartiallyInvisible();
        }
    }

    ;

    /**
     * view初始化后时不可见的情况下调用(不管是否重新加载view都会调用)
     *
     * @see [类、类#方法、类#成员]
     */
    public void onInvisible() {
        if (mComponentContainer != null) {
            mComponentContainer.onBecomesPartiallyInvisible();
        }
    }

    /**
     * 传值
     *
     * @param key
     * @param value 值
     * @see [类、类#方法、类#成员]
     */
    public void addValues(String key, Object value) {
        if (bundle == null) {
            bundle = new Bundle();
        }
        if (value instanceof Boolean) {
            bundle.putBoolean(key, (Boolean) value);
        } else if (value instanceof Byte) {
            bundle.putByte(key, (Byte) value);
        } else if (value instanceof Character) {
            bundle.putChar(key, (Character) value);
        } else if (value instanceof Short) {
            bundle.putShort(key, (Short) value);
        } else if (value instanceof Integer) {
            bundle.putInt(key, (Integer) value);
        } else if (value instanceof Integer) {
            bundle.putInt(key, (Integer) value);
        } else if (value instanceof Long) {
            bundle.putLong(key, (Long) value);
        } else if (value instanceof Float) {
            bundle.putFloat(key, (Float) value);
        } else if (value instanceof Double) {
            bundle.putDouble(key, (Double) value);
        } else if (value instanceof String) {
            bundle.putString(key, (String) value);
        } else if (value instanceof CharSequence) {
            bundle.putCharSequence(key, (CharSequence) value);
        } else if (value instanceof Serializable) {
            bundle.putSerializable(key, (Serializable) value);
        } else if (value instanceof Parcelable) {
            bundle.putParcelable(key, (Parcelable) value);
        } else if (value instanceof ArrayList) {
            ArrayList list = (ArrayList) value;
            if (list.get(0) instanceof Parcelable) {
                bundle.putParcelableArrayList(key, list);
            } else if (list.get(0) instanceof CharSequence) {
                bundle.putCharSequenceArrayList(key, list);
            } else if (list.get(0) instanceof Integer) {
                bundle.putIntegerArrayList(key, list);
            } else if (list.get(0) instanceof String) {
                bundle.putStringArrayList(key, list);
            } else {
                new Exception("传的参数错误");
            }
        } else if (value instanceof SparseArray) {
            if (((SparseArray) value).get(0) instanceof Parcelable) {
                bundle.putSparseParcelableArray(key, (SparseArray) value);
            } else {
                new Exception("传的参数错误");
            }
        } else if (value instanceof Parcelable[]) {
            bundle.putParcelableArray(key, (Parcelable[]) value);
        } else if (value instanceof boolean[]) {
            bundle.putBooleanArray(key, (boolean[]) value);
        } else if (value instanceof byte[]) {
            bundle.putByteArray(key, (byte[]) value);
        } else if (value instanceof char[]) {
            bundle.putCharArray(key, (char[]) value);
        } else if (value instanceof CharSequence[]) {
            bundle.putCharSequenceArray(key, (CharSequence[]) value);
        } else if (value instanceof double[]) {
            bundle.putDoubleArray(key, (double[]) value);
        } else if (value instanceof float[]) {
            bundle.putFloatArray(key, (float[]) value);
        } else if (value instanceof int[]) {
            bundle.putIntArray(key, (int[]) value);
        } else if (value instanceof long[]) {
            bundle.putLongArray(key, (long[]) value);
        } else if (value instanceof short[]) {
            bundle.putShortArray(key, (short[]) value);
        } else if (value instanceof String[]) {
            bundle.putStringArray(key, (String[]) value);
        } else {
            new Exception("传的参数错误");
        }
    }

    /**
     * 提交要接收的值
     *
     * @see [类、类#方法、类#成员]
     */
    public void commitAddValues() {
        if (bundle != null) {
            setArguments(bundle);
        }
    }

}
