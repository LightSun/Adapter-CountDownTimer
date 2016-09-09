package com.heaven7.android.adapter.countdown;

import com.heaven7.adapter.ISelectable;

/**
 * the left time getter
 * Created by heaven7 on 2016/9/9.
 */
public interface ILeftTimeGetter extends ISelectable{

    /**
     * get the left time in millisecond
     * @return  the left time in millisecond
     */
    long getLeftTime();

}
