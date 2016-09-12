package com.heaven7.android.adapter.countdown;

import android.support.v7.widget.RecyclerView;

import com.heaven7.adapter.AdapterManager;
import com.heaven7.adapter.QuickRecycleViewAdapter;

import java.util.List;

/**
 * the count down data observer.
 * @param <T> the data
 */
public class CountDownDataObserver<T extends ILeftTimeGetter> extends RecyclerView.AdapterDataObserver
        implements AdapterManager.IAdapterDataRemovedObserver<T>{

    private final CountDownManager<T> mCDM;
    private final QuickRecycleViewAdapter<T> mAdapter;

    public CountDownDataObserver(CountDownManager<T> cdm,QuickRecycleViewAdapter<T> mAdapter) {
        this.mCDM = cdm;
        this.mAdapter = mAdapter;
    }

    public void attach(){
        mAdapter.registerAdapterDataObserver(this);
        mAdapter.getAdapterManager().registerAdapterDataRemoveObserver(this);
    }

    public void detach(){
        mAdapter.getAdapterManager().unregisterAdapterDataRemoveObserver(this);
        mAdapter.unregisterAdapterDataObserver(this);
    }

    private AdapterManager<T> getAdapterManager(){
        return mAdapter.getAdapterManager();
    }

    @Override
    public void onChanged() {
        final AdapterManager<T> am = getAdapterManager();
        int size = am.getItemSize();
        for (int i = 0; i < size; i++) {
            mCDM.addCountDownTask( i, am.getItemAt(i) , false);
        }
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
        onItemRangeChanged(positionStart, itemCount);
    }

    @Override
    public void onItemRangeChanged(int positionStart, int itemCount) {
        final AdapterManager<T> am = getAdapterManager();
        final AdapterManager.IHeaderFooterManager hf = this.mAdapter;
        int pos;
        T bean;
        for (int i = 0; i < itemCount; i++) {
            pos = positionStart + i;
            if(hf.isHeader(pos) || hf.isFooter(pos)){
                continue;
            }
            bean = am.getItemAt(pos);
            mCDM.notifyLeftTimeChanged(pos, bean);
        }
    }

    @Override
    public void onItemRangeInserted(int positionStart, int itemCount) {
        //may be header and footer.
        final AdapterManager<T> am = getAdapterManager();
        final AdapterManager.IHeaderFooterManager hf = this.mAdapter;
        int pos;
        T bean;
        for (int i = 0; i < itemCount; i++) {
            pos = positionStart + i;
            if(hf.isHeader(pos) || hf.isFooter(pos)){
                continue;
            }
            bean = am.getItemAt(pos);
            mCDM.addCountDownTask(pos, bean, true);
        }
    }

    @Override
    public void onItemRangeRemoved(int positionStart, int itemCount) {
        //change to implement by #onItemRangeRemoved(...)
    }

    @Override
    public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
        final AdapterManager<T> am = getAdapterManager();
        final AdapterManager.IHeaderFooterManager hf = this.mAdapter;
        int pos;
        T bean;
        for (int i = 0; i < itemCount; i++) {
            pos = toPosition + i; //get the reach position
            if(hf.isHeader(pos) || hf.isFooter(pos)){
                continue;
            }
            bean = am.getItemAt(pos);
            mCDM.notifyPositionChanged(pos, bean);
        }
    }

    @Override
    public void onItemRangeRemoved(List<T> items) {
        for(T t : items){
            mCDM.cancel(t);
        }
    }
}
