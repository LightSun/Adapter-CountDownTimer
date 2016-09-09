package com.heaven7.android.adapter.countdown;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

public abstract class CountDownDataObserver<T extends ILeftTimeGetter> extends RecyclerView.AdapterDataObserver {

    private final CountDownManager<T> mCDM;

    public CountDownDataObserver(CountDownManager<T> cdm) {
        this.mCDM = cdm;
    }

    @Override
    public void onChanged() {
        mCDM.cancelAll();
        int size = getItemSize();
        for (int i = 0; i < size; i++) {
            mCDM.addCountDownTask(i, getItem(i));
        }
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
        onItemRangeChanged(positionStart, itemCount);
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount) {
        int pos;
        T bean;
        for (int i = 0; i < itemCount; i++) {
            pos = positionStart + i;
            bean = getItem(pos);
            mCDM.notifyTimeChanged(pos, bean);
        }
    }

    @Override
    public void onItemRangeInserted(int positionStart, int itemCount) {
        int pos;
        T bean;
        for (int i = 0; i < itemCount; i++) {
            pos = positionStart + i;
            bean = getItem(pos);
            mCDM.addCountDownTask(pos, bean);
        }
    }

    @Override
    public void onItemRangeRemoved(int positionStart, int itemCount) {
        for (int i = 0; i < itemCount; i++) {
            mCDM.cancel(getItem(positionStart + i));
        }
    }

    @Override
    public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
        int pos;
        T bean;
        for (int i = 0; i < itemCount; i++) {
            pos = fromPosition + i;
            bean = getItem(pos);
            mCDM.notifyPositionChanged(pos, bean);
        }
    }

    @NonNull
    protected abstract T getItem(int position);

    protected abstract int getItemSize();
}
