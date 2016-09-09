package com.heaven7.android.adapter.countdown;

import android.widget.TextView;

/**
 * the count down callback
 * @param <T>
 */
public abstract class CountDownCallbackImpl<T extends ILeftTimeGetter> implements ICountDownCallback<T> {

    private final TextView mTv;

    public CountDownCallbackImpl(int position, TextView tv) {
        this.mTv = tv;
        tv.setTag(R.id.count_down_position, position);
    }

    @Override
    public void onTick(int pos, T bean, long millisUntilFinished) {
        if (pos == getPosition()) {
            mTv.setText(format(pos,bean, millisUntilFinished));
        }
        //DF.format(new Date(millisUntilFinished)
    }

    private int getPosition() {
        return (int) mTv.getTag(R.id.count_down_position);
    }
    @Override
    public void onPositionChanged(int oldPosition, int newPosition) {
        if(getPosition() == oldPosition){
            mTv.setTag(R.id.count_down_position, newPosition);
        }
    }

    /**
     * format the millisUntilFinished to text, which will set to {@link TextView}
     * @param position the position , often is the position of adapter
     * @param millisUntilFinished the millseconds util finish  or 0 if finished.
     * @param bean the item data
     * @return the formatted text to show.
     */
    protected  abstract CharSequence format(int position, T bean , long millisUntilFinished);
}
