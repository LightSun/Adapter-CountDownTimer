package com.heaven7.android.adapter.countdown;

/**
 * the count down callback
 * @param <T> the data which implements ILeftTimeGetter
 */
public interface ICountDownCallback<T extends ILeftTimeGetter> {

    /**
     * called when the interval is ticked.
     * @param position  the position of data in adapter
     * @param bean the  data
     * @param leftTime the left time until finish. 0 means is finished.
     */
    void onTick(int position, T bean, long leftTime);

    /**
     * called when the position is changed.
     * @param oldPosition  the old position
     * @param newPosition  the newPosition
     */
    void onPositionChanged(int oldPosition, int newPosition);
}
