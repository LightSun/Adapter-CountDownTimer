package com.heaven7.android.adapter.countdown.demo.sample;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.heaven7.adapter.AdapterManager;
import com.heaven7.adapter.QuickRecycleViewAdapter;
import com.heaven7.android.adapter.countdown.CountDownCallbackImpl;
import com.heaven7.android.adapter.countdown.CountDownManager;
import com.heaven7.android.adapter.countdown.demo.BaseActivity;
import com.heaven7.android.adapter.countdown.demo.R;
import com.heaven7.android.adapter.countdown.demo.extra.TestBean;
import com.heaven7.core.util.Logger;
import com.heaven7.core.util.ViewHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import butterknife.InjectView;
import butterknife.OnClick;

/**
 * recycler view 或者list view里面item倒计时。测试
 * Created by heaven7 on 2016/9/9.
 */
public class ItemCountDownTest extends BaseActivity {

    private static final SimpleDateFormat DF = new SimpleDateFormat("HH:mm:ss");
    private static final String TAG = "ItemCountDownTest";

    @InjectView(R.id.bt_add)
    Button mBt_add;
    @InjectView(R.id.bt_delete)
    Button mBt_delete;
    @InjectView(R.id.bt_update)
    Button mBt_update;

    @InjectView(R.id.rv)
    RecyclerView mRv;

    private QuickRecycleViewAdapter<TestBean> mAdapter;
    private long mMaxLeftTime = 60*1000;

    private final CountDownManager<TestBean> mCDM = new CountDownManager<TestBean>(1000);

    @Override
    protected int getlayoutId() {
        return R.layout.ac_item_count_down;
    }

    //TODO item 多了之后删除有bug
    @Override
    protected void initData(Bundle savedInstanceState) {
         DF.setTimeZone(TimeZone.getTimeZone("UTC"));
         mRv.setLayoutManager(new LinearLayoutManager(this));

         mAdapter = new QuickRecycleViewAdapter<TestBean>(android.R.layout.simple_list_item_1,
                 new ArrayList<TestBean>()) {
             @Override
             protected void onBindData(Context context, int position, final TestBean item, int itemLayoutId, final ViewHelper helper) {
                 final TextView tv = helper.getView(android.R.id.text1);
                 mCDM.setCountDownCallback(item, new CountDownCallbackImpl<TestBean>(position, tv) {
                     @Override
                     protected CharSequence format(int position, TestBean bean, long millisUntilFinished) {
                         return DF.format(new Date(millisUntilFinished) ) ;
                     }
                 });
             }
         };
        //必须在setAdapter之前调用
        mCDM.attachCountDownObserver(mAdapter);
        mRv.setAdapter(mAdapter);
      //  addTestData();
    }

    @Override
    protected void onDestroy() {
        //因为有timer.必须调用这个
        mCDM.detachCountDownObserver();
        mCDM.cancelAll();
        super.onDestroy();
    }

    private void addTestData() {
        long minLeftTime = 20000; //最小20秒
        long maxLeftTime = 0;
        for(int i=0, size = 30 ;i<size ;i++){
            maxLeftTime = minLeftTime + i * 2000;
            mAdapter.getAdapterManager().addItem(new TestBean(maxLeftTime));//每个多2秒钟
        }
        this.mMaxLeftTime =  maxLeftTime;
    }

    //在末尾添加一条数据
    @OnClick(R.id.bt_add)
    public void onClickAdd(View v){
        this.mMaxLeftTime += 2000;
        mAdapter.getAdapterManager().addItem(new TestBean(mMaxLeftTime));
        Logger.i(TAG, "onClickAdd", "item added, position = " + (mAdapter.getAdapterManager().getItemSize() -1) );
    }

    //删除一条数据
    @OnClick(R.id.bt_delete)
    public void onClickDelete(View v){
        //当前item个数
        final int itemSize = mAdapter.getAdapterManager().getItemSize();
        if(itemSize == 0){
            return;
        }
        if(itemSize > 1){
            mAdapter.getAdapterManager().removeItem(1);
            Logger.i(TAG, "onClickUpdate", "item deleted, position = " + 1);
        }else{
            mAdapter.getAdapterManager().removeItem(0);
            Logger.i(TAG, "onClickUpdate", "item deleted, position = " + 0);
        }
    }
    //更新一条数据
    @OnClick(R.id.bt_update)
    public void onClickUpdate(View v){
        final int itemSize = mAdapter.getAdapterManager().getItemSize();
        if(itemSize == 0){
            return;
        }
        if(itemSize > 1){
            int index = 1;
            mAdapter.getAdapterManager().performItemChange(1, mChanger);
            Logger.i(TAG, "onClickUpdate", "item changed, position = " + index +" ,left time = " + mMaxLeftTime);
        }else{
            mAdapter.getAdapterManager().performItemChange(0, mChanger);
            Logger.i(TAG, "onClickUpdate", "item changed, position = " + 0 +" ,left time = " + mMaxLeftTime);
        }
    }

    private final AdapterManager.ItemChanger<TestBean> mChanger = new AdapterManager.ItemChanger<TestBean>() {
        @Override
        public boolean onItemChange(TestBean testBean) {
            testBean.leftTime = (mMaxLeftTime += 2000);
            return true ;
        }
    };
}
