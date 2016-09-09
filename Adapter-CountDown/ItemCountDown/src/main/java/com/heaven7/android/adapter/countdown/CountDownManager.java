package com.heaven7.android.adapter.countdown;

import android.os.CountDownTimer;

import java.util.HashMap;

/**
 * the count down manager.
 *
 * @param <T> the data
 */
public class CountDownManager<T extends ILeftTimeGetter> {

    private static final String TAG = "CountDownManager";

    private final HashMap<T, CountDownItem> mMap = new HashMap<>();
    private final long countDownInterval;
    //private CountDownDataObserver<T> mObserver;

    public CountDownManager(long countDownInterval) {
        this.countDownInterval = countDownInterval;
    }

   /* *//**
     * attach count down timer for adapter.
     * @param adapter the adapter
     *//*
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

    *//**
     * detach count down timer for adapter.
     * @param adapter the adapter
     *//*
    public void detachCountDownTimer(QuickRecycleViewAdapter<T> adapter) {
        if (mObserver != null) {
            adapter.unregisterAdapterDataObserver(mObserver);
            mObserver = null;
        }
    }
*/

    /**
     * bind a count down callback to timer , which is indicated by target bean data,
     *
     * @param bean     the data
     * @param callback the callback
     */
    public void setCountDownCallback(T bean, ICountDownCallback<T> callback) {
        if (bean instanceof ICountDownable && !((ICountDownable) bean).shouldCountDown()) {
            return;
        }
        final CountDownItem item = mMap.get(bean);
        item.setCallback(callback);
        item.tick(item.mLeftTime);
    }

    /**
     * add a count down task by target args.
     *
     * @param pos       the position
     * @param bean      the data
     * @param cancelOld true to cancel the old timer which is indicated by target data.
     */
    public void addCountDownTask(int pos, T bean, boolean cancelOld) {
        if (bean instanceof ICountDownable && !((ICountDownable) bean).shouldCountDown()) {
            return;
        }
        final CountDownItem oldItem = mMap.get(bean);
        // if handle the old item.
        boolean handled = false;
        //pre contains timer, check if cancel
        if (oldItem != null) {
            if (cancelOld) {
                oldItem.mTimer.cancel();
            } else {
                oldItem.changePosition(pos);
                handled = true;
            }
        }
        if (!handled) {
            final CountDownItem currItem = new CountDownItem(pos, bean, null);
            mMap.put(bean, currItem);
            currItem.mTimer.start();
        }
    }

    /**
     * notify the left time of raw data is changed.
     *
     * @param pos  the position
     * @param bean the data
     */
    public void notifyLeftTimeChanged(int pos, T bean) {
        if (bean instanceof ICountDownable && !((ICountDownable) bean).shouldCountDown()) {
            return;
        }
        Debugger.getDefault().i(TAG + "__notifyLeftTimeChanged", "pos = " + pos + " ,item = " + bean);
        final CountDownItem item = mMap.remove(bean);
        //cancel and start new timer
        if (item != null) {
            item.mTimer.cancel();
            CountDownItem currItem = new CountDownItem(pos, bean, item.mCallback);
            mMap.put(bean, currItem);
            currItem.mTimer.start();
        }
    }

    /**
     * notify the item position is changed
     *
     * @param pos  the position
     * @param bean the data
     */
    public void notifyPositionChanged(int pos, T bean) {
        if (bean instanceof ICountDownable && !((ICountDownable) bean).shouldCountDown()) {
            return;
        }
        Debugger.getDefault().i(TAG + "__notifyPositionChanged", "pos = " + pos + " ,item = " + bean);
        final CountDownItem item = mMap.get(bean);
        if (item != null) {
            item.changePosition(pos);
        }
    }

    /**
     * cancel the target timer which is indicated by target data.
     *
     * @param bean the data
     */
    public void cancel(T bean) {
        if (bean instanceof ICountDownable && !((ICountDownable) bean).shouldCountDown()) {
            return;
        }
        Debugger.getDefault().i(TAG + "__cancel", "item = " + bean);
        final CountDownItem item = mMap.remove(bean);
        if (item != null) {
            item.mTimer.cancel();
        }
    }

    /**
     * cancel all timers
     */
    public void cancelAll() {
        for (CountDownItem item : mMap.values()) {
            item.mTimer.cancel();
        }
        mMap.clear();
    }

    private class CountDownItem {
        private final T mBean;
        private final CountDownTimer mTimer;

        private int mPos;
        private ICountDownCallback<T> mCallback;

        private long mLeftTime;       //剩余时间

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
                    tick(0);
                }
            };
        }

        /**
         * tick the the interval.
         *
         * @param leftTime the left time or 0 means finished
         */
        public void tick(long leftTime) {
            this.mLeftTime = leftTime;
            if (mCallback != null) {
                mCallback.onTick(mPos, mBean, leftTime);
            }
        }

        public void changePosition(int position) {
            if (mCallback != null) {
                mCallback.onPositionChanged(mPos, position);
            }
            this.mPos = position;
        }

        public void setCallback(ICountDownCallback<T> callback) {
            this.mCallback = callback;
        }
    }

}
