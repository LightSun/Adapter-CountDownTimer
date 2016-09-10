package com.heaven7.android.adapter.countdown.demo.extra;

import com.heaven7.adapter.BaseSelector;
import com.heaven7.android.adapter.countdown.ILeftTimeGetter;

/**
 * Created by heaven7 on 2016/9/10.
 */
public class TestBean extends BaseSelector implements ILeftTimeGetter{

    public long leftTime;

    public TestBean(long leftTime) {
        this.leftTime = leftTime;
    }

    @Override
    public long getLeftTime() {
        return leftTime;
    }
}
