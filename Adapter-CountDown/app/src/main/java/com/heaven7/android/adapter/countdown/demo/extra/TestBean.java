package com.heaven7.android.adapter.countdown.demo.extra;

import com.heaven7.adapter.BaseSelector;
import com.heaven7.android.adapter.countdown.ILeftTimeGetter;

/**
 * Created by heaven7 on 2016/9/10.
 */
public class TestBean extends BaseSelector implements ILeftTimeGetter {

    private static int sId = 0;
    private final int id;

    public long leftTime;

    public TestBean(long leftTime) {
        this.leftTime = leftTime;
        this.id = sId ++;
    }

    @Override
    public long getLeftTime() {
        return leftTime;
    }

    @Override
    public boolean decreaseLeftTime(long delta) {
        if (leftTime == 0) {
            return false;
        }
        if (leftTime > delta) {
            leftTime -= delta;
        } else {
            leftTime = 0;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TestBean{" +
                "id=" + id +
                ", leftTime=" + leftTime +
                '}';
    }
}
