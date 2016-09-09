package com.heaven7.android.adapter.countdown;


public interface ICountDownCallback<T extends ILeftTimeGetter> {
        // millisUntilFinished = left time
     void onTick(int position, T bean, long millisUntilFinished);
     void onFinish(int position, T bean);
}
