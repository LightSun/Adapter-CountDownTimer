package com.heaven7.android.adapter.countdown;

/**
 * the count down able interface, indicate the data can count down or not.
 * Created by heaven7 on 2016/9/9.
 */
public interface ICountDownable {

    /**
     * should count down for the data
     * @return true if should count down.
     */
    boolean  shouldCountDown();
}
