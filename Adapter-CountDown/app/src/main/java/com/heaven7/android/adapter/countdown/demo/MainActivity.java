package com.heaven7.android.adapter.countdown.demo;

import com.heaven7.android.adapter.countdown.demo.sample.ItemCountDownTest;

import java.util.List;

/**
 * Created by heaven7 on 2016/5/25.
 */
public class MainActivity extends AbsMainActivity {

    @Override
    protected void addDemos(List<ActivityInfo> list) {
        list.add(new ActivityInfo(ItemCountDownTest.class, "test ItemCountDown"));
    }
}
