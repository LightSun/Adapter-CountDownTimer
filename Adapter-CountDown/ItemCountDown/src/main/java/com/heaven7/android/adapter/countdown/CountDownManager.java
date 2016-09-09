package com.heaven7.android.adapter.countdown;

import android.os.CountDownTimer;
import android.support.annotation.NonNull;

import com.heaven7.adapter.QuickRecycleViewAdapter;
import com.heaven7.core.util.Logger;

import java.util.HashMap;

public final class CountDownManager<T extends ILeftTimeGetter> {

    private static final String TAG = "CountDownManager";

    private final HashMap<T, CountDownItem> mMap = new HashMap<>();
    private final long countDownInterval;
    private CountDownDataObserver<T> mObserver;

    public CountDownManager(long countDownInterval) {
        this.countDownInterval = countDownInterval;
    }

    public void attachCountDownTimer(final QuickRecycleViewAdapter<T> adapter) {
        detachCountDownTimer(adapter);
        adapter.registerAdapterDataObserver(mObserver = new CountDownDataObserver<T>(this) {
            @NonNull
            @Override
            protected T getItem(int position) {
                return adapter.getAdapterManager().getItemAt(position);
            }
            @Override
            protected int getItemSize() {
                return adapter.getAdapterManager().getItemSize();
            }
        });
    }

    public void detachCountDownTimer(QuickRecycleViewAdapter<T> adapter) {
        if (mObserver != null) {
            adapter.unregisterAdapterDataObserver(mObserver);
        }
    }

    public void addCountDownCallback( T bean, ICountDownCallback<T> callback) {
        final CountDownItem item = mMap.get(bean);
        item.mCallback = callback;
        if (!item.isFinish) {
            item.tick(item.mLeftTime);
        }
    }

    public void addCountDownTask(int pos, T bean) {
        final CountDownItem currItem = new CountDownItem(pos, bean, null);
        final CountDownItem preItem = mMap.put(bean, currItem);
        if (preItem != null) {
            preItem.mTimer.cancel();
            //TODO 维护 time ?
        }
        currItem.mTimer.start();
    }

    //取消，重新倒计时
    public void notifyTimeChanged(int pos, T mBean) {
        Logger.i(TAG, "notifyTimeChanged", "item = " + mBean);
        final CountDownItem item = mMap.remove(mBean);
        if (item != null) {
            item.mTimer.cancel();
            CountDownItem currItem = new CountDownItem(pos, mBean, item.mCallback);
            mMap.put(mBean, currItem);
            currItem.mTimer.start();
        }
    }

    public void notifyPositionChanged(int pos, T mBean) {
        Logger.i(TAG, "notifyTimeChanged", "item = " + mBean);
        final CountDownItem item = mMap.remove(mBean);
        if (item != null) {
            item.changePosition(pos);
        }
    }

    //返回剩余时间.
    public long cancel(T mBean) {
        Logger.i(TAG, "cancel", "item = " + mBean);
        final CountDownItem item = mMap.remove(mBean);
        if (item != null) {
            item.mTimer.cancel();
            return item.mLeftTime;
        }
        return -1;
    }

    public void cancelAll() {
        for (CountDownItem item : mMap.values()) {
            item.mTimer.cancel();
        }
        mMap.clear();
    }

    private class CountDownItem {
        final T mBean;
        final CountDownTimer mTimer;

        int mPos;
        ICountDownCallback<T> mCallback;

        long mLeftTime; //剩余时间
        boolean isFinish;

        public CountDownItem(int position, T bean, ICountDownCallback<T> callback) {
            this.mPos = position;
            this.mLeftTime = bean.getLeftTime();
            this.mBean = bean;
            this.mCallback = callback;
            this.mTimer = new CountDownTimer(mLeftTime, countDownInterval) {
                @Override
                public void onTick(long millisUntilFinished) {
                    tick(millisUntilFinished);
                }

                @Override
                public void onFinish() {
                    isFinish = true;
                    if (mCallback != null) {
                        mCallback.onFinish(mPos, mBean);
                    } else {
                        Logger.w(TAG, "onTick", "mCallback = null");
                    }
                }
            };
        }

        public void tick(long leftTime) {
            this.mLeftTime = leftTime;
            if (mCallback != null) {
                mCallback.onTick(mPos, mBean, leftTime);
            }
        }

        public void changePosition(int position) {
            this.mPos = position;
        }
    }

}
