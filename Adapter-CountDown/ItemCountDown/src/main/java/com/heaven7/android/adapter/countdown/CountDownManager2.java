package com.heaven7.android.adapter.countdown;

import android.os.Message;
import android.os.SystemClock;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.heaven7.adapter.AdapterManager;
import com.heaven7.adapter.QuickRecycleViewAdapter;
import com.heaven7.core.util.AsyncManager;
import com.heaven7.core.util.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * the countdown manager which only support for RecyclerView adapter, also delete item in RecyclerView adapter.
 * Created by heaven7 on 2017/1/11.
 *
 * @since 1.1.0
 */

public class CountDownManager2<T extends ILeftTimeGetter> extends AsyncManager {

    private static final String TAG = CountDownManager2.class.getSimpleName();
    private static final int MSG = 1;

    private final Map<T, ItemInfo> mMap = new ConcurrentHashMap<>();
    private final AdapterObserverImpl mImpl = new AdapterObserverImpl();

    private final ArrayList<Map.Entry<T, ItemInfo>> mTempList = new ArrayList<>();
    private final ArrayList<Integer> mRemovedPositions = new ArrayList<>();

    private final long mCountdownInterval;
    /**
     * the max iteration count .determined by {@link #mCountdownInterval}. if {@link #mIterationCount} reach
     * {@link #mMaxIterationCount}. the iteration will be quit. but back thread is also active.
     */
    private final long mMaxIterationCount;
    private QuickRecycleViewAdapter<T> mAdapter;

    /**
     * indicate if is iteration the countdown. it will be reset to zero after {@link #detach()}.
     */
    private boolean mStartedIteration = false;
    private volatile boolean mDetached = false;
    /**
     * should just the position .
     */
    private boolean mAdjustPosition = false;
    /**
     * the count of iteration. it will be reset to zero after {@link #detach()}.
     */
    private long mIterationCount;
    /**
     * indicate whether the countdown update the view or not, default is true.
     * if set to false , it will only update data, but not update view/ui .
     */
    private volatile boolean mEnableUpdateView = true;

    /**
     * create a CountDownManager2.
     *
     * @param countdownInterval the interval time of countdown.
     */
    public CountDownManager2(long countdownInterval) {
        this.mCountdownInterval = countdownInterval;
        this.mMaxIterationCount = Long.MAX_VALUE / countdownInterval;
    }

    /**
     * create a CountDownManager.
     *
     * @param countdownInterval  the interval time of countdown.
     * @param mMaxIterationCount the max iteration count. if reached it will be auto detach.
     */
    public CountDownManager2(long countdownInterval, int mMaxIterationCount) {
        this.mCountdownInterval = countdownInterval;
        long max = Long.MAX_VALUE / countdownInterval;
        this.mMaxIterationCount = Math.max(mMaxIterationCount, max);
    }

    /**
     * attach the adapter to  this , if the work thread is not prepared. it will auto prepare for count down task.
     *
     * @param adapter the adapter
     */
    public synchronized void attach(QuickRecycleViewAdapter<T> adapter) {
        prepare();
        detach();
        this.mDetached = false;
        this.mAdapter = adapter;
        adapter.registerAdapterDataObserver(mImpl);
        adapter.getAdapterManager().registerAdapterDataRemoveObserver(mImpl);
    }

    /**
     * set if enabled update the view or not.
     * @param enable true to enable.
     */
    public void setEnableUpdateView(boolean enable) {
        this.mEnableUpdateView = enable;
    }

    /**
     *  whether the update view enabled or not.
     * @return whether the update view enabled or not.
     */
    public boolean isEnabledUpdateView() {
        return mEnableUpdateView;
    }

    /**
     * detach the adapter and stop iteration. but it can start iteration in later.
     * so the background thread is also alive. see {@link android.os.HandlerThread}.
     */
    public synchronized void detach() {
        this.mDetached = true;
        this.mMap.clear();
        mStartedIteration = false;
        mIterationCount = 0;
        if (mAdapter != null) {
            mAdapter.unregisterAdapterDataObserver(mImpl);
            mAdapter.getAdapterManager().unregisterAdapterDataRemoveObserver(mImpl);
            mAdapter = null;
        }
    }

    /**
     * if is detached. it also be true if call this after {@link #detach()} or {@link #destroy()} .
     * but if you call {@link #attach(QuickRecycleViewAdapter)} it will return false.
     *
     * @return true if is detached.
     */
    public boolean isDetached() {
        return mDetached;
    }

    public long getCountdownIntervalTime() {
        return mCountdownInterval;
    }

    public long getTotalIntervalTime() {
        return mIterationCount * mCountdownInterval;
    }

    /**
     * get the iteration count from the last call {@link #attach(QuickRecycleViewAdapter)}.
     *
     * @return the iteration count
     */
    public long getIterationCount() {
        return mIterationCount;
    }

    /**
     * get the max iteration count.
     *
     * @return the max iteration count. it's determined by the {@link #getCountdownIntervalTime()}.
     */
    public long getMaxIterationCount() {
        return mMaxIterationCount;
    }

    /**
     * destroy all . also contains the thread of background.
     */
    public void destroy() {
        quit();
        detach();
    }

    private synchronized void startLoop() {
        if (!mStartedIteration) {
            mStartedIteration = true;
            mWorkHandler.sendEmptyMessage(MSG);
        }
    }

    @Override
    protected boolean processMessageInWorkThread(Message msg) {
        synchronized (this) {
            if (mDetached) {
                return true;
            }
            final long startTime = SystemClock.elapsedRealtime();
            final Map<T, ItemInfo> map = this.mMap;
            final long interval = mCountdownInterval;

            final List<Map.Entry<T, ItemInfo>> mTempList = this.mTempList;
            mTempList.addAll(map.entrySet());

            for (Map.Entry<T, ItemInfo> en : mTempList) {
                if (!en.getKey().decreaseLeftTime(interval)) {
                    mTempList.remove(en);
                    map.remove(en.getKey());
                    if (Debugger.DEBUG) {
                        Logger.i(TAG, "processMessageInWorkThread", "an item removed: " + en.getValue());
                    }
                }
            }
            if (mEnableUpdateView) {
                mMainHandler.obtainMessage(MSG, startTime).sendToTarget();
            } else {
                //update data , but not update ui.
                mWorkHandler.sendEmptyMessageDelayed(MSG, mCountdownInterval);
            }
        }
        return true;
    }

    @Override
    protected boolean processMessageInMainThread(Message msg) {
        final long startTime = (long) msg.obj;
        synchronized (this) {
            if (++mIterationCount == mMaxIterationCount) {
                mIterationCount = mMaxIterationCount;
                mDetached = true;
                return false;
            }
            notifyIntervalTimeChanged();
        }
        final long consumeTime = SystemClock.elapsedRealtime() - startTime;
        if (Debugger.DEBUG) {
            Logger.i(TAG, "processMessageInMainThread", "consumeTime = " + consumeTime);
        }
        mWorkHandler.sendEmptyMessageDelayed(MSG, mCountdownInterval - consumeTime);
        return true;
    }

    private void notifyIntervalTimeChanged() {
        final AdapterManager<T> am = getAdapter().getAdapterManager();
        final ArrayList<Integer> mRemovedPoss = this.mRemovedPositions;
        final boolean shouldAdjust = mAdjustPosition;
        ItemInfo info;
        for (Map.Entry<T, ItemInfo> en : mTempList) {
            info = en.getValue();
            if (shouldAdjust) {
                /**
                 * sometimes getAdapterPosition() may return -1, caused by the item is not showing in RecyclerView.
                 * @ee {@link RecyclerView#getChildAdapterPosition(View)} .
                 * so we need adjust by myself.
                 */
                int pos = info.getAdapterPosition();
                if (pos == -1) {
                    pos = info.getPosition();
                    for (int position : mRemovedPoss) {
                        /**
                         * we care about delete position < target position, position == lastPos ? , impossible.
                         */
                        if (position < pos) {
                            pos--;
                        }
                    }
                }
                info.setPosition(pos);
                am.notifyItemChanged(pos);
            } else {
                am.notifyItemChanged(info.getPosition());
            }
            if (Debugger.DEBUG) {
                Logger.d(TAG, "main_notifyItemChanged", "pos = " + info.mPosition);
            }
        }
        mAdjustPosition = false;
        mRemovedPoss.clear();
        mTempList.clear();
    }

    private void checkAdapter() {
        if (mAdapter == null) {
            throw new IllegalStateException("AdapterManager is null, have you call attached() ? .");
        }
    }

    private QuickRecycleViewAdapter<T> getAdapter() {
        return mAdapter;
    }

    private class AdapterObserverImpl extends RecyclerView.AdapterDataObserver
            implements AdapterManager.IAdapterDataRemovedObserver<T> {

        public void onChanged() {
            // Do nothing
            //Logger.i(TAG, "onChanged", "");
        }

        public void onItemRangeChanged(int positionStart, int itemCount) {
            // do nothing
            //Logger.i(TAG, "onItemRangeChanged", "");
        }

        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            // fallback to onItemRangeChanged(positionStart, itemCount) if app
            // does not override this method.
            onItemRangeChanged(positionStart, itemCount);
        }

        public void onItemRangeInserted(int positionStart, int itemCount) {
            //Logger.i(TAG, "onItemRangeInserted", "");
            checkAdapter();
            final QuickRecycleViewAdapter<T> adapter = getAdapter();
            final AdapterManager<T> am = adapter.getAdapterManager();
            final Map<T, ItemInfo> mMap = CountDownManager2.this.mMap;
            int pos;
            T bean;
            for (int i = 0; i < itemCount; i++) {
                pos = positionStart + i;
                if (adapter.isHeader(pos) || adapter.isFooter(pos)) {
                    continue;
                }
                bean = am.getItemAt(pos);
                mMap.put(bean, new ItemInfo(pos));
            }
            startLoop();
        }

        public void onItemRangeRemoved(int positionStart, int itemCount) {
            // do nothing, moved to #onItemRangeRemoved(List<T> items)
        }

        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            // do nothing
            // Logger.i(TAG, "onItemRangeMoved", "");
        }

        @Override
        public void onItemRangeRemoved(List<T> items) {
            // Logger.i(TAG, "onItemRangeRemoved", "");
            final Map<T, ItemInfo> mMap = CountDownManager2.this.mMap;
            final ArrayList<Integer> mRemovedPositions = CountDownManager2.this.mRemovedPositions;
            for (T t : items) {
                mRemovedPositions.add(mMap.remove(t).getPosition());
                // Logger.i(TAG, "onItemRangeRemoved", "an item removed: " + t);
            }
            //notify 尚未完成，不能在这里adjust position.
            //adjustPositions();
            mAdjustPosition = true;
        }
    }

    private class ItemInfo {
        private int mPosition;

        ItemInfo(int position) {
            this.mPosition = position;
        }

        /**
         * get Adapter Position
         *
         * @return the position . may be -1 , if  the view holder not bound.
         */
        int getAdapterPosition() {
            return getAdapter().getAdapterManager().getAdapterPosition(mPosition);
        }

        int setAndGetPosition() {
            final int pos = getAdapter().getAdapterManager().getAdapterPosition(mPosition);
            if (pos != -1) {
                Logger.i(TAG, "setAndGetPosition", "change position from " + mPosition + " to " + pos);
                return (mPosition = pos);
            }
            /**
             * 15个item. 删除第2个后, 只有第12，第14个 adjust position failed， why?
             */
            Logger.w(TAG, "setAndGetPosition", "change position failed. pos = " + mPosition);
            return mPosition;
        }

        int getPosition() {
            return mPosition;
        }

        void setPosition(int position) {
            this.mPosition = position;
        }
    }


}
